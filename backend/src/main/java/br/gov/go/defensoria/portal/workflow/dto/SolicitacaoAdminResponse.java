package br.gov.go.defensoria.portal.workflow.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.assistido.domain.Vulnerabilidade;
import br.gov.go.defensoria.portal.auth.domain.Perfil;
import br.gov.go.defensoria.portal.auth.domain.Usuario;
import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Hipossuficiencia;
import br.gov.go.defensoria.portal.solicitacao.domain.Modalidade;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.TipoDocumento;
import br.gov.go.defensoria.portal.unidade.dto.UnidadeResponse;
import br.gov.go.defensoria.portal.workflow.domain.HistoricoStatus;
import br.gov.go.defensoria.portal.workflow.domain.Mensagem;

public record SolicitacaoAdminResponse(
        String protocolo,
        Integer versao,
        StatusSolicitacao status,
        String statusDescricao,
        Set<StatusSolicitacao> proximosStatus,
        AreaAtuacao area,
        String areaDescricao,
        String assunto,
        String relato,
        Modalidade modalidade,
        Prioridade prioridade,
        Hipossuficiencia hipossuficiencia,
        AssistidoResumo assistido,
        UnidadeResponse unidade,
        String responsavel,
        Instant dataAgendamento,
        String localAtendimento,
        String encaminhamentoDestino,
        String motivoIndeferimento,
        List<Documento> documentos,
        List<Historico> historico,
        List<MensagemEquipe> mensagens,
        Instant criadoEm,
        Instant atualizadoEm) {

    public record AssistidoResumo(String nome, String nomeSocial, String cpf, LocalDate dataNascimento,
            String telefone, String email, String cidade, BigDecimal rendaIndividual, BigDecimal rendaFamiliar,
            Short membrosFamilia, boolean possuiImovel, boolean possuiVeiculo, Set<Vulnerabilidade> vulnerabilidades) {

        static AssistidoResumo de(Assistido a) {
            Set<Vulnerabilidade> vulnerabilidades = a.getVulnerabilidades().isEmpty()
                    ? EnumSet.noneOf(Vulnerabilidade.class)
                    : EnumSet.copyOf(a.getVulnerabilidades());
            return new AssistidoResumo(a.getNome(), a.getNomeSocial(), a.getCpf(), a.getDataNascimento(),
                    a.getTelefone(), a.getEmail(), a.getCidade(), a.getRendaIndividual(), a.getRendaFamiliar(),
                    a.getMembrosFamilia(), a.isPossuiImovel(), a.isPossuiVeiculo(), vulnerabilidades);
        }
    }

    public record Documento(TipoDocumento tipo, boolean entregue) {
    }

    public record Historico(StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo, String observacao,
            String autor, Instant criadoEm) {
    }

    public record MensagemEquipe(Long id, String autor, boolean daEquipe, boolean visivelAssistido, String conteudo,
            Instant criadoEm, Instant lidaEm) {
    }

    public static SolicitacaoAdminResponse de(Solicitacao s, List<HistoricoStatus> historico,
            List<Mensagem> mensagens) {
        List<Documento> documentos = Arrays.stream(TipoDocumento.values())
                .filter(s.getDocumentos()::containsKey)
                .map(tipo -> new Documento(tipo, s.getDocumentos().get(tipo)))
                .toList();

        return new SolicitacaoAdminResponse(s.getProtocolo(), s.getVersao(), s.getStatus(),
                s.getStatus().getDescricao(), s.getStatus().destinosPossiveis(), s.getArea(),
                s.getArea().getDescricao(), s.getAssunto(), s.getRelato(), s.getModalidade(), s.getPrioridade(),
                s.getHipossuficiencia(), AssistidoResumo.de(s.getAssistido()), UnidadeResponse.de(s.getUnidade()),
                nome(s.getResponsavel()), s.getDataAgendamento(), s.getLocalAtendimento(),
                s.getEncaminhamentoDestino(), s.getMotivoIndeferimento(), documentos,
                historico.stream()
                        .map(h -> new Historico(h.getStatusAnterior(), h.getStatusNovo(), h.getObservacao(),
                                nome(h.getAutor()), h.getCriadoEm()))
                        .toList(),
                mensagens.stream()
                        .map(m -> new MensagemEquipe(m.getId(), m.getAutor().getNome(),
                                m.getAutor().getPerfil() != Perfil.ASSISTIDO, m.isVisivelAssistido(),
                                m.getConteudo(), m.getCriadoEm(), m.getLidaEm()))
                        .toList(),
                s.getCriadoEm(), s.getAtualizadoEm());
    }

    private static String nome(Usuario usuario) {
        return usuario == null ? null : usuario.getNome();
    }
}
