package core.components;

import contrib.utils.components.draw.AdditionalAnimations;

import core.Component;
import core.Entity;
import core.systems.VelocitySystem;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.draw.IPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Store all {@link Animation}s for an entity.
 *
 * <p>At creation, the component will read in each subdirectory in the given path and create an
 * animation for each subdirectory.
 *
 * <p>Each Animation will be created with default settings. If you want to change these settings,
 * use the methods from {@link Animation}.
 *
 * <p>Use {@link #currentAnimation} to set the current animation.
 *
 * <p>Use {@link #currentAnimation} to get the current active animation or use {@link #getAnimation}
 * to get a specific animation.
 *
 * <p>Use {@link #hasAnimation} to check if the component has the desired animation.
 *
 * <p>If you want to add your own Animations, create a subdirectory for the animation and add the
 * path to an enum that implements the {@link IPath} interface. So if you want to add a jump
 * animation to the hero, just create a new directory "jump" in the assert-directory of your hero
 * (for example character/hero) and then add a new Enum-Value JUMP("jump") to {@link
 * AdditionalAnimations}
 *
 * <p>Animations will be searched in the default asset directory. Normally this is "game/assets",
 * but you can change it in the gradle.build file, if you like.
 *
 * <p>Note: each entity needs at least a {@link CoreAnimations#IDLE_LEFT} and {@link
 * CoreAnimations#IDLE_RIGHT} Animation.
 *
 * @see Animation
 * @see IPath
 */
public final class DrawComponent extends Component {
    private final Map<String, Animation> animationMap;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Animation currentAnimation;

    /**
     * Create a new DrawComponent and add it to the associated entity.
     *
     * <p>Will read in all subdirectories of the given path and use each file in the subdirectory to
     * create an animation. So each subdirectory should contain only the files for one animation.
     *
     * <p>Will set the current animation to either idle down, idle left, idle right, idle up or
     * idle, depending on which one of these animations exist.
     *
     * <p>If no animations for any idle-state exist, {@link Animation#defaultAnimation()} for "IDLE"
     * is set.
     *
     * @param entity associated entity
     * @param path Path (as a string) to the directory in the assets folder where the subdirectories
     *     containing the animation files are stored. Example: "character/knight".
     * @throws IOException if the given path does not exist
     * @see Animation
     */
    public DrawComponent(final Entity entity, final String path) throws IOException {
        super(entity);
        // fetch available animations
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File directory = new File(classLoader.getResource(path).getFile());
            animationMap =
                    Arrays.stream(directory.listFiles())
                            .filter(File::isDirectory)
                            .collect(Collectors.toMap(File::getName, Animation::of));

            currentAnimation(
                    CoreAnimations.IDLE_DOWN,
                    CoreAnimations.IDLE_LEFT,
                    CoreAnimations.IDLE_RIGHT,
                    CoreAnimations.IDLE_UP,
                    CoreAnimations.IDLE);

            // if no idle animation exists, set the missing texture animation as idle
            if (currentAnimation == null) {
                animationMap.put(CoreAnimations.IDLE.pathString(), Animation.defaultAnimation());
                currentAnimation(CoreAnimations.IDLE);
            }
        } catch (NullPointerException np) {
            // The component gets registered at the entity in super().
            // This means that if the files can't be loaded, the component is considered "defective"
            // and will likely throw exceptions.
            // For that reason, we remove the defective component from the entity.
            entity.removeComponent(DrawComponent.class);
            // We convert the "NullPointerException" to a "FileNotFoundException" because the only
            // reason for a NullPointerException is if the directory does not exist.
            throw new FileNotFoundException(
                    "Path "
                            + path
                            + " not found. DrawComponent was removed from Entity: "
                            + entity);
        }
    }

    /**
     * Create a new DrawComponent with a specific animation.
     *
     * <p>The given animation will be used as idle-left and idle-right animation
     *
     * <p>This constructor is for special case only. Use {@link DrawComponent(Entity, String)} if
     * possible.
     *
     * @param entity associated entity
     * @param idle Animation to use as idle-left and idle-right animation.
     */
    public DrawComponent(final Entity entity, final Animation idle) {
        super(entity);
        animationMap = new HashMap<>();
        animationMap.put(CoreAnimations.IDLE_LEFT.pathString(), idle);
        animationMap.put(CoreAnimations.IDLE_RIGHT.pathString(), idle);
        currentAnimation = idle;
    }

    /**
     * Get the current animation being displayed on the entity.
     *
     * @return the current animation of the entity
     */
    public Animation currentAnimation() {
        return currentAnimation;
    }

    /**
     * Set the current animation displayed on the entity.
     *
     * <p>If the animation passed is not displayed on the entity, there may be another point in the
     * code where the animation is overwritten on the same tick (e.g., in {@link VelocitySystem}).
     *
     * <p>If the given animation is not stored in this component, a warning is logged.
     *
     * @param animationName Path of the new current animation (this is the name of the directory).
     *     If more than one path will be given, the first one that exists will be set as the new
     *     current animation.
     * @see IPath
     */
    public void currentAnimation(final IPath... animationName) {
        for (IPath animationPath : animationName) {
            Animation animation = animationMap.get(animationPath.pathString());
            if (animation != null) {
                currentAnimation = animation;
                return;
            } else
                LOGGER.warning(
                        "Animation "
                                + animationPath
                                + " can not be set, because the given Animation could not be found for "
                                + entity.toString());
        }
    }

    /**
     * Get the Animation at the given path.
     *
     * <p>Can be null if the component does not store an animation with this path.
     *
     * @param path Path of the Animation
     * @return The animation or null
     */
    public Optional<Animation> getAnimation(final IPath path) {
        return Optional.ofNullable(animationMap.get(path.pathString()));
    }

    /**
     * Check if the component stores an animation with the given path.
     *
     * @param path Path of the animation to look for
     * @return true if the animation exists in this component, false if not
     */
    public boolean hasAnimation(final IPath path) {
        return animationMap.containsKey(path.pathString());
    }

    /**
     * Check if the animation at the given path is the current animation
     *
     * <p>Will log a warning if no animation is stored for the given path.
     *
     * @param path Path to the animation to check
     * @return true if the current animation equals the animation at the given path, false if not or
     *     no animation for the given oath is stored in this component.
     */
    public boolean isCurrentAnimation(final IPath path) {
        Optional<Animation> animation = getAnimation(path);
        if (animation.isPresent()) return animation.get() == currentAnimation;
        LOGGER.warning("Animation " + path + " is not stored inside " + entity.toString());
        return false;
    }

    /**
     * Check if the current animation is a looping animation
     *
     * @return true if the current animation is looping
     */
    public boolean isCurrentAnimationLooping() {
        return currentAnimation.isLooping();
    }

    /**
     * Check if the current animation has finished playing
     *
     * @return true if the current animation has finished playing
     */
    public boolean isCurrentAnimationFinished() {
        return currentAnimation.isFinished();
    }
}
