package helpers;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.DungeonASTConverter;

public class Helpers {
    public static DungeonDSLParser.ProgramContext getParseTree(String program) {
        var stream = CharStreams.fromString(program);
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);

        return parser.program();
    }

    public static parser.AST.Node convertToAST(DungeonDSLParser.ProgramContext parseTree) {
        DungeonASTConverter converter = new DungeonASTConverter();
        return converter.walk(parseTree);
    }

    public static parser.AST.Node getASTFromString(String program) {
        var parseTree = getParseTree(program);
        return convertToAST(parseTree);
    }
}
