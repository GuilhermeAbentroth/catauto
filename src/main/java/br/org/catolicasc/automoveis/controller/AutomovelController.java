package br.org.catolicasc.automoveis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;

@Controller
public class AutomovelController {

    private final AutomovelRepository repository;

    public AutomovelController(AutomovelRepository repository) {
        this.repository = repository;
    }

    /** A frota mostra so o estoque: automoveis vendidos ficam de fora. */
    @GetMapping("/")
    public String listar(Model model) {
        Iterable<Automovel> automoveis = repository.findByVendidoFalse();
        model.addAttribute("Automovel", automoveis);
        return "lista";
    }

}
