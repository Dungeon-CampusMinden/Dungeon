package dsl.runtime.callable;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/**
 * {@link ICallable} implementation for an {@link IDSLExtensionMethod}. It is used for binding
 * {@link IDSLExtensionMethod} implementations as symbols in a DSL data type, which is created from
 * a java class.
 */
public class ExtensionMethod extends Symbol implements ICallable {
  private final IDSLExtensionMethod<Object, Object> extensionMethod;

  /**
   * Constructor.
   *
   * @param name The name of the method
   * @param parentScope The parent scope of this symbol
   * @param functionType The {@link FunctionType} containing the signature information of the method
   * @param callable The {@link IDSLExtensionMethod}, which contains the actual implementation of
   *     the method's logic
   */
  public ExtensionMethod(
      String name,
      IScope parentScope,
      FunctionType functionType,
      IDSLExtensionMethod<Object, Object> callable) {
    super(name, parentScope, functionType);
    this.extensionMethod = callable;
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    // resolve "THIS_VALUE"
    Value instance = interperter.getCurrentInstanceMemorySpace().resolve(Value.THIS_NAME);
    Object instanceObject = instance.getInternalValue();

    // interpret parameters and extract internal values
    var parameterValues = interperter.evaluateNodes(parameters);
    List<Object> parameterObjects = interperter.translateValuesToObjects(parameterValues);

    return this.extensionMethod.call(instanceObject, parameterObjects);
  }

  @Override
  public ICallable.Type getCallableType() {
    return null;
  }

  @Override
  public FunctionType getFunctionType() {
    return (FunctionType) this.dataType;
  }
}
