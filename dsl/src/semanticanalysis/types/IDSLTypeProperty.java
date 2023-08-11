package semanticanalysis.types;

public interface IDSLTypeProperty<T,V> {
    void set(T instance, V valueToSet);
    V get(T instance);
    boolean isSettable();
    boolean isGettable();
}
