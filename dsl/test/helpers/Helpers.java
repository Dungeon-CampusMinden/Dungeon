package helpers;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.DungeonASTConverter;
import symboltable.SymbolTableParser;

public class Helpers {

    private static DungeonDSLParser.ProgramContext getParseTreeFromCharStream(CharStream stream) {
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);

        return parser.program();
    }

    public static DungeonDSLParser.ProgramContext getParseTree(String program) {
        var stream = CharStreams.fromString(program);
        return getParseTreeFromCharStream(stream);
    }

    public static parser.AST.Node convertToAST(DungeonDSLParser.ProgramContext parseTree) {
        DungeonASTConverter converter = new DungeonASTConverter();
        return converter.walk(parseTree);
    }

    public static parser.AST.Node getASTFromString(String program) {
        var parseTree = getParseTree(program);
        return convertToAST(parseTree);
    }

    public static parser.AST.Node getASTFromResourceFile(URL fileResourceURL)
            throws URISyntaxException, IOException {
        var file = new File(fileResourceURL.toURI());
        var stream = CharStreams.fromFileName(file.getAbsolutePath());

        var parseTree = getParseTreeFromCharStream(stream);
        return convertToAST(parseTree);
    }

    public static SymbolTableParser.Result getSymtableForAST(parser.AST.Node ast) {
        SymbolTableParser symbolTableParser = new SymbolTableParser();
        return symbolTableParser.walk(ast);
    }
}
