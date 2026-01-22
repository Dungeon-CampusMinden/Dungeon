package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * A dynamic value which has a type and can be supplied either by Operations or block arguments.
 */
@JsonPropertyOrder({"type"})
@JsonIdentityInfo(scope = Value.class, generator = ValueSequenceGenerator.class, property = "@id")
public non-sealed abstract class Value implements ITypeLike, Serializable {
  /**
   * The kind of value, to draw a distinction between op results and block arguments.
   */
  public enum Kind {
    /**
     * This Value is the result of an operation.
     */
    OpResult,
    /**
     * This value is a block argument.
     */
    BlockArgument
  }

  private final Type type;
  private final Kind kind;

  protected Value(Type type, Kind kind) {
    this.type = type;
    this.kind = kind;
  }

  public Type getType() {
    return type;
  }

  @JsonIgnore
  public Kind getKind() {
    return kind;
  }
}

/**
 * Generates unique IDs for Value instances. This is used by Jackson to serialize/deserialize
 * e.g. "%0" "%1" ...
 */
final class ValueSequenceGenerator extends ObjectIdGenerator<String> {
  @Serial
  private static final long serialVersionUID = 1L;

  private transient int _nextIndex;

  private final Class<?> _scope;

  public ValueSequenceGenerator() {
    this(Object.class, -1);
  }

  private ValueSequenceGenerator(Class<?> scope, int fv) {
    _scope = scope;
  }

  @Override
  public final Class<?> getScope() {
    return _scope;
  }

  @Override
  public boolean canUseFor(ObjectIdGenerator<?> gen) {
    return (gen.getClass() == getClass()) && (gen.getScope() == _scope);
  }

  private int initialValue() {
    return 1;
  }

  @Override
  public ObjectIdGenerator<String> forScope(Class<?> scope) {
    return (_scope == scope) ? this : new ValueSequenceGenerator(scope, _nextIndex);
  }

  @Override
  public ObjectIdGenerator<String> newForSerialization(Object context) {
    return new ValueSequenceGenerator(_scope, initialValue());
  }

  @Override
  public IdKey key(Object key) {
    if (key == null) {
      return null;
    }
    return new IdKey(getClass(), _scope, key);
  }

  @Override
  public String generateId(Object forPojo) {
    if (forPojo == null) {
      return null;
    }
    return "%" + _nextIndex++;
  }
}
