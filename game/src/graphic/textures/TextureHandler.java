package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A texture handler class for managing textures paths for further use.
 *
 * <p>Singleton.
 */
public class TextureHandler {
    public static final String PLACEHOLDER_FILENAME = ".resource_root_please_not_modify";
    private static final TextureHandler INSTANCE = new TextureHandler();

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        List<Path> placeholderPaths = findAllPlaceholderPaths();
        assert placeholderPaths.size() == 2;
        // takes the placeholder path with the longest path string...
        if (placeholderPaths.get(0).toString().length()
                > placeholderPaths.get(1).toString().length()) {
            addAllAssets(new FileHandle(placeholderPaths.get(0).toFile()));
        } else {
            addAllAssets(new FileHandle(placeholderPaths.get(1).toFile()));
        }
    }

    private List<Path> findAllPlaceholderPaths() {
        try (Stream<Path> walk = Files.walk(Path.of(Gdx.files.getLocalStoragePath()), 3)) {
            return walk.filter(x -> Files.isRegularFile(x) && x.endsWith(PLACEHOLDER_FILENAME))
                    .toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns an instance of this {@link TextureHandler}.
     *
     * @return Returns an instance of this {@link TextureHandler}.
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
     * Returns all available asset paths, that was found. Can be used with {@link
     * TextureHandler#getTexturePaths(String)}.
     *
     * @return Returns all available asset paths, that was found.
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
