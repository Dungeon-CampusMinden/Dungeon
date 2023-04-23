package graphic.textures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A texture handler class for managing textures paths for further use.
 *
 * <p>Singleton.
 */
public class TextureHandler {
    private static TextureHandler INSTANCE;

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        addAllAssets(Gdx.files.internal("build/resources"));
    }

    public static TextureHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TextureHandler();
        }
        return INSTANCE;
    }

    private void addAllAssets(FileHandle fh) {
        if (fh.isDirectory()) {
            Arrays.stream(fh.list()).forEach(this::addAllAssets);
        } else {
            pathMap.computeIfAbsent(fh.path(), x -> new LinkedHashSet<>()).add(fh);
        }
    }

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
