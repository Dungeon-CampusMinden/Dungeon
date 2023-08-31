package semanticanalysis;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import java.util.List;

public interface IInstanceCallable {
    Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters);
}
