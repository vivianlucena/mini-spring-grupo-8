package br.com.minispring.exemplo.service;

import br.com.minispring.framework.annotation.Service;
import br.com.minispring.framework.web.HttpException;
import br.com.minispring.exemplo.model.Servico;
import br.com.minispring.exemplo.model.ServicoInput;
import br.com.minispring.exemplo.repository.ServicoRepository;

import java.util.List;
import java.util.Locale;

@Service
public final class ServicoService {

    private static final int DURACAO_MAXIMA = 240;

    private final ServicoRepository repository;

    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    public List<Servico> list(String nome) {
        if (nome == null || nome.isBlank()) {
            return repository.findAll();
        }

        String filtro = nome.toLowerCase(Locale.ROOT).trim();

        return repository.findAll()
                .stream()
                .filter(servico -> servico.nome().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    public Servico get(long id) {
        return repository.findById(id).orElseThrow(this::notFound);
    }

    public Servico create(ServicoInput input) {
        validateBusinessRule(input);
        return repository.create(input.nome().trim(), input.duracaoMinutos());
    }

    public Servico update(long id, ServicoInput input) {
        validateBusinessRule(input);
        return repository.update(id, input.nome().trim(), input.duracaoMinutos())
                .orElseThrow(this::notFound);
    }

    public void delete(long id) {
        if (!repository.delete(id)) {
            throw notFound();
        }
    }

    private void validateBusinessRule(ServicoInput input) {
        if (input.duracaoMinutos() > DURACAO_MAXIMA) {
            throw new HttpException(422, "A duração do serviço não pode ultrapassar 240 minutos");
        }
    }

    private HttpException notFound() {
        return new HttpException(404, "Serviço não encontrado");
    }
}