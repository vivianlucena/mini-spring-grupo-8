package br.com.minispring.exemplo.controller;

import br.com.minispring.framework.annotation.*;
import br.com.minispring.framework.web.HttpResult;
import br.com.minispring.exemplo.model.Servico;
import br.com.minispring.exemplo.model.ServicoInput;
import br.com.minispring.exemplo.service.ServicoService;

import java.util.List;

@RestController("/servicos")
public final class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @Route(method = "GET")
    public List<Servico> list(@RequestParam("nome") String nome) {
        return service.list(nome);
    }

    @Route(method = "GET", path = "/{id}")
    public Servico get(@PathVariable("id") long id) {
        return service.get(id);
    }

    @Route(method = "POST")
    public HttpResult create(@RequestBody ServicoInput input) {
        Servico servico = service.create(input);
        return HttpResult.created("/servicos/" + servico.id(), servico);
    }

    @Route(method = "PUT", path = "/{id}")
    public Servico update(
            @PathVariable("id") long id,
            @RequestBody ServicoInput input
    ) {
        return service.update(id, input);
    }

    @Route(method = "DELETE", path = "/{id}")
    public HttpResult delete(@PathVariable("id") long id) {
        service.delete(id);
        return HttpResult.noContent();
    }
}