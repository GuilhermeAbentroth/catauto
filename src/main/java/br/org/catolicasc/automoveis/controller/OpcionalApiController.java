package br.org.catolicasc.automoveis.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.org.catolicasc.automoveis.model.Opcional;
import br.org.catolicasc.automoveis.repository.OpcionalRepository;

@RestController
@RequestMapping("/api/opcionais")
public class OpcionalApiController {

    public record OpcionalDto(Long id, String nome) {
        static OpcionalDto de(Opcional o) {
            return new OpcionalDto(o.getId(), o.getNome());
        }
    }

    private final OpcionalRepository repository;

    public OpcionalApiController(OpcionalRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<OpcionalDto> listar() {
        return repository.findAll().stream().map(OpcionalDto::de).toList();
    }

    @GetMapping("/{id}")
    public OpcionalDto buscar(@PathVariable Long id) {
        return OpcionalDto.de(obter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpcionalDto criar(@RequestBody OpcionalDto dto) {
        return OpcionalDto.de(repository.save(new Opcional(dto.nome())));
    }

    @PutMapping("/{id}")
    public OpcionalDto atualizar(@PathVariable Long id, @RequestBody OpcionalDto dto) {
        Opcional opcional = obter(id);
        opcional.setNome(dto.nome());
        return OpcionalDto.de(repository.save(opcional));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        repository.delete(obter(id));
    }

    private Opcional obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opcional nao encontrado"));
    }
}
