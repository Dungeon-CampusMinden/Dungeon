package interpreter;

import antlr.main.*;
import org.antlr.v4.runtime.*;
import parser.DungeonASTConverter;

public class DSLInterpreter {

    public dslToGame.QuestConfig getQuestConfig(String configSkript) {
        var stream = CharStreams.fromString(configSkript);
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);
        var programParseTree = parser.program();

        DungeonASTConverter astConverter = new DungeonASTConverter();
        var programAST = astConverter.walk(programParseTree);

        var dotInterpreter = new interpreter.dot.Interpreter();
        var graphs = dotInterpreter.getGraphs(programAST);

        return new dslToGame.QuestConfig(graphs.get(0), null, 0);
    }
}
