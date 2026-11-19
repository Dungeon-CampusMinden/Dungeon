package feature.prefabs;

import java.util.Objects;

/**
 * A problem that keeps a prefab instance from working as intended, e.g. a door control that is not
 * placed on a door. The level editor lists the problems of the edited level, see {@link
 * PrefabChecks}.
 *
 * @param severity how bad the problem is
 * @param instance name of the instance with the problem, where it is fixed
 * @param message editor-facing description
 * @param port key of the input port of {@code instance} storing the link the problem is about, or
 *     {@code null} for a problem of the instance itself
 * @param link the link the problem is about, or {@code null} for a problem of the instance itself
 */
public record PrefabProblem(
    Severity severity, String instance, String message, String port, PrefabLink link) {

  /** How bad a problem is. */
  public enum Severity {
    /** The instance works, but maybe not as intended. */
    WARNING,
    /** The instance does not work as intended. */
    ERROR
  }

  /** Validates the problem members. */
  public PrefabProblem {
    Objects.requireNonNull(severity, "severity");
    PrefabPorts.requireText(instance, "instance");
    PrefabPorts.requireText(message, "message");
    if ((port == null) != (link == null)) {
      throw new IllegalArgumentException("A link problem needs both the port and the link");
    }
  }

  /**
   * Creates an error of an instance itself.
   *
   * @param instance name of the instance
   * @param message editor-facing description
   * @return the problem
   */
  public static PrefabProblem error(String instance, String message) {
    return new PrefabProblem(Severity.ERROR, instance, message, null, null);
  }

  /**
   * Creates a warning about an instance itself.
   *
   * @param instance name of the instance
   * @param message editor-facing description
   * @return the problem
   */
  public static PrefabProblem warning(String instance, String message) {
    return new PrefabProblem(Severity.WARNING, instance, message, null, null);
  }

  /**
   * Returns whether the problem is about a link rather than the instance itself.
   *
   * @return true if {@link #port()} and {@link #link()} are set
   */
  public boolean isLinkProblem() {
    return link != null;
  }
}
