package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.ArrayList;
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
    private static final TextureHandler INSTANCE = new TextureHandler();

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        List<FileHandle> roots =
                getAllAssetRoots(
                        new ArrayList<>(), Gdx.files.internal(Gdx.files.getLocalStoragePath()));
        assert !roots.isEmpty();
        // take the first assets root dir:
        addAllAssets(roots.get(0).parent());
    }

    private List<FileHandle> getAllAssetRoots(List<FileHandle> roots, FileHandle current) {
        if (current.isDirectory()) {
            FileHandle[] fhs = current.list();
            for (FileHandle fh : fhs) {
                getAllAssetRoots(roots, fh);
            }
        } else {
            if ("placeholder-asset-do-not-delete.png".equals(current.name())) {
                roots.add(current);
            }
        }
        return roots;
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
