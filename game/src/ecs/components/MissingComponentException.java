package ecs.components;

public class MissingComponentException extends NullPointerException {

    public MissingComponentException(String message) {
        super("Missing Component:" + message);
    }
}
