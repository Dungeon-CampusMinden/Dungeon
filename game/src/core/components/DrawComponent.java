package core.components;

import core.Component;
import core.Entity;
import core.systems.VelocitySystem;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimationPathEnum;
import core.utils.components.draw.IAnimationPathEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The DrawComponent stores all {@link Animation}s for an entity.
 *
 * <p>At creation, the component will read in each subdirectory in the given path and create an
 * animation for each subdirectory.
 *
 * <p>Each Animation will be created with default settings. If you want to change these settings,
 * use the methods from {@link Animation}.
 *
 * <p>Use {@link #setCurrentAnimation} to set the current Animation.
 *
 * <p>Use {@link #getCurrentAnimation} to get the current active animation or use {@link
 * #getAnimation} to get a specific animation.
 *
 * <p>Use {@link #hasAnimation} to check if the component has the desired animation.
 *
 * <p>If you want to add your own Animations, create a subdirectory for the animation and add the
 * path to an enum that implements the {@link IAnimationPathEnum} interface.
 *
 * <p>Note: each entity needs at least a {@link
 * core.utils.components.draw.CoreAnimationPathEnum#IDLE_LEFT} and {@link
 * core.utils.components.draw.CoreAnimationPathEnum#IDLE_RIGHT} Animation
 *
 * @see Animation
 * @see IAnimationPathEnum
 */
public class DrawComponent extends Component {
    private final int DEFAULT_FRAME_TIME = 3;
    private final boolean DEFAULT_IS_LOOP = true;
    private final Map<String, Animation> animationMap;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Animation currentAnimation;

    /**
     * Create a new DrawComponent.
     *
     * <p>Will read in all subdirectories of the given path and use each file in the subdirectory
     * to create an animation. So each subdirectory should contain only the files for one animation.
     *
     * <p>Will set the current animation to idle left
     *
     * @param entity associated entity
     * @param path   Path (as a string) to the directory in the assets folder where the subdirectories containing the animation files are stored. Example: "character/knight".
     * @throws IOException if the given path does not exist
     * @see Animation
     */
    public DrawComponent(Entity entity, String path) throws IOException {
        super(entity);

        // fetch available animations
        ClassLoader classLoader = getClass().getClassLoader();
        File directory = new File(classLoader.getResource(path).getFile());
        if (!directory.exists() || !directory.isDirectory()) {
            throw new FileNotFoundException("Path " + path + " not found.");
        }
        animationMap =
            Arrays.stream(directory.listFiles())
                .filter(File::isDirectory)
                .collect(
                    Collectors.toMap(
                        File::getName,
                        subDir ->
                            Animation.of(
                                subDir,
                                DEFAULT_FRAME_TIME,
                                DEFAULT_IS_LOOP)));

        // set current animation
        currentAnimation = animationMap.get(CoreAnimationPathEnum.IDLE_LEFT);
    }

    /**
     * Get the current animation being displayed on the entity.
     *
     * @return the current animation of the entity
     */
    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    /**
     * Set the current animation displayed on the entity.
     *
     * <p>If the animation passed is not displayed on the entity, there may be another point in the
     * code where the animation is overwritten on the same tick (e.g., in {@link VelocitySystem}).
     *
     * @param animationName Path of the new current animation (this is the name of the directory).
     * @see IAnimationPathEnum
     */
    public void setCurrentAnimation(IAnimationPathEnum animationName) {
        Animation animation = animationMap.get(animationName.toString());
        if (animation != null) this.currentAnimation = animation;
    }

    /**
     * Get the Animation at the given path.
     *
     * <p>Can be null if the component does not store an animation with this path.
     *
     * @param path Path of the Animation
     * @return The animation or null
     */
    public Optional<Animation> getAnimation(IAnimationPathEnum path) {
        return Optional.ofNullable(animationMap.get(path.toString()));
    }

    /**
     * Check if the component stores an animation with the given path.
     *
     * @param path Path of the animation to look for
     * @return true if the animation exists in this component, false if not
     */
    public boolean hasAnimation(IAnimationPathEnum path) {
        return animationMap.containsKey(path.toString());
    }
}
