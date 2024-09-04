package newdsl.interpreter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

public class DSLTokenFactory extends CommonTokenFactory {
    private final String sourceName;

    public DSLTokenFactory(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public CommonToken create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start, int stop, int line, int charPositionInLine) {
        Pair<TokenSource, CharStream> newSource = new Pair<>(new DSLTokenSource(source.a, sourceName), source.b);
        return super.create(newSource, type, text, channel, start, stop, line, charPositionInLine);
    }
}

