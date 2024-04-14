package core.utils;

import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * Offers functions to work with and on Asset Paths.
 *
 * @see IPath
 */
public class AssetPathUtils {

  /**
   * Find the (relative) complete path of the given path.
   *
   * <p>This function can be used to load assets only with a part of the path. This allows for
   * easier asset management within the project.
   *
   * <p>For example: For "crafting/wood.png", this could return "items/crafting/wood.png".
   *
   * <p>Note: This will not return the absolute system path; it will return the relative path based
   * on the working directory.
   *
   * @param toComplete Path to complete
   * @return Optional that contains the completed path, or an empty optional if the path could not
   *     be completed.
   */
  public static Optional<IPath> completeAssetPath(final IPath toComplete) {

    // Check if the path is already complete
    File dir = new File(toComplete.pathString());
    if (dir.isFile() && dir.getPath().endsWith(toComplete.pathString()))
      return Optional.of(new SimpleIPath(dir.getPath()));

    // Search recursively
    return findTopLevelPath(toComplete, new SimpleIPath("."));
  }

  /**
   * Complete the given path.
   *
   * <p>Recursive function that will search for the path toComplete in each directory (and
   * subdirectory, and so on) in the given currentPath.
   *
   * @param toComplete Path to complete
   * @param currentPath Current path to check for (will be built in the recursive routine)
   * @return Optional that contains the completed path, or an empty optional if the path could not
   *     be completed.
   */
  private static Optional<IPath> findTopLevelPath(final IPath toComplete, final IPath currentPath) {
    File current = new File(currentPath.pathString());
    if (current.isDirectory()) {
      for (File next : Objects.requireNonNull(current.listFiles())) {
        if (next.isFile() && next.getPath().endsWith(toComplete.pathString()))
          // found it
          return Optional.of(new SimpleIPath(next.getPath()));
        if (next.isDirectory()) {
          // check in subdir
          Optional<IPath> rekRes =
              findTopLevelPath(
                  toComplete, new SimpleIPath(currentPath.pathString() + "/" + next.getName()));
          // check if the path was completed
          if (rekRes.isPresent()) return rekRes;
        }
      }
    }
    // nothing found
    return Optional.empty();
  }
}
