package interpreter;

import antlr.main.*;
import org.antlr.v4.runtime.*;
import parser.DungeonASTConverter;

public class DSLInterpreter {

    public dslToGame.QuestConfig getQuestConfig(String configScript) {
        var stream = CharStreams.fromString(configScript);
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);
        var programParseTree = parser.program();

        DungeonASTConverter astConverter = new DungeonASTConverter();
        var programAST = astConverter.walk(programParseTree);

        var dotInterpreter = new interpreter.dot.Interpreter();
        var graphs = dotInterpreter.getGraphs(programAST);

        // using the first graph for level generation is only a temporary solution
        // other problems:
        // - no consistent execution model for interpreter -> graph is somehow generated ahead of time, currently not
        //   accessible as an object in the dsl...
        // - interaction between interpreter and game during runtime is not specified (e.g. how should object-scripting
        //   be implemented?)
        return new dslToGame.QuestConfig(graphs.get(0), null, 0);
    }
}
