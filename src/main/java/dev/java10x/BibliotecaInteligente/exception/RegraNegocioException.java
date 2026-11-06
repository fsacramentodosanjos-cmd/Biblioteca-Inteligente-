package dev.java10x.BibliotecaInteligente.exception;

/** Operação que viola uma regra da biblioteca (vira HTTP 409). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
