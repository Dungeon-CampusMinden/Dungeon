package newdsl.common;


public class DSLError {
    private String message;
    private SourceLocation sourceLocation;

    public DSLError(String message, SourceLocation sourceLocation) {
        this.message = message;
        this.sourceLocation = sourceLocation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    @Override
    public String toString() {
        return String.format("ERROR %s: %s", sourceLocation, this.message);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.toString().equals(this.toString());
    }
}
