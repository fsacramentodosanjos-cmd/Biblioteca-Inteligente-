package dev.java10x.BibliotecaInteligente.enums;

public enum StatusLivros {

    DISPONIVEL("Disponível", "disponivel"),
    EMPRESTADO("Emprestado", "emprestado"),
    PERDIDO("Perdido", "perdido");

    private final String rotulo;
    private final String css;

    StatusLivros(String rotulo, String css) {
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
