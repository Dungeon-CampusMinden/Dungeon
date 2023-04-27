package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
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
 * <p>Singleton.
 */
public class TextureHandler {
    private static final TextureHandler INSTANCE = new TextureHandler();

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        List<FileHandle> placeholderAssets = findAllPlaceholderAssets();
        assert placeholderAssets.size() == 2;
        // takes the placeholder assets with the longest path string...
        if (placeholderAssets.get(0).path().length() > placeholderAssets.get(1).path().length()) {
            addAllAssets(placeholderAssets.get(0).parent());
        } else {
            addAllAssets(placeholderAssets.get(1).parent());
        }
    }

    private List<FileHandle> findAllPlaceholderAssets() {
        // needs circa 277 currentDir.list() calls...
        List<FileHandle> results = new ArrayList<>();
        Deque<FileHandle> dirQueue = new ArrayDeque<>();
        dirQueue.add(Gdx.files.internal(Gdx.files.getLocalStoragePath()));
        while (!dirQueue.isEmpty()) {
            FileHandle currentDir = dirQueue.removeFirst();
            FileHandle[] list = currentDir.list();
            List<FileHandle> currentDirs =
                    Arrays.stream(list).filter(FileHandle::isDirectory).toList();
            List<FileHandle> currentFiles =
                    Arrays.stream(list).filter(Predicate.not(FileHandle::isDirectory)).toList();
            boolean hasPlaceholderAsset = false;
            for (FileHandle file : currentFiles) {
                if ("placeholder-asset-do-not-delete.png".equals(file.name())) {
                    hasPlaceholderAsset = true;
                    results.add(file);
                }
            }
            if (!hasPlaceholderAsset) {
                dirQueue.addAll(currentDirs);
            }
        }
        return results;
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
