package dsl.runtime.value;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.IInstanceCallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.MapType;
import java.util.*;

public class MapValue extends Value {

  // stores the internal values of the Value-instances in order to ensure,
  // that only Value-instances with distinct internal values are stored
  private HashMap<Object, Value> internalObjectMap = new HashMap<>();

  /**
   * Constructor
   *
   * @param dataType type of the set
   */
  public MapValue(MapType dataType) {
    super(dataType, new HashMap<Value, Value>());
  }

  public MapType getDataType() {
    return (MapType) this.dataType;
  }

  /**
   * @return the internal HashMap of this {@link SetValue}.
   */
  public HashMap<Value, Value> internalMap() {
    return ((HashMap<Value, Value>) this.object);
  }

  /**
   * Add a Value to the set. The Value will only be added to the set, if no other Value with the
   * same internal value of the passed Value is already stored in this set.
   *
   * @param entry the Value to store in the set
   * @return true, if the Value was added, false otherwise
   */
  public boolean addValue(Value key, Value entry) {
    var internalKeyValue = key.getInternalValue();

    if (internalObjectMap.containsKey(internalKeyValue)) {
      // don't add value
      return false;
    }

    internalObjectMap.put(internalKeyValue, entry);
    internalMap().put(key, entry);

    return true;
  }

  public boolean removeKeyValue(Value key) {
    var internalKeyValue = key.getInternalValue();

    if (!internalObjectMap.containsKey(internalKeyValue)) {
      return false;
    }

    internalObjectMap.remove(internalKeyValue);
    internalMap().remove(key);
    return true;
  }

  public Value getValue(Value key) {
    var value = internalMap().get(key);
    if (value == null) {
      return Value.NONE;
    } else {
      return value;
    }
  }

  /**
   * @return all stored values
   */
  public Map<Value, Value> getValues() {
    return new HashMap<>(this.internalMap());
  }

  public void clearMap() {
    internalObjectMap.clear();
    internalMap().clear();
  }

  @Override
  public boolean setFrom(Value other) {
    if (!(other instanceof MapValue otherMapValue)) {
      throw new RuntimeException("Other value is not a set value!");
    }

    boolean didSetValue = super.setFrom(other);
    if (didSetValue) {
      this.internalObjectMap = otherMapValue.internalObjectMap;
    }
    return didSetValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MapValue otherMapValue)) {
      return false;
    }

    var valueEquals = super.equals(obj);
    if (!valueEquals) {
      var allEntriesMatch = false;

      var myInternalObjectMap = this.internalObjectMap;
      var otherInternalObjectMap = otherMapValue.internalObjectMap;

      if (myInternalObjectMap.size() == otherInternalObjectMap.size()) {
        // check each key and value mapping
        for (var key : myInternalObjectMap.keySet()) {
          var myValue = myInternalObjectMap.get(key);
          var otherValue = otherInternalObjectMap.getOrDefault(key, Value.NONE);
          allEntriesMatch = myValue.equals(otherValue);
          if (!allEntriesMatch) {
            break;
          }
        }
      }
      return allEntriesMatch;
    }
    return true;
  }

  // region native_methods
  /**
   * Native method, which implements adding a Value to the internal {@link Map} of a {@link
   * MapValue}.
   */
  public static class AddMethod implements IInstanceCallable {

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

  public static class GetKeysMethod implements IInstanceCallable {

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

  public static class GetElementsMethod implements IInstanceCallable {

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

  public static class ClearMethod implements IInstanceCallable {

    public static MapValue.ClearMethod instance = new MapValue.ClearMethod();

    private ClearMethod() {}

    @Override
    public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
      MapValue mapValue = (MapValue) instance;
      mapValue.internalObjectMap.clear();
      mapValue.internalMap().clear();
      return null;
    }
  }
  // endregion

}
