package helpers;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;

import interpreter.DSLInterpreter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import parser.DungeonASTConverter;

import runtime.GameEnvironment;
import runtime.MemorySpace;
import runtime.Value;

import semanticanalysis.ScopedSymbol;
import semanticanalysis.SemanticAnalyzer;
import semanticanalysis.Symbol;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
     * Converts a {@link DungeonDSLParser.ProgramContext} to a {@link parser.ast.Node}
     *
     * @param parseTree the parser tree to convert
     * @return the AST for the parse tree
     */
    public static parser.ast.Node convertToAST(DungeonDSLParser.ProgramContext parseTree) {
        DungeonASTConverter converter = new DungeonASTConverter();
        return converter.walk(parseTree);
    }

    /**
     * Generates the AST for a passed program string
     *
     * @param program the program to generate an AST for
     * @return the AST
     */
    public static parser.ast.Node getASTFromString(String program) {
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
    public static parser.ast.Node getASTFromResourceFile(URL fileResourceURL)
            throws URISyntaxException, IOException {
        var file = new File(fileResourceURL.toURI());
        var stream = CharStreams.fromFileName(file.getAbsolutePath());

        var parseTree = getParseTreeFromCharStream(stream);
        return convertToAST(parseTree);
    }

    /**
     * Performs semantic analysis for given AST and returns the {@link SemanticAnalyzer.Result}
     * output from the SymbolTableParser
     *
     * @param ast the AST to create the symbol table for
     * @return the {@link SemanticAnalyzer.Result} of the semantic analysis
     */
    public static SemanticAnalyzer.Result getSymtableForAST(parser.ast.Node ast) {
        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(new GameEnvironment());
        return symbolTableParser.walk(ast);
    }

    public static void bindDefaultValueInMemorySpace(
            Symbol symbol, MemorySpace ms, DSLInterpreter interpreter) {
        var defaultValue = interpreter.createDefaultValue(symbol.getDataType());
        ms.bindValue(symbol.getName(), defaultValue);
    }

    /**
     * @param program String representation of DSL program to generate the quest config for
     * @param environment GameEnvironment to use for loading types and semantic analysis
     * @param interpreter DSLInterpreter to use to generate the quest config
     * @param classesToLoadAsTypes List of all classes marked with @DSLType to load as types into
     *     the environment
     * @return the generated quest config
     */
    public static Object generateQuestConfigWithCustomTypes(
            String program,
            GameEnvironment environment,
            DSLInterpreter interpreter,
            Class<?>... classesToLoadAsTypes) {

        for (var clazz : classesToLoadAsTypes) {
            var type =
                    environment
                            .getTypeBuilder()
                            .createDSLTypeForJavaTypeInScope(environment.getGlobalScope(), clazz);
            environment.loadTypes(type);
        }

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(environment);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        interpreter.initializeRuntime(environment);
        Value questConfigValue = (Value) interpreter.generateQuestConfig(ast);
        return questConfigValue.getInternalValue();
    }

    /**
     * @param program String representation of DSL program to generate the quest config for
     * @param environment GameEnvironment to use for loading functions and semantic analysis
     * @param interpreter DSLInterpreter to use to generate the quest config
     * @param functions List of all functions to load into the environment
     * @return the generated quest config
     */
    public static Object generateQuestConfigWithCustomFunctions(
            String program,
            GameEnvironment environment,
            DSLInterpreter interpreter,
            ScopedSymbol... functions) {

        environment.loadFunctions(functions);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(environment);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        interpreter.initializeRuntime(environment);

        return interpreter.generateQuestConfig(ast);
    }
}
