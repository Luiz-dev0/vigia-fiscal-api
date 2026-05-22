package br.com.vigiafiscal.api.application.dto;

public record TokenResponse(String token, String tipo, String nome, String email) {
    public static TokenResponse of(String token, String nome, String email) {
        return new TokenResponse(token, "Bearer", nome, email);
    }
}
