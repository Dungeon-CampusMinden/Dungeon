package savegame;

public class UnserializableFieldException extends Error {

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
