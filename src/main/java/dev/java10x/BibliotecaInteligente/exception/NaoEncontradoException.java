package dev.java10x.BibliotecaInteligente.exception;

/** Recurso inexistente (vira HTTP 404). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
