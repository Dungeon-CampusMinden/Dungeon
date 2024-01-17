package dsl.runtime.value;

import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Field;

public class EncapsulatedField extends Value {
  private final Field field;

  public EncapsulatedField(IType type, Field field, Object object) {
    super(type, object, true);
    this.field = field;
  }

  @Override
  public boolean setInternalValue(Object internalValue) {
    try {
      field.set(this.object, internalValue);
      return true;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object getInternalValue() {
    try {
      return field.get(this.object);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Value value)) {
      return false;
    }
    var myInternalObject = this.getInternalValue();
    if (myInternalObject instanceof Value myInternalValue) {
      return value.equals(myInternalValue);
    } else {
      var otherInternalObject = value.getInternalValue();
      return myInternalObject.equals(otherInternalObject);
    }
  }
}
