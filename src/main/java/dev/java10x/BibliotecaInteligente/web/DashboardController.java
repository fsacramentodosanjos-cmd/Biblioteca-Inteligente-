package dev.java10x.BibliotecaInteligente.web;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dev.java10x.BibliotecaInteligente.service.DashboardService;
import dev.java10x.BibliotecaInteligente.service.EmprestimoService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmprestimoService emprestimoService;
    private final Clock clock;

    public DashboardController(DashboardService dashboardService, EmprestimoService emprestimoService, Clock clock) {
        this.dashboardService = dashboardService;
        this.emprestimoService = emprestimoService;
        this.clock = clock;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("hoje", LocalDate.now(clock));
        model.addAttribute("resumo", dashboardService.resumo());
        model.addAttribute("atrasados", emprestimoService.listarAtrasados());
        model.addAttribute("recentes", emprestimoService.listarRecentes());
        return "index";
    }
}
