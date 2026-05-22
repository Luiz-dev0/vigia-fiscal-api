package br.com.vigiafiscal.api.application.dto;

public record PlanoResponse(
        String nome,
        int preco,
        int precoOriginal,
        String desconto,
        int limiteCnpjs,
        String descricao,
        String priceId
) {}
