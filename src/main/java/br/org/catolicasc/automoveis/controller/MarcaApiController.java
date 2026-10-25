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

import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.repository.MarcaRepository;

@RestController
@RequestMapping("/api/marcas")
public class MarcaApiController {

    public record MarcaDto(Long id, String nome) {
        static MarcaDto de(Marca m) {
            return new MarcaDto(m.getId(), m.getNome());
        }
    }

    private final MarcaRepository repository;

    public MarcaApiController(MarcaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MarcaDto> listar() {
        return repository.findAll().stream().map(MarcaDto::de).toList();
    }

    @GetMapping("/{id}")
    public MarcaDto buscar(@PathVariable Long id) {
        return MarcaDto.de(obter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarcaDto criar(@RequestBody MarcaDto dto) {
        return MarcaDto.de(repository.save(new Marca(dto.nome())));
    }

    @PutMapping("/{id}")
    public MarcaDto atualizar(@PathVariable Long id, @RequestBody MarcaDto dto) {
        Marca marca = obter(id);
        marca.setNome(dto.nome());
        return MarcaDto.de(repository.save(marca));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        repository.delete(obter(id));
    }

    private Marca obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca nao encontrada"));
    }
}
