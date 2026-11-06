package dev.java10x.BibliotecaInteligente.web;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.java10x.BibliotecaInteligente.config.BibliotecaProperties;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoRequest;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoResponse;
import dev.java10x.BibliotecaInteligente.exception.NaoEncontradoException;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.service.EmprestimoService;
import dev.java10x.BibliotecaInteligente.service.LivroService;
import dev.java10x.BibliotecaInteligente.service.UsuarioService;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoWebController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmprestimoService emprestimoService;
    private final UsuarioService usuarioService;
    private final LivroService livroService;
    private final BibliotecaProperties regras;
    private final Clock clock;

    public EmprestimoWebController(
            EmprestimoService emprestimoService,
            UsuarioService usuarioService,
            LivroService livroService,
            BibliotecaProperties regras,
            Clock clock) {
        this.emprestimoService = emprestimoService;
        this.usuarioService = usuarioService;
        this.livroService = livroService;
        this.regras = regras;
        this.clock = clock;
    }

    @GetMapping
    public String listar(@RequestParam(name = "filtro", defaultValue = "todos") String filtro, Model model) {
        String filtroValido = normalizar(filtro);
        model.addAttribute("filtro", filtroValido);
        model.addAttribute("emprestimos", emprestimoService.listarPorFiltro(filtroValido));
        model.addAttribute("maxRenovacoes", regras.getMaxRenovacoes());
        return "emprestimos/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(name = "livroId", required = false) Long livroId, Model model) {
        LocalDate hoje = LocalDate.now(clock);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("livros", livroService.listarDisponiveis());
        model.addAttribute("livroIdSelecionado", livroId);
        model.addAttribute("hoje", hoje);
        model.addAttribute("prazoPadrao", regras.getPrazoPadraoDias());
        model.addAttribute("dataSugerida", hoje.plusDays(regras.getPrazoPadraoDias()));
        model.addAttribute("limite", regras.getLimiteEmprestimos());
        return "emprestimos/novo";
    }

    @PostMapping
    public String realizar(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("livroId") Long livroId,
            @RequestParam(name = "dataDevolucaoPrevista", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDevolucaoPrevista,
            RedirectAttributes redirect) {
        try {
            EmprestimoResponse emprestimo = emprestimoService.realizarEmprestimo(
                    new EmprestimoRequest(usuarioId, livroId, dataDevolucaoPrevista));
            redirect.addFlashAttribute("sucesso",
                    "Empréstimo registrado. Devolução prevista para "
                            + emprestimo.dataDevolucaoPrevista().format(FORMATO) + ".");
            return "redirect:/emprestimos";
        } catch (RegraNegocioException | NaoEncontradoException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            redirect.addAttribute("livroId", livroId);
            return "redirect:/emprestimos/novo";
        }
    }

    @PostMapping("/{id}/devolver")
    public String devolver(
            @PathVariable("id") Long id,
            @RequestParam(name = "filtro", defaultValue = "todos") String filtro,
            @RequestParam(name = "voltar", required = false) String voltar,
            RedirectAttributes redirect) {
        try {
            EmprestimoResponse emprestimo = emprestimoService.devolverLivro(id);
            String mensagem = "Devolução registrada: " + emprestimo.livroTitulo() + ".";
            if (emprestimo.diasAtraso() > 0) {
                mensagem += " Atraso de " + emprestimo.diasAtraso() + " dia(s), multa de R$ "
                        + emprestimo.multa().toPlainString().replace('.', ',') + ".";
            }
            redirect.addFlashAttribute("sucesso", mensagem);
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return destino(voltar, filtro, redirect);
    }

    @PostMapping("/{id}/renovar")
    public String renovar(
            @PathVariable("id") Long id,
            @RequestParam(name = "filtro", defaultValue = "todos") String filtro,
            RedirectAttributes redirect) {
        try {
            EmprestimoResponse emprestimo = emprestimoService.renovar(id);
            redirect.addFlashAttribute("sucesso",
                    "Empréstimo renovado até " + emprestimo.dataDevolucaoPrevista().format(FORMATO) + ".");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return destino(null, filtro, redirect);
    }

    private String destino(String voltar, String filtro, RedirectAttributes redirect) {
        if ("inicio".equals(voltar)) {
            return "redirect:/";
        }
        redirect.addAttribute("filtro", normalizar(filtro));
        return "redirect:/emprestimos";
    }

    private String normalizar(String filtro) {
        return ("abertos".equals(filtro) || "atrasados".equals(filtro)) ? filtro : "todos";
    }
}
