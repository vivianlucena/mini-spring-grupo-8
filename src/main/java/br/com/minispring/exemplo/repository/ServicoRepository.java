package br.com.minispring.exemplo.repository;

import br.com.minispring.exemplo.model.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository {
    Servico create(String nome, int duracaoMinutos);

    List<Servico> findAll();

    Optional<Servico> findById(long id);

    Optional<Servico> update(long id, String nome, int duracaoMinutos);

    boolean delete(long id);
}