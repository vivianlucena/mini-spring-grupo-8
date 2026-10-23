package br.com.minispring.exemplo.repository;

import br.com.minispring.exemplo.model.Servico;
import br.com.minispring.framework.annotation.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public final class InMemoryServicoRepository implements ServicoRepository {

    private final ConcurrentHashMap<Long, Servico> servicos = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Servico create(String nome, int duracaoMinutos) {
        long id = sequence.incrementAndGet();
        var servico = new Servico(id, nome, duracaoMinutos);
        servicos.put(id, servico);
        return servico;
    }

    @Override
    public List<Servico> findAll() {
        return servicos.values()
                .stream()
                .sorted(Comparator.comparingLong(Servico::id))
                .toList();
    }

    @Override
    public Optional<Servico> findById(long id) {
        return Optional.ofNullable(servicos.get(id));
    }

    @Override
    public Optional<Servico> update(long id, String nome, int duracaoMinutos) {
        return Optional.ofNullable(
                servicos.computeIfPresent(
                        id,
                        (key, old) -> new Servico(id, nome, duracaoMinutos)
                )
        );
    }

    @Override
    public boolean delete(long id) {
        return servicos.remove(id) != null;
    }
}