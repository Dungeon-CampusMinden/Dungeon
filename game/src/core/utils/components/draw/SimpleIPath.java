package core.utils.components.draw;

public class SimpleIPath implements IPath {
    private final String path;
    public SimpleIPath(String path) {
        this.path = path;
    }

    @Override
    public String pathString() {
        return path;
    }

    @Override
    public int priority() {
        return 0;
    }
}
