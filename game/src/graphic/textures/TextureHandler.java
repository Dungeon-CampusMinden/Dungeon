package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
        try {
            addAllAssets(getResourceRoot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Searches for the resource root that has the longest path name string. Internal helper method.
     *
     * @return the resource root with the longest path name string.
     */
    private FileHandle getResourceRoot() throws IOException {
        Predicate<Path> isPlaceHolder =
                p -> PLACEHOLDER_FILENAME.equals(p.getFileName().toString());
        int maxDepth = 4;
        return Files.walk(Path.of(Gdx.files.getLocalStoragePath()), maxDepth)
                .filter(Files::isRegularFile)
                .filter(isPlaceHolder)
                .max(Comparator.comparingInt(a -> a.toString().length()))
                .map(p -> new FileHandle(p.getParent().toString()))
                .orElseThrow();
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
