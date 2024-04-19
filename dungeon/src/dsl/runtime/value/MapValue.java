package dsl.runtime.value;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.IInstanceCallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.MapType;
import java.util.*;

/** WTF? . */
public class MapValue extends Value {

  // stores the internal values of the Value-instances in order to ensure,
  // that only Value-instances with distinct internal values are stored
  private HashMap<Object, Object> internalObjectMap = new HashMap<>();

  /**
   * Constructor.
   *
   * @param dataType type of the set
   */
  public MapValue(MapType dataType) {
    super(dataType, new HashMap<Value, Value>());
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public MapType getDataType() {
    return (MapType) this.dataType;
  }

  /**
   * WTF? .
   *
   * @return the internal HashMap of this {@link SetValue}.
   */
  public HashMap<Value, Value> internalMap() {
    return ((HashMap<Value, Value>) this.object);
  }

  /**
   * WTF? (KURZER erster Satz!) .
   *
   * <p>Add a Value to the set. The Value will only be added to the set, if no other Value with the
   * same internal value of the passed Value is already stored in this set.
   *
   * @param key foo
   * @param entry the Value to store in the set
   * @return true, if the Value was added, false otherwise
   */
  public boolean addValue(Value key, Value entry) {
    var internalKeyValue = key.getInternalValue();
    var internalEntryValue = entry.getInternalValue();

    if (internalObjectMap.containsKey(internalKeyValue)) {
      // don't add value
      return false;
    }

    internalObjectMap.put(internalKeyValue, internalEntryValue);
    internalMap().put(key, entry);

    return true;
  }

  /**
   * WTF? .
   *
   * @param key foo
   * @return foo
   */
  public boolean removeKeyValue(Value key) {
    var internalKeyValue = key.getInternalValue();

    if (!internalObjectMap.containsKey(internalKeyValue)) {
      return false;
    }

    internalObjectMap.remove(internalKeyValue);
    internalMap().remove(key);
    return true;
  }

  /**
   * WTF? .
   *
   * @param key foo
   * @return foo
   */
  public Value getValue(Value key) {
    var value = internalMap().get(key);
    if (value == null) {
      return Value.NONE;
    } else {
      return value;
    }
  }

  /**
   * WTF? .
   *
   * @return all stored values
   */
  public Map<Value, Value> getValues() {
    return new HashMap<>(this.internalMap());
  }

  /** WTF? . */
  public void clearMap() {
    internalObjectMap.clear();
    internalMap().clear();
  }

  // region native_methods WTF?
  /**
   * Native method, which implements adding a Value to the internal {@link Map} of a {@link
   * MapValue}.
   */
  public static class AddMethod implements IInstanceCallable {

    /** WTF? . */
    public static MapValue.AddMethod instance = new MapValue.AddMethod();

    private AddMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      MapValue mapValue = (MapValue) instance;
      Node keyNode = parameters.get(0);
      Node elementNode = parameters.get(1);
      Value keyValue = (Value) keyNode.accept(interpreter);
      Value elementValue = (Value) elementNode.accept(interpreter);

      return mapValue.addValue(keyValue, elementValue);
    }
  }

  /** WTF? . */
  public static class GetKeysMethod implements IInstanceCallable {

    /** WTF? . */
    public static MapValue.GetKeysMethod instance = new MapValue.GetKeysMethod();

    private GetKeysMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      MapValue mapValue = (MapValue) instance;
      ArrayList<Value> keys = new ArrayList<>();
      keys.addAll(mapValue.internalMap().keySet());
      return keys;
    }
  }

  /** WTF? . */
  public static class GetElementsMethod implements IInstanceCallable {

    /** WTF? . */
    public static MapValue.GetElementsMethod instance = new MapValue.GetElementsMethod();

    private GetElementsMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      MapValue mapValue = (MapValue) instance;
      ArrayList<Value> values = new ArrayList<>();
      values.addAll(mapValue.internalMap().values());
      return values;
    }
  }
  // endregion

}
