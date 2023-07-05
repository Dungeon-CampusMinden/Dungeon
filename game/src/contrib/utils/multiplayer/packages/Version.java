package contrib.utils.multiplayer.packages;

public class Version implements Comparable<Version> {

    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int compareTo(Version other) {
        // compare major versions
        int result = Integer.compare(this.major, other.major);
        if (result != 0) {
            return result;
        }

        // compare minor versions
        result = Integer.compare(this.minor, other.minor);
        if (result != 0) {
            return result;
        }

        // compare patch versions
        return Integer.compare(this.patch, other.patch);
    }

    public int major() { return this.major; }
    public int minor() { return this.minor; }
    public int patch() { return this.patch; }
}
