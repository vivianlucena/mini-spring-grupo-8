
package br.com.minispring.exemplo.controller;

import br.com.minispring.exemplo.model.Servico;
import br.com.minispring.exemplo.model.ServicoInput;
import br.com.minispring.exemplo.service.ServicoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Servico> list(
            @RequestParam(value = "nome", required = false) String nome
    ) {
        return service.list(nome);
    }

    @GetMapping("/{id}")
    public Servico get(@PathVariable("id") long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Servico> create(
            @Valid @RequestBody ServicoInput input
    ) {
        Servico servico = service.create(input);

        URI location = URI.create("/servicos/" + servico.id());

        return ResponseEntity
                .created(location)
                .body(servico);
    }

    @PutMapping("/{id}")
    public Servico update(
            @PathVariable("id") long id,
            @Valid @RequestBody ServicoInput input
    ) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") long id
    ) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
