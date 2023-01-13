package runtime;

import java.util.Map;
import java.util.Set;

public interface IMemorySpace {
    boolean bindValue(String name, Value value);

    Value resolve(String name);

    Value resolve(String name, boolean resolveInParent);

    boolean setValue(String name, Value value);

    Set<Map.Entry<String, Value>> getValueSet();
}
