package contrib.utils.multiplayer.packages;

import static java.util.Objects.requireNonNull;

/** Used to handle different versions of client and server. */
public class Version implements Comparable<Version> {

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Create new version instance.
     *
     * @param major Major number of the version.
     * @param minor Minor number of the version.
     * @param patch Patch number of the version.
     */
    public Version(final int major, final int minor, final int patch) {

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int compareTo(final Version other) {
        requireNonNull(other);

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

    /**
     * @return Major number of the version.
     */
    public int major() {
        return this.major;
    }

    /**
     * @return Minor number of the version.
     */
    public int minor() {
        return this.minor;
    }

    /**
     * @return Patch number of the version.
     */
    public int patch() {
        return this.patch;
    }
}
