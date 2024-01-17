package dsl.runtime.value;

import dsl.runtime.memoryspace.EncapsulatedObject;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.memoryspace.MemorySpace;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.Map;
import java.util.Set;

public class AggregateValue extends Value {

  /**
   * @return {@link IMemorySpace} holding the values of this AggregateValue
   */
  @Override
  public IMemorySpace getMemorySpace() {
    return memorySpace;
  }

  private IMemorySpace parentMemorySpace;

  /**
   * Constructor
   *
   * @param datatype {@link IType} representing the datatype of the new AggregateValue
   * @param parentSpace the {@link IMemorySpace} in which the new AggregateValue was defined
   */
  public AggregateValue(IType datatype, IMemorySpace parentSpace) {
    super(datatype, null);
    initializeMemorySpace(parentSpace);
  }

  /**
   * Constructor
   *
   * @param datatype {@link IType} representing the datatype of the new AggregateValue
   * @param parentSpace the {@link IMemorySpace} in which the new AggregateValue was defined
   * @param internalValue an Object representing an internal value of the new AggregateValue
   */
  public AggregateValue(IType datatype, IMemorySpace parentSpace, Object internalValue) {
    super(datatype, internalValue);
    initializeMemorySpace(parentSpace);
  }

  public static AggregateValue fromEncapsulatedObject(
      IMemorySpace parentMemorySpace, EncapsulatedObject encapsulatedObject) {
    var val =
        new AggregateValue(
            encapsulatedObject.dataType, parentMemorySpace, encapsulatedObject.object);
    val.setMemorySpace(encapsulatedObject);
    return val;
  }

  private void initializeMemorySpace(IMemorySpace parentSpace) {
    this.memorySpace = new MemorySpace(parentSpace);
    this.memorySpace.bindValue(THIS_NAME, this);
    this.parentMemorySpace = parentSpace;
  }

  /**
   * @param ms the {@link IMemorySpace} to set as the memory space of this AggregateValue
   */
  public void setMemorySpace(IMemorySpace ms) {
    ms.delete(THIS_NAME);
    ms.bindValue(THIS_NAME, this);
    this.memorySpace = ms;
  }

  /**
   * @return set of entries of values in the AggregateValues {@link IMemorySpace} (combination of
   *     value-name and {@link Value})
   */
  public Set<Map.Entry<String, Value>> getValueSet() {
    return this.getMemorySpace().getValueSet();
  }

  @Override
  public Object clone() {
    var cloneValue =
        new AggregateValue(this.dataType, this.parentMemorySpace, this.getInternalValue());
    cloneValue.dirty = this.dirty;
    cloneValue.setMemorySpace(this.getMemorySpace());
    return cloneValue;
  }

  /**
   * Is this {@link AggregateValue} empty, e.g. is the internal value null and has it no other
   * member-Values than the {@link Value#THIS_NAME}-Value
   *
   * @return true, if this {@link AggregateValue} is empty, false otherwise
   */
  public boolean isEmpty() {
    boolean internalValueNull = this.getInternalValue() == null;

    var valueSet = this.memorySpace.getValueSet();

    // has this AggregateValue either no values or no values other than the "$THIS$"-value
    long dataMemberCount =
        valueSet.stream()
            .filter(
                v ->
                    !v.getKey().equals(Value.THIS_NAME) && !(v.getValue() instanceof FunctionValue))
            .count();

    return internalValueNull && dataMemberCount == 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AggregateValue aggregateValue)) {
      return false;
    }
    // compare based on addresses -> are we comparing to the same object?
    if (this == aggregateValue) {
      return true;
    }
    if (!this.dataType.equals(aggregateValue.dataType)) {
      return false;
    }
    if (this.getInternalValue() != null && aggregateValue.getInternalValue() != null) {
      // compare internal values, if they are not null
      return this.getInternalValue().equals(aggregateValue.getInternalValue());
    }

    // compare count of values in memory space
    long myMsSizeWithoutThis =
        this.getMemorySpace().getValueSet().stream()
            .filter(e -> !e.getKey().equals(Value.THIS_NAME))
            .count();
    long otherMsSizeWithoutThis =
        aggregateValue.getMemorySpace().getValueSet().stream()
            .filter(e -> !e.getKey().equals(Value.THIS_NAME))
            .count();
    if (myMsSizeWithoutThis != otherMsSizeWithoutThis) {
      return false;
    }

    // compare values in memoryspace
    var otherMs = aggregateValue;
    boolean equalMemorySpaces = true;
    for (var entry : this.getValueSet()) {
      String name = entry.getKey();
      Value value = entry.getValue();
      var valueInOtherMs = aggregateValue.getMemorySpace().resolve(name);
      if (valueInOtherMs.equals(NONE)) {
        equalMemorySpaces = false;
        break;
      }
      equalMemorySpaces &= value.equals(valueInOtherMs);
    }
    return equalMemorySpaces;
  }

  @Override
  public boolean setFrom(Value other) {
    if (!(other instanceof AggregateValue otherAggregateValue)) {
      throw new RuntimeException("Other value is not an aggregate Value!");
    }

    AggregateType myType = (AggregateType) this.getDataType();
    AggregateType otherType = (AggregateType) otherAggregateValue.getDataType();

    boolean typesAreIncompatible = false;
    if (!myType.equals(otherType)) {
      if (!myType.getOriginType().isAssignableFrom(otherType.getOriginType())) {
        typesAreIncompatible = true;
      }
    }

    if (typesAreIncompatible) {
      throw new RuntimeException("Incompatible data types, can't assign value!");
    }

    boolean didSetValue = this.setInternalValue(other.getInternalValue());
    if (didSetValue) {
      this.parentMemorySpace = otherAggregateValue.parentMemorySpace;
      this.memorySpace = otherAggregateValue.memorySpace;
    }
    return didSetValue;
  }
}
