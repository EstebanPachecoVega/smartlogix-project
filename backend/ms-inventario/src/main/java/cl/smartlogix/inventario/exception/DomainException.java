package cl.smartlogix.inventario.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}