package br.com.nfemonitor.api.application.dto;

public record PlanoResponse(
        String nome,
        int preco,
        int limiteCnpjs,
        String descricao,
        String priceId
) {}