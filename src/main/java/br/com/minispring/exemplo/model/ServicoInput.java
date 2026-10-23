package br.com.minispring.exemplo.model;

import br.com.minispring.framework.validation.NotBlank;
import br.com.minispring.framework.validation.Positive;

public record ServicoInput(
        @NotBlank String nome,
        @Positive int duracaoMinutos
) {}