package semanticanalysis;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import java.util.List;

/**
 * Simple interface for methods, which require an instance for context.
 * This interface is used for built-in native methods only, for external methods,
 * use {@link semanticanalysis.types.IDSLExtensionMethod}!
 */
public interface IInstanceCallable {
    Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters);
}
