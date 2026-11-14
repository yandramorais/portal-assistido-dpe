package br.gov.go.defensoria.portal.solicitacao.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import br.gov.go.defensoria.portal.auth.domain.Perfil;
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

public record SolicitacaoDetalheResponse(
        String protocolo,
        AreaAtuacao area,
        String areaDescricao,
        String assunto,
        String relato,
        Modalidade modalidade,
        StatusSolicitacao status,
        Prioridade prioridade,
        Hipossuficiencia hipossuficiencia,
        UnidadeResponse unidade,
        Instant dataAgendamento,
        String localAtendimento,
        String encaminhamentoDestino,
        String motivoIndeferimento,
        List<DocumentoItem> documentos,
        List<HistoricoItem> historico,
        List<MensagemItem> mensagens,
        Instant criadoEm,
        Instant atualizadoEm) {

    public record DocumentoItem(TipoDocumento tipo, boolean entregue) {
    }

    public record HistoricoItem(StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo, String observacao,
            Instant criadoEm) {

        static HistoricoItem de(HistoricoStatus h) {
            return new HistoricoItem(h.getStatusAnterior(), h.getStatusNovo(), h.getObservacao(), h.getCriadoEm());
        }
    }

    public record MensagemItem(Long id, String autor, boolean daEquipe, String conteudo, Instant criadoEm,
            Instant lidaEm) {

        static MensagemItem de(Mensagem m) {
            return new MensagemItem(m.getId(), m.getAutor().getNome(), m.getAutor().getPerfil() != Perfil.ASSISTIDO,
                    m.getConteudo(), m.getCriadoEm(), m.getLidaEm());
        }
    }

    public static SolicitacaoDetalheResponse de(Solicitacao s, List<HistoricoStatus> historico,
            List<Mensagem> mensagens) {
        List<DocumentoItem> documentos = Arrays.stream(TipoDocumento.values())
                .filter(s.getDocumentos()::containsKey)
                .map(tipo -> new DocumentoItem(tipo, s.getDocumentos().get(tipo)))
                .toList();

        return new SolicitacaoDetalheResponse(s.getProtocolo(), s.getArea(), s.getArea().getDescricao(),
                s.getAssunto(), s.getRelato(), s.getModalidade(), s.getStatus(), s.getPrioridade(),
                s.getHipossuficiencia(), UnidadeResponse.de(s.getUnidade()), s.getDataAgendamento(),
                s.getLocalAtendimento(), s.getEncaminhamentoDestino(), s.getMotivoIndeferimento(), documentos,
                historico.stream().map(HistoricoItem::de).toList(),
                mensagens.stream().map(MensagemItem::de).toList(),
                s.getCriadoEm(), s.getAtualizadoEm());
    }
}
