package dev.java10x.BibliotecaInteligente.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.java10x.BibliotecaInteligente.dto.EmprestimoRequest;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoResponse;
import dev.java10x.BibliotecaInteligente.service.EmprestimoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @GetMapping
    public List<EmprestimoResponse> listar() {
        return emprestimoService.listarTodos();
    }

    @GetMapping("/{id}")
    public EmprestimoResponse buscar(@PathVariable("id") Long id) {
        return emprestimoService.buscarPorId(id);
    }

    @GetMapping("/abertos")
    public List<EmprestimoResponse> listarEmAberto() {
        return emprestimoService.listarEmAberto();
    }

    @GetMapping("/atrasados")
    public List<EmprestimoResponse> listarAtrasados() {
        return emprestimoService.listarAtrasados();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<EmprestimoResponse> listarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return emprestimoService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResponse realizar(@Valid @RequestBody EmprestimoRequest pedido) {
        return emprestimoService.realizarEmprestimo(pedido);
    }

    @PutMapping("/{id}/devolver")
    public EmprestimoResponse devolver(@PathVariable("id") Long id) {
        return emprestimoService.devolverLivro(id);
    }

    @PutMapping("/{id}/renovar")
    public EmprestimoResponse renovar(@PathVariable("id") Long id) {
        return emprestimoService.renovar(id);
    }
}
