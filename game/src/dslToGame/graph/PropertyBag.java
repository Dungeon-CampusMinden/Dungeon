package dslToGame.graph;

import java.util.HashMap;

public class PropertyBag {
    private HashMap<String, Property> properties;

    /** Constructor */
    public PropertyBag() {
        properties = new HashMap<>();
    }

    /**
     * Adds an {@link Property} to this PropertyBag
     *
     * @param name the name of the new property
     * @param attr the Property to store associated with the name
     * @return false, if a property with the same name is already stored in this PropertyBag, true,
     *     if adding succeeded
     */
    public boolean addProperty(String name, Property attr) {
        if (properties.containsKey(name)) {
            return false;
        }
        properties.put(name, attr);
        return true;
    }

    /**
     * @param name the name of the {@link Property} to get from this PropertyBag
     * @return the {@link Property} associated with the name or `Property.NONE`, if no Property with
     *     the name is stored in this PropertyBag
     */
    public Property getAttribute(String name) {
        return properties.getOrDefault(name, Property.NONE);
    }
}
