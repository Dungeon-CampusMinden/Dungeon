package core.serialization;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import core.ir.Value;

import java.io.Serial;

public class BlockIdGenerator extends ObjectIdGenerator<String> {
  @Serial
  private static final long serialVersionUID = 1L;

  private static int nextId = 0;

  @Override
  public Class<?> getScope() {
    return Value.class;
  }

  @Override
  public ObjectIdGenerator<String> forScope(Class<?> scope) {
    return this;
  }

  @Override
  public ObjectIdGenerator<String> newForSerialization(Object context) {
    nextId = 0;
    return this;
  }

  @Override
  public ObjectIdGenerator.IdKey key(Object key) {
    if (key == null) {
      return null;
    }
    return new ObjectIdGenerator.IdKey(getClass(), null, key);
  }

  @Override
  public boolean canUseFor(ObjectIdGenerator<?> gen) {
    return (gen.getClass() == getClass()) && (gen.getScope() == getScope());
  }

  @Override
  public String generateId(Object forPojo) {
    return ".blk_" + nextId++;
  }
}
