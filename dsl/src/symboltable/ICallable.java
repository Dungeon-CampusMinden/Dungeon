package symboltable;

import interpreter.DSLInterpreter;
import parser.AST.AstVisitor;

import java.util.List;

public interface ICallable {
    // TODO: refine signature
    //Object call(AstVisitor<Object> interperter, List<Object> parameters);
    Object call(DSLInterpreter interperter, List<Object> parameters);
}
