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
 * <p>Singleton (possibly thread-safe).
 */
public class TextureHandler {
    private static final TextureHandler INSTANCE = new TextureHandler();

    private final Map<String, Set<FileHandle>> pathMap = new LinkedHashMap<>();

    private TextureHandler() {
        addAllAssets(Gdx.files.internal(Gdx.files.getLocalStoragePath()));
    }

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

    public Set<String> getAvailablePaths() {
        return pathMap.keySet();
    }

    private Stream<String> getTexturesForPath(String path) {
        return pathMap.get(path).stream().map(FileHandle::path);
    }

    private List<String> firstFindFilter(final List<String> pathsOriginal) {
        List<String> pathsCopy = new ArrayList<>();
        for (final String path1 : pathsOriginal) {
            FileHandle fh1 = Gdx.files.absolute(path1);
            boolean isFirst = true;
            for (final String path2 : pathsCopy) {
                FileHandle fh2 = Gdx.files.absolute(path2);
                int differeces = 0;
                FileHandle parent1 = fh1;
                FileHandle parent2 = fh2;
                while (true) {
                    if (parent1.name().isEmpty() || parent2.name().isEmpty()) {
                        if (parent1.name().isEmpty() ^ parent2.name().isEmpty()) {
                            differeces++;
                        }
                        break;
                    }
                    if (!parent1.name().equals(parent2.name())) {
                        differeces++;
                    }
                    parent1 = parent1.parent();
                    parent2 = parent2.parent();
                }
                if (differeces > 1) {
                    isFirst = false;
                    break;
                }
            }
            if (isFirst) {
                pathsCopy.add(path1);
            }
        }
        return pathsCopy;
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
        List<String> paths =
                getAvailablePaths().stream()
                        .filter(pattern.asPredicate())
                        .flatMap(this::getTexturesForPath)
                        .collect(Collectors.toList());
        return firstFindFilter(paths);
    }
}
