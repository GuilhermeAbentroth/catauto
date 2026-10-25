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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.model.Modelo;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.ModeloRepository;

@RestController
@RequestMapping("/api/modelos")
public class ModeloApiController {

    public record ModeloDto(Long id, String nome, Long marcaId, String marca) {
        static ModeloDto de(Modelo m) {
            Marca marca = m.getMarca();
            return new ModeloDto(m.getId(), m.getNome(),
                    marca == null ? null : marca.getId(),
                    marca == null ? null : marca.getNome());
        }
    }

    private final ModeloRepository repository;
    private final MarcaRepository marcaRepository;

    public ModeloApiController(ModeloRepository repository, MarcaRepository marcaRepository) {
        this.repository = repository;
        this.marcaRepository = marcaRepository;
    }

    /** Sem parametro lista todos; com ?marca=GM filtra pelo nome da marca. */
    @GetMapping
    public List<ModeloDto> listar(@RequestParam(required = false) String marca) {
        List<Modelo> modelos = marca == null ? repository.findAll() : repository.findByMarcaNome(marca);
        return modelos.stream().map(ModeloDto::de).toList();
    }

    @GetMapping("/{id}")
    public ModeloDto buscar(@PathVariable Long id) {
        return ModeloDto.de(obter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloDto criar(@RequestBody ModeloDto dto) {
        Modelo modelo = new Modelo(dto.nome());
        modelo.setMarca(obterMarca(dto.marcaId()));
        return ModeloDto.de(repository.save(modelo));
    }

    @PutMapping("/{id}")
    public ModeloDto atualizar(@PathVariable Long id, @RequestBody ModeloDto dto) {
        Modelo modelo = obter(id);
        modelo.setNome(dto.nome());
        modelo.setMarca(obterMarca(dto.marcaId()));
        return ModeloDto.de(repository.save(modelo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        repository.delete(obter(id));
    }

    private Modelo obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo nao encontrado"));
    }

    private Marca obterMarca(Long marcaId) {
        if (marcaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marcaId e obrigatorio");
        }
        return marcaRepository.findById(marcaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca inexistente"));
    }
}
