package dsl.semanticanalysis.groum;

public interface GroumVisitor<T> {
  default T visit(GroumNode node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ActionNode node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ControlNode node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ConstRefAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(DefinitionAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(DefinitionByImportAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ExpressionAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(FunctionCallAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(MethodAccessAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ParameterInstantiationAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(PassAsParameterAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(PropertyAccessAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(ReferenceInGraphAction node) {
    throw new UnsupportedOperationException();
  }

  default T visit(VariableReferenceAction node) {
    throw new UnsupportedOperationException();
  }
}
