package savegame;

public class UnserializableFieldException extends Exception {

    public UnserializableFieldException() {
        super();
    }

    public UnserializableFieldException(String message) {
        super(message);
    }

    public UnserializableFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
