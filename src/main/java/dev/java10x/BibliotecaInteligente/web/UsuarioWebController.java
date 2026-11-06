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

import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Usuario;
import dev.java10x.BibliotecaInteligente.service.UsuarioService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    public UsuarioWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("usuarios", usuarioService.buscar(q));
        model.addAttribute("q", q == null ? "" : q);
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult resultado,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "usuarios/form";
        }
        try {
            usuarioService.salvar(usuario);
        } catch (RegraNegocioException e) {
            resultado.reject("regra", e.getMessage());
            return "usuarios/form";
        }
        redirect.addFlashAttribute("sucesso", "Usuário cadastrado: " + usuario.getNome() + ".");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable("id") Long id, @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult resultado, RedirectAttributes redirect) {
        usuario.setId(id);
        if (resultado.hasErrors()) {
            return "usuarios/form";
        }
        try {
            usuarioService.atualizar(id, usuario);
        } catch (RegraNegocioException e) {
            resultado.reject("regra", e.getMessage());
            return "usuarios/form";
        }
        redirect.addFlashAttribute("sucesso", "Usuário atualizado: " + usuario.getNome() + ".");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            usuarioService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Usuário excluído.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
