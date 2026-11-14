package br.gov.go.defensoria.portal.solicitacao.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import br.gov.go.defensoria.portal.TestcontainersConfiguration;
import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.assistido.repository.AssistidoRepository;
import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.auth.repository.UsuarioRepository;
import br.gov.go.defensoria.portal.notificacao.domain.StatusNotificacao;
import br.gov.go.defensoria.portal.notificacao.repository.NotificacaoEmailRepository;
import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;
import br.gov.go.defensoria.portal.solicitacao.domain.Modalidade;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import br.gov.go.defensoria.portal.unidade.repository.UnidadeRepository;
import br.gov.go.defensoria.portal.workflow.repository.HistoricoStatusRepository;
import br.gov.go.defensoria.portal.workflow.repository.MensagemRepository;
import jakarta.persistence.EntityManager;

/**
 * Sobe o Postgres 16 via Testcontainers, aplica o Liquibase (schema + seed, context dev)
 * e o Hibernate valida o mapeamento (ddl-auto=validate) ao criar o contexto.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("dev")
class SolicitacaoRepositoryTest {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;
    @Autowired
    private AssistidoRepository assistidoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UnidadeRepository unidadeRepository;
    @Autowired
    private HistoricoStatusRepository historicoRepository;
    @Autowired
    private MensagemRepository mensagemRepository;
    @Autowired
    private NotificacaoEmailRepository notificacaoRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private JdbcTemplate jdbc;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    void seedCarregadoEMapeado() {
        assertThat(unidadeRepository.findAllByOrderByNome()).hasSize(6);
        assertThat(usuarioRepository.count()).isEqualTo(13);
        assertThat(assistidoRepository.count()).isEqualTo(10);
        assertThat(solicitacaoRepository.count()).isEqualTo(contar("solicitacao"));
        assertThat(historicoRepository.count()).isEqualTo(contar("historico_status"));
        assertThat(mensagemRepository.count()).isEqualTo(contar("mensagem"));
        assertThat(notificacaoRepository.findByStatusOrderByCriadoEm(StatusNotificacao.FALHA)).isNotEmpty();

        assertThat(usuarioRepository.findByEmailIgnoreCase("DEFENSOR@dpe.local"))
                .get().extracting(u -> u.getPerfil()).isEqualTo(Perfil.DEFENSOR);
    }

    @Test
    void vulnerabilidadesComoElementCollection() {
        Assistido juliana = assistidoRepository.findByCpf("62819482112").orElseThrow();

        assertThat(juliana.getVulnerabilidades()).containsExactly(Vulnerabilidade.VIOLENCIA_DOMESTICA);
        assertThat(juliana.getUf()).isEqualTo("GO");
        assertThat(juliana.getUsuario().getPerfil()).isEqualTo(Perfil.ASSISTIDO);
    }

    @Test
    void documentosComoElementCollection() {
        Solicitacao aguardando = solicitacaoRepository
                .findAll(SolicitacaoSpecifications.comStatus(StatusSolicitacao.AGUARDANDO_TRIAGEM)).getFirst();

        assertThat(aguardando.getDocumentos())
                .hasSize(TipoDocumento.values().length)
                .containsEntry(TipoDocumento.DOCUMENTO_IDENTIFICACAO, true)
                .containsEntry(TipoDocumento.COMPROVANTE_RENDA, false);
    }

    @Test
    void kanbanCarregaTudoEmUmaUnicaConsulta() {
        em.clear();
        statistics.clear();

        List<Solicitacao> cards = solicitacaoRepository.findAll(SolicitacaoSpecifications.comDadosDoCard());
        cards.forEach(s -> {
            s.getAssistido().getNome();
            s.getUnidade().getNome();
            if (s.getResponsavel() != null) {
                s.getResponsavel().getNome();
            }
        });

        assertThat(cards).hasSize((int) contar("solicitacao"));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);

        // Checklist de todos os cards num único SELECT extra (default_batch_fetch_size), não um por card.
        cards.forEach(s -> s.getDocumentos().size());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }

    @Test
    void persisteNovaSolicitacaoComVersaoEDocumentos() {
        Assistido assistido = assistidoRepository.findByCpf("52601815906").orElseThrow();
        String protocolo = "DPE-TESTE-%06d".formatted(solicitacaoRepository.proximoNumeroProtocolo());

        Solicitacao nova = new Solicitacao(protocolo, assistido, unidadeRepository.findAllByOrderByNome().getFirst(),
                AreaAtuacao.SAUDE, "Fornecimento de fralda geriátrica", "Relato de teste.", Modalidade.VIRTUAL,
                Prioridade.ALTA, Hipossuficiencia.PRESUMIDA);
        nova.getDocumentos().put(TipoDocumento.DOCUMENTO_IDENTIFICACAO, false);
        solicitacaoRepository.saveAndFlush(nova);

        assertThat(nova.getVersao()).isZero();
        assertThat(nova.getStatus()).isEqualTo(StatusSolicitacao.AGUARDANDO_TRIAGEM);
        assertThat(nova.getCriadoEm()).isNotNull();

        nova.setStatus(StatusSolicitacao.EM_ANALISE);
        nova.getDocumentos().put(TipoDocumento.DOCUMENTO_IDENTIFICACAO, true);
        solicitacaoRepository.saveAndFlush(nova);

        assertThat(nova.getVersao()).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "select entregue from solicitacao_documento where solicitacao_id = ? and tipo = 'DOCUMENTO_IDENTIFICACAO'",
                Boolean.class, nova.getId())).isTrue();

        em.clear();
        Solicitacao recarregada = solicitacaoRepository.findByProtocolo(protocolo).orElseThrow();
        assertThat(recarregada.getArea()).isEqualTo(AreaAtuacao.SAUDE);
        assertThat(recarregada.getArea().getDescricao()).isEqualTo("Saúde");
        assertThat(recarregada.getDocumentos()).containsEntry(TipoDocumento.DOCUMENTO_IDENTIFICACAO, true);
    }

    private long contar(String tabela) {
        return jdbc.queryForObject("select count(*) from " + tabela, Long.class);
    }
}
