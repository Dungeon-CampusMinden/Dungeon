public class ErrorInfo {
    int line;
    int charPositionInLine;
    String message;

    public ErrorInfo(int line, int charPositionInLine, String message) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error at line " + line + ", position " + charPositionInLine + ": " + message;
    }
}
