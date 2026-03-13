package blockly.dgir.compiler.java.transformations;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Rewrites logical binary expressions (&& and ||) into explicit control flow using if statements.
 * This is done to simplify later transformations and code generation, as it eliminates
 * short-circuiting behavior and makes the control flow more explicit.
 *
 * <p>It is necessary to perform this step so that logical chains like null checks (e.g. a != null
 * && a.method()) can be correctly transformed into short-circuiting control flow in the generated
 * code, rather than evaluating all operands unconditionally.
 */
public class LogicalBinaryToScf extends VoidVisitorAdapter<Void> {}
