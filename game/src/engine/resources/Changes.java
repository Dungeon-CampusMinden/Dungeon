package engine.resources;

import java.util.Objects;

/**
 * Detects changes of a resource for one reader.
 *
 * <p>A system that wants to react when a resource changes creates its own {@code Changes} tracker
 * in its constructor and checks it once per {@code execute()}:
 *
 * <pre>{@code
 * private final Res<ILevel> level;
 * private final Changes levelChanges;
 *
 * public MySystem() {
 *   this(Resources.level());
 * }
 *
 * MySystem(Res<ILevel> level) {
 *   this.level = level;
 *   this.levelChanges = Changes.of(level);
 * }
 *
 * public void execute() {
 *   if (levelChanges.changedSinceLastCheck()) {
 *     // react to the new level
 *   }
 * }
 * }</pre>
 *
 * <p>Each tracker remembers on its own which {@link Res#version() version} it has seen last.
 * Therefore every reader notices every change exactly once, no matter in which order the systems
 * run. Do not share one tracker between several systems.
 *
 * <p>The first check always reports a change. This way a reader also reacts to a resource that
 * already existed when the reader was created, for example a level that is already loaded when a
 * system is added.
 */
public final class Changes {

  private static final long NOT_CHECKED_YET = -1L;

  private final Res<?> res;
  private long lastSeenVersion = NOT_CHECKED_YET;

  private Changes(final Res<?> res) {
    this.res = Objects.requireNonNull(res, "res");
  }

  /**
   * Creates a new tracker for the given resource handle.
   *
   * @param res the handle of the resource to track
   * @return a new tracker that has not checked the resource yet
   */
  public static Changes of(final Res<?> res) {
    return new Changes(res);
  }

  /**
   * Checks whether the resource changed since the last call of this method.
   *
   * <p>Calling this method marks the current state as seen. A second call without a change in
   * between returns {@code false}. Call it once per {@code execute()} and store the result in a
   * local variable if it is needed more than once.
   *
   * @return {@code true} on the first call and whenever the resource was inserted, replaced or
   *     removed since the previous call, otherwise {@code false}
   */
  public boolean changedSinceLastCheck() {
    long currentVersion = res.version();
    if (currentVersion == lastSeenVersion) {
      return false;
    }
    lastSeenVersion = currentVersion;
    return true;
  }
}
