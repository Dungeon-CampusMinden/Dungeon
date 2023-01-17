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
import runtime.GameEnvironment;
import runtime.MemorySpace;
import runtime.Value;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTableParser;
import semanticAnalysis.types.IType;

public class Helpers {

    private static DungeonDSLParser.ProgramContext getParseTreeFromCharStream(CharStream stream) {
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);

        return parser.program();
    }

    /**
     * Uses ANTLR-classes to create a parse tree from passed program string
     *
     * @param program the program to parse
     * @return the {@link DungeonDSLParser.ProgramContext} for the parsed program
     */
    public static DungeonDSLParser.ProgramContext getParseTree(String program) {
        var stream = CharStreams.fromString(program);
        return getParseTreeFromCharStream(stream);
    }

    /**
     * Converts a {@link DungeonDSLParser.ProgramContext} to a {@link parser.AST.Node}
     *
     * @param parseTree the parser tree to convert
     * @return the AST for the parse tree
     */
    public static parser.AST.Node convertToAST(DungeonDSLParser.ProgramContext parseTree) {
        DungeonASTConverter converter = new DungeonASTConverter();
        return converter.walk(parseTree);
    }

    /**
     * Generates the AST for a passed program string
     *
     * @param program the program to generate an AST for
     * @return the AST
     */
    public static parser.AST.Node getASTFromString(String program) {
        var parseTree = getParseTree(program);
        return convertToAST(parseTree);
    }

    /**
     * Load a resource file and generate an AST for it's contents
     *
     * @param fileResourceURL the URL of the resourceFile
     * @return the AST for the contents of the file
     * @throws URISyntaxException on invalid URI syntax
     * @throws IOException if the file does not exist
     */
    public static parser.AST.Node getASTFromResourceFile(URL fileResourceURL)
            throws URISyntaxException, IOException {
        var file = new File(fileResourceURL.toURI());
        var stream = CharStreams.fromFileName(file.getAbsolutePath());

        var parseTree = getParseTreeFromCharStream(stream);
        return convertToAST(parseTree);
    }

    /**
     * Performs semantic analysis for given AST and returns the {@link
     * semanticAnalysis.SymbolTableParser.Result} output from the SymbolTableParser
     *
     * @param ast the AST to create the symbol table for
     * @return the {@link semanticAnalysis.SymbolTableParser.Result} of the semantic analysis
     */
    public static SymbolTableParser.Result getSymtableForAST(parser.AST.Node ast) {
        SymbolTableParser symbolTableParser = new SymbolTableParser();
        symbolTableParser.setup(new GameEnvironment());
        return symbolTableParser.walk(ast);
    }

    /**
     * Performs semantic analysis for given AST with loaded types and returns the {@link
     * semanticAnalysis.SymbolTableParser.Result} output from the SymbolTableParser
     *
     * @param ast the AST to create the symbol table for
     * @param types the types to load into the environment before doing semantic analysis
     * @return the {@link semanticAnalysis.SymbolTableParser.Result} of the semantic analysis
     */
    public static SymbolTableParser.Result getSymtableForASTWithLoadedTypes(
            parser.AST.Node ast, IType[] types) {
        var symTableParser = new SymbolTableParser();
        var env = new GameEnvironment();
        env.loadTypes(types);
        symTableParser.setup(env);
        return symTableParser.walk(ast);
    }

    public static void bindDefaultValueInMemorySpace(Symbol symbol, MemorySpace ms) {
        var defaultValue = Value.getDefaultValue(symbol.getDataType());
        var value = new Value(symbol.getDataType(), defaultValue);
        ms.bindValue(symbol.getName(), value);
    }
}
