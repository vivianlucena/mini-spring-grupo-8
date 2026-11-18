
package br.com.minispring.exemplo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ServicoInput(

        @NotBlank(message = "O nome do serviço é obrigatório")
        String nome,

        @Positive(message = "A duração deve ser maior que zero")
        int duracaoMinutos

) {}
