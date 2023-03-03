package dslToGame.graph;

public record Property<T>(T value) {
    public static Property<Void> NONE = new Property<>(null);
}
