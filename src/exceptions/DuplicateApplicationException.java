package exceptions;

public class DuplicateApplicationException extends Exception {
    public DuplicateApplicationException(String message) {
        super(message);
    }
}
