package br.com.nfemonitor.api.domain.certificado;

public class CertificadoException extends RuntimeException {

    public CertificadoException(String message) {
        super(message);
    }

    public static class SenhaInvalidaException extends CertificadoException {
        public SenhaInvalidaException() {
            super("Senha do certificado inválida.");
        }
    }

    public static class CertificadoVencidoException extends CertificadoException {
        public CertificadoVencidoException() {
            super("O certificado informado está vencido.");
        }
    }

    public static class CertificadoNaoEncontradoException extends CertificadoException {
        public CertificadoNaoEncontradoException(java.util.UUID cnpjId) {
            super("Nenhum certificado encontrado para o CNPJ: " + cnpjId);
        }
    }
}