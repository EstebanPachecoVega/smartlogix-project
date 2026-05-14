package cl.smartlogix.pedidos.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}