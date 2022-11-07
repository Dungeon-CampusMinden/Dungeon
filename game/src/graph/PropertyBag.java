package graph;

import java.util.HashMap;

public class PropertyBag {
    private HashMap<String, Property> properties;

    public PropertyBag() {
        properties = new HashMap<>();
    }

    public boolean addAttribute(String name, Property attr) {
        if (properties.containsKey(name)) {
            return false;
        }
        properties.put(name, attr);
        return true;
    }

    public Property getAttribute(String name) {
        return properties.getOrDefault(name, Property.NONE);
    }
}
