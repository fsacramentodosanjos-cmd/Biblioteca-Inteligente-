package dev.java10x.BibliotecaInteligente.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import dev.java10x.BibliotecaInteligente.exception.NaoEncontradoException;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;

/** Páginas de erro amigáveis para a interface web. */
@ControllerAdvice(basePackages = "dev.java10x.BibliotecaInteligente.web")
public class WebExceptionHandler {

    @ExceptionHandler(NaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(NaoEncontradoException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("mensagem", e.getMessage());
        return "error/erro";
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String regraDeNegocio(RegraNegocioException e, Model model) {
        model.addAttribute("status", 409);
        model.addAttribute("mensagem", e.getMessage());
        return "error/erro";
    }
}
