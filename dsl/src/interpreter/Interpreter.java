package interpreter;

import antlr.main.*;
import org.antlr.v4.runtime.*;

public class Interpreter {

    /**
     * minimal ANTLR setup to parse a progam
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String hello = "hello world";
        var stream = CharStreams.fromString(hello);
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);
        var program = parser.program();

        if (program.children.size() != 2) {
            throw new Exception("Other children count than expected");
        }
    }
}
