package core.components;

import core.Component;
import core.systems.VelocitySystem;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
 * <p>The {@link core.systems.DrawSystem} uses a Priority-based queue. Use {@link
 * #queueAnimation(IPath...)} or {@link #queueAnimation(int, IPath...)} to add an animation to the
 * queue. The {@link core.systems.DrawSystem} will always show the animation with the highest
 * priority in the queue.
 *
 * <p>Use {@link #currentAnimation} to get the current active animation or use {@link #animation} to
 * get a specific animation.
 *
 * <p>Use {@link #hasAnimation} to check if the component has the desired animation.
 *
 * <p>If you want to add your own Animations, create a subdirectory for the animation and add the
 * path to an enum that implements the {@link IPath} interface. So if you want to add a jump
 * animation to the hero, just create a new directory "jump" in the asset directory of your hero
 * (for example character/hero) and then add a new Enum-Value JUMP("jump") to the enum that
 * implements {@link IPath}.
 *
 * <p>Animations will be searched in the default asset directory. Normally, this is "game/assets",
 * but you can change it in the gradle.build file if you like.
 *
 * <p>Note: Each entity needs at least a {@link CoreAnimations#IDLE} Animation.
 *
 * @see Animation
 * @see IPath
 */
public final class DrawComponent implements Component {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
    private Map<String, Animation> animationMap = null;
    private Animation currentAnimation;
    /** allows only one Element from a certain priority and orders them */
    private final Map<IPath, Integer> animationQueue =
            new TreeMap<>(Comparator.comparingInt(IPath::priority));
    /**
     * Create a new DrawComponent.
     *
     * <p>Will read in all subdirectories of the given path and use each file in the subdirectory to
     * create an animation. So each subdirectory should contain only the files for one animation.
     *
     * <p>Animations should not be set directly via {@link #currentAnimation()} but rather be queued
     * via {@link #queueAnimation(IPath...)} or {@link #queueAnimation(int, IPath...)}.
     *
     * <p>Will set the current animation to either idle down, idle left, idle right, idle up, or
     * idle, depending on which one of these animations exists.
     *
     * <p>If no animations for any idle-state exist, {@link Animation#defaultAnimation()} for "IDLE"
     * is set.
     *
     * @param path Path (as a string) to the directory in the assets folder where the subdirectories
     *     containing the animation files are stored. Example: "character/knight".
     * @throws IOException if the given path does not exist.
     * @see Animation
     */
    public DrawComponent(final IPath path) throws IOException {
        // fetch available animations
        try {
            loadAnimationAssets(path);
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
            // We convert the "NullPointerException" to a "FileNotFoundException" because the only
            // reason for a NullPointerException is if the directory does not exist.
            throw new FileNotFoundException("Path " + path + " not found.");
        }
    }

    /**
     * Create a new DrawComponent with a specific animation.
     *
     * <p>The given animation will be used as the idle animation.
     *
     * <p>This constructor is for a special case only. Use {@link DrawComponent(String)} if
     * possible.
     *
     * @param idle Animation to use as the idle animation.
     */
    public DrawComponent(final Animation idle) {
        animationMap = new HashMap<>();
        animationMap.put(CoreAnimations.IDLE_LEFT.pathString(), idle);
        animationMap.put(CoreAnimations.IDLE_RIGHT.pathString(), idle);
        currentAnimation = idle;
    }

    /**
     * Get the current animation being displayed on the entity.
     *
     * @return The current animation of the entity.
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
     *     If more than one path is given, the first one that exists will be set as the new current
     *     animation.
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
                                + " can not be set, because the given Animation could not be found.");
        }
    }

    /**
     * Queue up an Animation to be considered as the next played Animation.
     *
     * <p>Animations are given as an {@link IPath} Array or multiple variables. Animation length is
     * set to one frame. If you need to queue longer Animations, use {@link #queueAnimation(int,
     * IPath...)}. The First existing Animation * will be added to the queue.
     *
     * @param next Array of IPaths representing the Animation.
     */
    public void queueAnimation(final IPath... next) {
        queueAnimation(1, next);
    }

    /**
     * Queue up an Animation to be considered as the next played Animation.
     *
     * <p>Animations are given as an {@link IPath} Array or multiple variables. The First existing
     * Animation will be added to the queue. If the Animation is already added, the remaining Frames
     * are set to the highest of remaining or new.
     *
     * <p>Animation length is set to the given parameter.
     *
     * @param forFrames Number of frames to play the Animation for.
     * @param next Array of IPaths representing the Animation.
     */
    public void queueAnimation(int forFrames, final IPath... next) {
        for (IPath path : next) {
            // is an existing animation of the component
            if (animationMap.containsKey(path.pathString())) {
                // check if the path is already queued
                if (animationQueue.containsKey(path)) {
                    // update time of the animation
                    animationQueue.put(path, Math.max(animationQueue.get(path), forFrames));
                } else {
                    // add animation
                    animationQueue.put(path, forFrames);
                }
                return;
            }
        }
    }

    /**
     * Remove the given animation from the animation queue.
     *
     * <p>This method removes the animation specified by the provided path from the animation queue.
     *
     * @param animation The path of the animation to remove from the queue.
     */
    public void deQueue(final IPath animation) {
        animationQueue.remove(animation);
    }

    /**
     * Remove all animations with the given priority from the animation queue.
     *
     * <p>This method removes all animations from the animation queue that have the specified
     * priority.
     *
     * @param prio The priority of animations to remove.
     */
    public void deQueueByPriority(int prio) {
        animationQueue.keySet().removeIf(e -> e.priority() == prio);
    }

    /**
     * Get the Animation at the given path.
     *
     * <p>Can be null if the component does not store an animation with this path.
     *
     * @param path Path of the Animation.
     * @return The animation or null.
     */
    public Optional<Animation> animation(final IPath path) {
        return Optional.ofNullable(animationMap.get(path.pathString()));
    }
    /**
     * Check if the component stores an animation with the given path.
     *
     * @param path Path of the animation to look for.
     * @return true if the animation exists in this component, false if not.
     */
    public boolean hasAnimation(final IPath path) {
        return animationMap.containsKey(path.pathString());
    }

    /**
     * Check if the animation at the given path is the current animation.
     *
     * <p>Will log a warning if no animation is stored for the given path.
     *
     * @param path Path to the animation to check.
     * @return true if the current animation equals the animation at the given path, false if not,
     *     or no animation for the given path is stored in this component.
     */
    public boolean isCurrentAnimation(final IPath path) {
        Optional<Animation> animation = animation(path);
        if (animation.isPresent()) return animation.get() == currentAnimation;
        LOGGER.warning("Animation " + path + " is not stored.");
        return false;
    }

    /**
     * Check if the current animation is a looping animation.
     *
     * @return true if the current animation is looping.
     */
    public boolean isCurrentAnimationLooping() {
        return currentAnimation.isLooping();
    }

    /**
     * Check if the current animation has finished playing.
     *
     * @return true if the current animation has finished playing.
     */
    public boolean isCurrentAnimationFinished() {
        return currentAnimation.isFinished();
    }

    /**
     * Check if the Animation is queued up.
     *
     * @return true if the Animation is in the queue.
     */
    public boolean isAnimationQueued(final IPath requestedAnimation) {
        for (Map.Entry<IPath, Integer> animationArr : animationQueue.entrySet()) {
            if (animationArr.getKey().pathString().equals(requestedAnimation.pathString()))
                return true;
        }
        return false;
    }

    /**
     * Get a copy of the animation queue.
     *
     * @return The entire queue of animations.
     */
    public Map<IPath, Integer> animationQueue() {
        return new HashMap<>(animationQueue);
    }

    /**
     * Get a copy of the Map that contains all the Animations in this component.
     *
     * <p>This method returns a new HashMap containing a copy of the original animationMap.
     * Modifying the returned map will not affect the internal state of this component.
     *
     * @return A new Map containing a copy of the animationMap.
     */
    public Map<String, Animation> animationMap() {
        return new HashMap<>(animationMap);
    }

    /**
     * Replace the current animationMap with a new Map.
     *
     * <p>This method allows replacing the entire animationMap with a new one provided as a
     * parameter. The new animationMap is a mapping of animation names (String) to their
     * corresponding Animation objects.
     *
     * @param animationMap the new animationMap.
     */
    public void animationMap(final Map<String, Animation> animationMap) {
        if (animationMap == null)
            throw new IllegalArgumentException("AnimationMap can not be null");
        this.animationMap = new HashMap<>(animationMap);
    }

    /**
     * Loading animation assets.
     *
     * <p>Checks if the game is running in a JAR or not and will execute the corresponding loading
     * logic.
     */
    private void loadAnimationAssets(final IPath path) throws IOException {
        File jarFile =
                new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) loadAnimationsFromJar(path, jarFile);
        else loadAnimationsFromIDE(path);
    }

    /**
     * Load the animation assets if the game is running in a JAR.
     *
     * <p>This function will create a map of directories (String) and the files (LinkedList<String>)
     * inside these directories. The map will be filled with the directories inside the given path
     * (e.g., "character/knight"). Ultimately, this function will manually create an Animation for
     * each entry within this map.
     *
     * @param path Path to the assets.
     * @param jarFile Path to the JAR files.
     * @throws IOException if the JAR file or the files in the JAR file cannot be read.
     */
    private void loadAnimationsFromJar(final IPath path, final File jarFile) throws IOException {

        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar

        // This will be used to map the directory names (e.g., "idle") and the texture files.
        // Ultimately, we will create animations out of this by using the
        // Animation(LinkedList<String>) constructor.

        HashMap<String, List<IPath>> storage = new HashMap<>();
        animationMap = new HashMap<>();

        // Iterate over each file and directory in the JAR.

        while (entries.hasMoreElements()) {
            // example: character/knight/idle_down/idle_down_knight_1.png
            // but also: character/knight/idle/
            // and: character/knight/
            String fileName = entries.nextElement().getName();

            // If the entry starts with the path name (character/knight/idle),
            // this is true for entries like (character/knight/idle_down/idle_down_knight_1.png) and
            // (character/knight/idle/).
            if (fileName.startsWith(path.pathString() + "/")) {

                // Get the index of the last FileSeparator; every character after that separator is
                // part of the filename.
                int lastSlashIndex = fileName.lastIndexOf("/");

                // Ignore directories, so we only work with strings like
                // (character/knight/idle_down/idle_down_knight_1.png).
                if (lastSlashIndex != fileName.length() - 1) {
                    // Get the index of the second-to-last part of the string.
                    // For example, in "character/knight/idle_down/idle_down_knight_1.png", this
                    // would
                    // be the
                    // index of the slash in "/idle".

                    int secondLastSlashIndex = fileName.lastIndexOf("/", lastSlashIndex - 1);

                    // Get the name of the directory. The directory name is between the
                    // second-to-last and the last separator index.
                    // The directory name serves as the key of the animation in the animation map
                    // (similar to what the IPATh values are for them).
                    // For example: "idle"

                    String lastDir = fileName.substring(secondLastSlashIndex + 1, lastSlashIndex);

                    // add animation-files to new or existing storage map
                    if (storage.containsKey(lastDir))
                        storage.get(lastDir).add(new SimpleIPath(fileName));
                    else {
                        LinkedList<IPath> list = new LinkedList<>();
                        list.add(new SimpleIPath(fileName));
                        storage.put(lastDir, list);
                    }
                }
            }
        }

        // sort the files in lexicographic order (like the most os)
        // animations will be played in order
        storage.values().forEach(x -> x.sort(Comparator.comparing(IPath::pathString)));
        // create animations
        storage.forEach(
                (name, textureSet) -> animationMap.put(name, Animation.fromCollection(textureSet)));
        jar.close();
    }

    /**
     * Load animations if the game is running in the IDE (or over the shell).
     *
     * @param path Path to the animations.
     */
    private void loadAnimationsFromIDE(final IPath path) {
        URL url = DrawComponent.class.getResource("/" + path.pathString());
        if (url != null) {
            try {
                File apps = new File(url.toURI());
                animationMap =
                        Arrays.stream(Objects.requireNonNull(apps.listFiles()))
                                .filter(File::isDirectory)
                                .collect(
                                        Collectors.toMap(
                                                File::getName,
                                                DrawComponent::allFilesFromDirectory));
            } catch (URISyntaxException ignored) {
            }
        }
    }

    /**
     * @param subDir in which to look for files for the animation
     * @return a basic configured Animation
     */
    private static Animation allFilesFromDirectory(final File subDir) {
        return Animation.fromCollection(
                Arrays.stream(Objects.requireNonNull(subDir.listFiles()))
                        // only look for direct Files no recursive search
                        .filter(File::isFile)
                        // File object needs to be converted to IPath
                        .map(file -> new SimpleIPath(file.getPath()))
                        // sort by name streams may lose the ordering by name
                        .sorted(Comparator.comparing(SimpleIPath::pathString))
                        .collect(Collectors.toList()));
    }
}
