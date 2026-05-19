package br.com.vigiafiscal.api.infrastructure.sefaz;

public class SefazException extends RuntimeException {

    public SefazException(String message) {
        super(message);
    }

    public SefazException(String message, Throwable cause) {
        super(message, cause);
    }
}
