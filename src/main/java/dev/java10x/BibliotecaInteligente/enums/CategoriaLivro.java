package dev.java10x.BibliotecaInteligente.enums;

public enum CategoriaLivro {

    ROMANCE("Romance"),
    FICCAO("Ficção"),
    TECNOLOGIA("Tecnologia"),
    PROGRAMACAO("Programação"),
    BIOGRAFIA("Biografia"),
    HISTORIA("História"),
    CIENCIA("Ciência"),
    OUTROS("Outros");

    private final String rotulo;

    CategoriaLivro(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
