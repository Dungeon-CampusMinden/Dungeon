package dsl.runtime.value;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.IInstanceCallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;
import java.util.*;

/** Implements a set value. */
public class SetValue extends Value {

  // stores the internal values of the Value-instances in order to ensure,
  // that only Value-instances with distinct internal values are stored
  private HashSet<Object> internalValueSet = new HashSet<>();

  /**
   * Constructor.
   *
   * @param dataType type of the set
   */
  public SetValue(SetType dataType) {
    super(dataType, new HashSet<Value>());
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public SetType getDataType() {
    return (SetType) this.dataType;
  }

  /**
   * WTF? .
   *
   * @return the internal HashSet of this {@link SetValue}.
   */
  public HashSet<Value> internalSet() {
    return ((HashSet<Value>) this.object);
  }

  /**
   * WTF? (erster Satz KURZ).
   *
   * <p>Add a Value to the set. The Value will only be added to the set, if no other Value with the
   * same internal value of the passed Value is already stored in this set.
   *
   * @param value the Value to store in the set
   * @return true, if the Value was added, false otherwise
   */
  public boolean addValue(Value value) {
    var internalValue = value.getInternalValue();
    if (internalValueSet.contains(internalValue)) {
      return false;
    }
    internalValueSet.add(internalValue);
    internalSet().add(value);
    return true;
  }

  /**
   * WTF? .
   *
   * @return all stored values
   */
  public Set<Value> getValues() {
    return internalSet();
  }

  /** WTF? . */
  public void clearSet() {
    internalValueSet.clear();
    internalSet().clear();
  }

  // region native_methods
  /**
   * Native method, which implements adding a Value to the internal {@link Set} of a {@link
   * SetValue}.
   */
  public static class AddMethod implements IInstanceCallable {

    /** WTF? . */
    public static SetValue.AddMethod instance = new SetValue.AddMethod();

    private AddMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      SetValue setValue = (SetValue) instance;
      Node paramNode = parameters.get(0);
      Value paramValue = (Value) paramNode.accept(interpreter);

      return setValue.addValue(paramValue);
    }
  }

  /**
   * Native method, which implements calculating the size (i.e. the number of stored elements of a
   * {@link SetValue}.
   */
  public static class SizeMethod implements IInstanceCallable {
    /** WTF? . */
    public static SetValue.SizeMethod instance = new SetValue.SizeMethod();

    private SizeMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      SetValue setValue = (SetValue) instance;

      return setValue.internalSet().size();
    }
  }

  /**
   * WTF? (erster Satz KURZ) .
   *
   * <p>Native method, which checks whether a given Value is present in the internal value set of a
   * {@link SetValue}. Because different instances of {@link Value} can refer to the same internal
   * value, the internal values are used for the lookup.
   */
  public static class ContainsMethod implements IInstanceCallable {

    /** WTF? . */
    public static SetValue.ContainsMethod instance = new SetValue.ContainsMethod();

    private ContainsMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      SetValue setValue = (SetValue) instance;

      Node valueToCheckNode = parameters.get(0);
      Value valueToCheck = (Value) valueToCheckNode.accept(interpreter);

      return setValue.internalValueSet.contains(valueToCheck.getInternalValue());
    }
  }
  // endregion

}
