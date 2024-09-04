package newdsl.interpreter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

class DSLTokenSource implements TokenSource {
    private final TokenSource original;
    private final String sourceName;

    public DSLTokenSource(TokenSource original, String sourceName) {
        this.original = original;
        this.sourceName = sourceName;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public Token nextToken() {
        return original.nextToken();
    }

    @Override
    public int getCharPositionInLine() {
        return original.getCharPositionInLine();
    }

    @Override
    public CharStream getInputStream() {
        return original.getInputStream();
    }

    @Override
    public int getLine() {
        return original.getLine();
    }

    @Override
    public void setTokenFactory(TokenFactory<?> factory) {
        original.setTokenFactory(factory);
    }

    @Override
    public TokenFactory<?> getTokenFactory() {
        return original.getTokenFactory();
    }
}
