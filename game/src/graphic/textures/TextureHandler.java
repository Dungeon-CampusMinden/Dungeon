package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A texture handler class for managing textures paths for further use.
 *
 * <p>This class is designed as Singleton, because all asset paths should be read only once at the
 * beginning of the application.
 */
public class TextureHandler {
    public static final String PLACEHOLDER_FILENAME = ".resource_root";

    private static final TextureHandler INSTANCE = new TextureHandler();

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        addAllAssets(Objects.requireNonNull(getResourceRoot()));
    }

    /**
     * Searches for the resource root that has the longest path name string. Internal helper method.
     *
     * @return the resource root with the longest path name string.
     */
    private FileHandle getResourceRoot() {
        Predicate<Path> isRegularFile = Files::isRegularFile;
        Predicate<Path> isNamedSameAs =
                p -> PLACEHOLDER_FILENAME.equals(p.getFileName().toString());
        Predicate<Path> isBothPredicate = p -> isRegularFile.test(p) && isNamedSameAs.test(p);
        int maxDepthBestGuess = 4;
        try (Stream<Path> walk =
                Files.walk(Path.of(Gdx.files.getLocalStoragePath()), maxDepthBestGuess)) {
            return walk.filter(isBothPredicate)
                    .max(Comparator.comparingInt(a -> a.toString().length()))
                    .map(p -> new FileHandle(p.getParent().toString()))
                    .orElseThrow();
        } catch (Exception e) {
            Logger logger = Logger.getLogger(this.getClass().getName());
            logger.warning(e.toString());
            logger.warning("No resource root found.");
            logger.warning(PLACEHOLDER_FILENAME + " may have been removed.");
            logger.warning("Program will exit.");
            Gdx.app.exit();
            return null;
        }
    }

    /**
     * Returns an instance of this {@link TextureHandler}.
     *
     * @return an instance of this {@link TextureHandler}.
     */
    public static TextureHandler getInstance() {
        return INSTANCE;
    }

    private void addAllAssets(FileHandle fh) {
        if (fh.isDirectory()) {
            Arrays.stream(fh.list()).forEach(this::addAllAssets);
        } else {
            pathMap.computeIfAbsent(fh.path(), x -> new LinkedHashSet<>()).add(fh);
        }
    }

    /**
     * This Method is public only for unit tests. Returns all available asset paths, that were
     * found. Can be used with {@link TextureHandler#getTexturePaths(String)}.
     *
     * @return all available asset paths, that were found.
     */
    public Set<String> getAvailablePaths() {
        return pathMap.keySet();
    }

    private Stream<String> getTexturesForPath(String path) {
        return pathMap.get(path).stream().map(FileHandle::path);
    }

    /**
     * Searches for all textures paths that matches with the given regular expression.
     *
     * <p>Example: knight_m_idle_anim_f(2|3).png will return the paths for knight_m_idle_anim_f2.png
     * and knight_m_idle_anim_f3.png.
     *
     * @param regex the regular expression
     * @return a String List with all texture paths, that have matched
     */
    public List<String> getTexturePaths(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return getAvailablePaths().stream()
                .filter(pattern.asPredicate())
                .flatMap(this::getTexturesForPath)
                .collect(Collectors.toList());
    }
}
