package dsl.runtime.value;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.IInstanceCallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import java.util.ArrayList;
import java.util.List;

/** Implements a list value. */
public class ListValue extends Value {
  /**
   * Constructor
   *
   * @param dataType The type of the list
   */
  public ListValue(ListType dataType) {
    super(dataType, new ArrayList<Value>());
  }

  public ListType getDataType() {
    return (ListType) this.dataType;
  }

  /**
   * @return the internal ArrayList of this {@link ListValue}.
   */
  public ArrayList<Value> internalList() {
    return (ArrayList<Value>) this.object;
  }

  /**
   * Add a Value to the list
   *
   * @param value the value to add
   */
  public void addValue(Value value) {
    internalList().add(value);
  }

  /**
   * Get a value by index
   *
   * @param index the index
   * @return the Value at specified index
   */
  public Value getValue(int index) {
    return internalList().get(index);
  }

  /**
   * Return all stored Values
   *
   * @return the stored Values
   */
  public List<Value> getValues() {
    return internalList();
  }

  public void clearList() {
    internalList().clear();
  }

  @Override
  public Object clone() {
    var cloneValue = new ListValue(this.getDataType());
    cloneValue.object = this.object;
    return cloneValue;
  }

  @Override
  public boolean setFrom(Value other) {
    if (!(other instanceof ListValue otherListValue)) {
      throw new RuntimeException("Other value is not a list value!");
    }

    return super.setFrom(otherListValue);
  }

  // region native_methods

  /**
   * Native method, which implements adding a Value to the internal {@link List} of a {@link
   * ListValue}.
   */
  public static class AddMethod implements IInstanceCallable {

    public static AddMethod instance = new AddMethod();

    private AddMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      ListValue listValue = (ListValue) instance;
      Node paramNode = parameters.get(0);
      Value paramValue = (Value) paramNode.accept(interpreter);

      listValue.internalList().add(paramValue);
      return null;
    }
  }

  /**
   * Native method, which implements calculating the size (i.e. the number of stored elements of a
   * {@link ListValue}.
   */
  public static class SizeMethod implements IInstanceCallable {

    public static SizeMethod instance = new SizeMethod();

    private SizeMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      ListValue listValue = (ListValue) instance;

      return listValue.internalList().size();
    }
  }

  /**
   * Native method, which implements the access to one element of a {@link ListValue} by index. If
   * the index is out of range of the internal {@link List} of the {@link ListValue}, {@link
   * Value#NONE} is returned.
   */
  public static class GetMethod implements IInstanceCallable {

    public static GetMethod instance = new GetMethod();

    private GetMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      ListValue listValue = (ListValue) instance;

      Node indexParameterNode = parameters.get(0);
      Value indexValue = (Value) indexParameterNode.accept(interpreter);
      int index = (int) indexValue.getInternalValue();

      if (index >= listValue.internalList().size()) {
        return Value.NONE;
      } else {
        return listValue.internalList().get(index);
      }
    }
  }

  public static class ClearMethod implements IInstanceCallable {

    public static ClearMethod instance = new ClearMethod();

    private ClearMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      ListValue listValue = (ListValue) instance;
      listValue.internalList().clear();
      return null;
    }
  }
  // endregion
}
