package graph;

public class Property<T> {
    /*enum AttributeType {
        STRING,
        INTEGER,
        FLOAT
    }*/

    public static Property NONE = new Property<Void>(null);

    private final T value;

    public T getValue() {
        return this.value;
    }

    public Property(T value) {
        this.value = value;
    }
}
