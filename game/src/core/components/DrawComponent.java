package core.components;

import core.Component;
import core.Entity;
import core.systems.VelocitySystem;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.draw.IPath;

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
 * (for example character/hero) and then add a new Enum-Value JUMP("jump") to enum that implements
 * {@link IPath}
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
public final class DrawComponent implements Component {
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Map<String, Animation> animationMap = null;
    private Animation currentAnimation;
    /** allows only one Element from a certain priority and orders them */
    private Map<IPath, Integer> animationQueue =
            new TreeMap<>(Comparator.comparingInt(IPath::priority));

    /**
     * Create a new DrawComponent.
     *
     * <p>Will read in all subdirectories of the given path and use each file in the subdirectory to
     * create an animation. So each subdirectory should contain only the files for one animation.
     *
     * <p>Animations should not be set directly via {@link #currentAnimation()} but rather be queued
     * via {@link #queueAnimation(IPath...)} or {@link #queueAnimation(int, IPath...)}
     *
     * <p>Will set the current animation to either idle down, idle left, idle right, idle up or
     * idle, depending on which one of these animations exist.
     *
     * <p>If no animations for any idle-state exist, {@link Animation#defaultAnimation()} for "IDLE"
     * is set.
     *
     * @param path Path (as a string) to the directory in the assets folder where the subdirectories
     *     containing the animation files are stored. Example: "character/knight".
     * @throws IOException if the given path does not exist
     * @see Animation
     */
    public DrawComponent(final String path) throws IOException {
        // fetch available animations
        try {
            loadAnimationsFromDirectory(path);
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
     * <p>The given animation will be used as idle-left and idle-right animation
     *
     * <p>This constructor is for special case only. Use {@link DrawComponent(Entity, String)} if
     * possible.
     *
     * @param idle Animation to use as idle-left and idle-right animation.
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
                                + " can not be set, because the given Animation could not be found.");
        }
    }

    /**
     * Queue up an Animation to be considered as the next played Animation. Animations are given as
     * an IPath Array, or multiple variables. Animation length is set as one frame. If you need to
     * queue longer Animations use {@link #queueAnimation(int, IPath...)}
     *
     * @param next Array of IPaths to Animation
     */
    public void queueAnimation(IPath... next) {
        queueAnimation(1, next);
    }

    /**
     * Queue up an Animation to be considered as the next played Animation. Animations are given as
     * an IPath Array, or multiple variables. Animation length is set to given parameter.
     *
     * @param forFrames number of Frames to play Animation for
     * @param next Array of IPaths to Animation
     */
    public void queueAnimation(int forFrames, IPath... next) {
        IPath lowest = null;
        // Streams lose ordering
        for (IPath path : next) {
            if (lowest == null) lowest = path;
            if (lowest.priority() > path.priority()) lowest = path;
        }
        for (Map.Entry<IPath, Integer> entry : animationQueue.entrySet()) {
            // check for already added entry
            if (entry.getKey().equals(lowest)) {
                return;
            }
        }
        // add if not already in queue
        animationQueue.put(lowest, forFrames);
    }

    /**
     * removes all animations with given priority
     *
     * @param prio priority to remove
     */
    public void deQueueByPriority(int prio) {
        animationQueue.keySet().removeIf(e -> e.priority() == prio);
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
        LOGGER.warning("Animation " + path + " is not stored.");
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

    /**
     * Check if Animation is queued up
     *
     * @return true if Animation is in queue
     */
    public boolean isAnimationQueued(IPath requestedAnimation) {
        for (Map.Entry<IPath, Integer> animationArr : animationQueue.entrySet()) {
            if (animationArr.getKey().pathString().equals(requestedAnimation.pathString()))
                return true;
        }
        return false;
    }

    /**
     * @return the whole queue of animations
     */
    public Map<IPath, Integer> animationQueue() {
        return animationQueue;
    }

    /**
     * Allows replacing the whole AnimationQueue with a new one
     *
     * @param queue
     */
    public void animationQueue(Map<IPath, Integer> queue) {
        animationQueue = queue;
    }

    /**
     * Allows replacing and adding Entries.
     *
     * @return the whole animationMap
     */
    public Map<String, Animation> animationMap() {
        return animationMap;
    }

    /*
     * Loading logic
     */
    private void loadAnimationsFromDirectory(String path) throws IOException {
        File jarFile =
                new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) loadAnimationsFromJar(path, jarFile);
        else loadAnimationsFromIDE(path);
    }

    private void loadAnimationsFromJar(String path, File jarFile) throws IOException {
        // This function will create a map of directories (String) and the files
        // (LinkedList<String>) inside these directories.
        // The map will be filled with the directories inside the given path (e.g.,
        // "character/knight").
        // Ultimately, this function will manually create an Animation for each entry within this
        // map.

        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar

        // This will be used to map the directory names (e.g., "idle") and the texture files.
        // Ultimately, we will create animations out of this by using the
        // Animation(LinkedList<String>) constructor.

        HashMap<String, List<String>> storage = new HashMap<>();
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
            if (fileName.startsWith(path + "/")) {

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
                    if (storage.containsKey(lastDir)) storage.get(lastDir).add(fileName);
                    else {
                        LinkedList<String> list = new LinkedList<>();
                        list.add(fileName);
                        storage.put(lastDir, list);
                    }
                }
            }
        }

        // sort the files in lexicographic order (like the most os)
        // animations will be played in order
        storage.values().forEach(Collections::sort);
        // create animations
        storage.forEach((name, textureSet) -> animationMap.put(name, new Animation(textureSet)));
        jar.close();
    }

    private void loadAnimationsFromIDE(String path) {
        URL url = DrawComponent.class.getResource("/" + path);
        if (url != null) {
            try {
                File apps = new File(url.toURI());
                animationMap =
                        Arrays.stream(Objects.requireNonNull(apps.listFiles()))
                                .filter(File::isDirectory)
                                .collect(
                                        Collectors.toMap(
                                                File::getName, Animation::fromSubDir));
            } catch (URISyntaxException ignored) {
            }
        }
    }
}
