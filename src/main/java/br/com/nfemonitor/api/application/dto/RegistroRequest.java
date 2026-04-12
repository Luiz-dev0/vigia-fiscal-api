package br.com.nfemonitor.api.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(min = 2, max = 100)
        String nome,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        String senha,

        String whatsapp
) {}