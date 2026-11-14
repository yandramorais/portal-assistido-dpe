package br.gov.go.defensoria.portal.solicitacao.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import br.gov.go.defensoria.portal.assistido.domain.Assistido;
import br.gov.go.defensoria.portal.solicitacao.domain.AreaAtuacao;
import br.gov.go.defensoria.portal.solicitacao.domain.Prioridade;
import br.gov.go.defensoria.portal.solicitacao.domain.Solicitacao;
import br.gov.go.defensoria.portal.solicitacao.domain.StatusSolicitacao;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public final class SolicitacaoSpecifications {

    private static final char ESCAPE = '\\';

    private SolicitacaoSpecifications() {
    }

    public static Specification<Solicitacao> comDadosDoCard() {
        return (root, query, cb) -> {
            Class<?> resultado = query.getResultType();
            if (resultado != Long.class && resultado != long.class) {
                root.fetch("assistido");
                root.fetch("unidade");
                root.fetch("responsavel", JoinType.LEFT);
            }
            return null;
        };
    }

    public static Specification<Solicitacao> comStatus(StatusSolicitacao status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Solicitacao> daArea(AreaAtuacao area) {
        return area == null ? null : (root, query, cb) -> cb.equal(root.get("area"), area);
    }

    public static Specification<Solicitacao> daUnidade(Long unidadeId) {
        return unidadeId == null ? null : (root, query, cb) -> cb.equal(root.get("unidade").get("id"), unidadeId);
    }

    public static Specification<Solicitacao> comPrioridade(Prioridade prioridade) {
        return prioridade == null ? null : (root, query, cb) -> cb.equal(root.get("prioridade"), prioridade);
    }

    public static Specification<Solicitacao> comBusca(String termo) {
        if (!StringUtils.hasText(termo)) {
            return null;
        }
        String texto = termo.trim();
        String digitos = texto.replaceAll("\\D", "");

        return (root, query, cb) -> {
            Path<Assistido> assistido = root.get("assistido");
            String padraoMinusculo = contendo(texto.toLowerCase(Locale.ROOT));

            Predicate nome = cb.like(cb.lower(assistido.get("nome")), padraoMinusculo, ESCAPE);
            Predicate nomeSocial = cb.like(cb.lower(assistido.get("nomeSocial")), padraoMinusculo, ESCAPE);
            Expression<String> protocolo = cb.upper(root.get("protocolo"));
            Predicate porProtocolo = cb.like(protocolo, contendo(texto.toUpperCase(Locale.ROOT)), ESCAPE);

            if (digitos.isEmpty()) {
                return cb.or(nome, nomeSocial, porProtocolo);
            }
            Predicate cpf = cb.like(assistido.get("cpf"), contendo(digitos), ESCAPE);
            return cb.or(nome, nomeSocial, porProtocolo, cpf);
        };
    }

    private static String contendo(String termo) {
        String escapado = termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
