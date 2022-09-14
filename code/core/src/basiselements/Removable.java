package basiselements;

/**
 * An object that is not removable as default.
 *
 * <p>Must be implemented for all objects that should be controlled by the <code>AbstractController
 * </code>.
 */
public interface Removable {
    /**
     * @return <code>true</code>, if this instance can be deleted; <code>false</code> otherwise
     */
    default boolean removable() {
        return false;
    }
}
