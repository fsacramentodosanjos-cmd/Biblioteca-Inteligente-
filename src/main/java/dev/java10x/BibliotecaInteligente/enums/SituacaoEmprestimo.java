package dev.java10x.BibliotecaInteligente.enums;

public enum SituacaoEmprestimo {

    EM_ABERTO("Em aberto", "aberto"),
    ATRASADO("Atrasado", "atrasado"),
    DEVOLVIDO("Devolvido", "devolvido");

    private final String rotulo;
    private final String css;

    SituacaoEmprestimo(String rotulo, String css) {
        this.rotulo = rotulo;
        this.css = css;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getCss() {
        return css;
    }
}
