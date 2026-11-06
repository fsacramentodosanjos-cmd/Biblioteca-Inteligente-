package dev.java10x.BibliotecaInteligente.model;

import dev.java10x.BibliotecaInteligente.enums.CategoriaLivro;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false, length = 100)
    private String titulo;

    @NotBlank(message = "Nome do autor é obrigatório")
    @Column(nullable = false, length = 100)
    private String autor;

    @NotBlank(message = "ISBN é obrigatório")
    @Column(nullable = false, unique = true, length = 19)
    private String isbn;

    @NotBlank(message = "Nome da editora é obrigatório")
    @Column(nullable = false, length = 100)
    private String editora;

    @NotNull(message = "Ano de publicação é obrigatório")
    @Min(value = 1000, message = "Ano de publicação inválido")
    @Max(value = 2100, message = "Ano de publicação inválido")
    @Column(nullable = false)
    private Integer anoPublicacao;

    @NotNull(message = "Categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaLivro categoria;

    // Livro novo nasce disponível; depois só o fluxo de empréstimo/devolução muda para EMPRESTADO.
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLivros status = StatusLivros.DISPONIVEL;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public CategoriaLivro getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaLivro categoria) {
        this.categoria = categoria;
    }

    public StatusLivros getStatus() {
        return status;
    }

    public void setStatus(StatusLivros status) {
        this.status = status;
    }
}
