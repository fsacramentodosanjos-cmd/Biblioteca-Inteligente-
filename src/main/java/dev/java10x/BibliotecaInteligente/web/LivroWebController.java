package dev.java10x.BibliotecaInteligente.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.java10x.BibliotecaInteligente.enums.CategoriaLivro;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.service.LivroService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/livros")
public class LivroWebController {

    private final LivroService livroService;

    public LivroWebController(LivroService livroService) {
        this.livroService = livroService;
    }

    @ModelAttribute("categorias")
    public CategoriaLivro[] categorias() {
        return CategoriaLivro.values();
    }

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("livros", livroService.buscar(q));
        model.addAttribute("q", q == null ? "" : q);
        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("livro", new Livro());
        return "livros/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("livro") Livro livro, BindingResult resultado,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "livros/form";
        }
        try {
            livroService.salvar(livro);
        } catch (RegraNegocioException e) {
            resultado.reject("regra", e.getMessage());
            return "livros/form";
        }
        redirect.addFlashAttribute("sucesso", "Livro cadastrado: " + livro.getTitulo() + ".");
        return "redirect:/livros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("livro", livroService.buscarPorId(id));
        return "livros/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable("id") Long id, @Valid @ModelAttribute("livro") Livro livro,
            BindingResult resultado, RedirectAttributes redirect) {
        livro.setId(id);
        if (resultado.hasErrors()) {
            return "livros/form";
        }
        try {
            livroService.atualizar(id, livro);
        } catch (RegraNegocioException e) {
            resultado.reject("regra", e.getMessage());
            return "livros/form";
        }
        redirect.addFlashAttribute("sucesso", "Livro atualizado: " + livro.getTitulo() + ".");
        return "redirect:/livros";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            livroService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Livro excluído.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/livros";
    }
}
