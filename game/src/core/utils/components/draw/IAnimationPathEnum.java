package core.utils.components.draw;

/**
 * This interface needs to be implemented by all enums that store paths to animations.
 *
 * <p>The enums represent a path (as a string) where animations can be found. The path starts at a
 * specific subfolder (e.g., "character/hero"), so the enums have the value of the folders inside
 * this folder.
 *
 * <p>The {@link core.components.DrawComponent} will use the given path to load and set the
 * animation.
 *
 * <p>Systems can use the enum value to set the current animation at the {@link
 * core.components.DrawComponent}.
 *
 * <p>To add your own path, simply create a new enum and implement this interface.
 *
 * @see core.components.DrawComponent
 */
public interface IAnimationPathEnum {
    /**
     * Make sure that your enum values are strings so the {@link core.components.DrawComponent} can
     * use them to read in directories.
     *
     * <p>Normally, if your enums represent strings, you do not need to overwrite this method.
     *
     * @return The value as a string that can be used as a path
     */
    String toString();
}
