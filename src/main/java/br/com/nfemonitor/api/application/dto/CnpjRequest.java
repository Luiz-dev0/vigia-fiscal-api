package br.com.nfemonitor.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CnpjRequest(
        @NotBlank
        @Pattern(regexp = "\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
                message = "CNPJ inválido")
        String cnpj,

        @NotBlank @Size(max = 200)
        String razaoSocial,

        String nomeFantasia,
        String ie,

        @NotBlank @Size(min = 2, max = 2)
        String uf,

        String emailContato
) {}