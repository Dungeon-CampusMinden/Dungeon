package feature.prefabs;

import java.util.regex.Pattern;

/**
 * Link from an output port of a source instance to an input port of the instance storing the link.
 *
 * @param source name of the source instance
 * @param port key of the output port on the source instance
 * @param alias name of the value in multi-value artifact slots, otherwise {@code null}
 */
public record PrefabLink(String source, String port, String alias) {

  private static final Pattern ALIAS = Pattern.compile("[A-Za-z0-9_-]+");

  /** Validates the link members. */
  public PrefabLink {
    PrefabPorts.requireText(source, "source");
    PrefabPorts.requireText(port, "port");
    if (alias != null && !ALIAS.matcher(alias).matches()) {
      throw new IllegalArgumentException(
          "Artifact alias '" + alias + "' may only contain letters, digits, '_' and '-'");
    }
  }

  /**
   * Creates a link without an alias.
   *
   * @param source name of the source instance
   * @param port key of the output port on the source instance
   */
  public PrefabLink(String source, String port) {
    this(source, port, null);
  }

  /**
   * Returns whether this link points at the same output port as another one.
   *
   * @param other other link
   * @return true if source and port are equal
   */
  public boolean sameOutput(PrefabLink other) {
    return source.equals(other.source) && port.equals(other.port);
  }

  /**
   * Creates a copy pointing at a renamed source instance.
   *
   * @param newSource new source instance name
   * @return updated link
   */
  public PrefabLink withSource(String newSource) {
    return new PrefabLink(newSource, port, alias);
  }

  /**
   * Creates a copy with another alias.
   *
   * @param newAlias new alias, or {@code null}
   * @return updated link
   */
  public PrefabLink withAlias(String newAlias) {
    return new PrefabLink(source, port, newAlias);
  }

  /**
   * Checks whether a text is a valid alias.
   *
   * @param alias text to check
   * @return true if the text may be used as alias
   */
  public static boolean isValidAlias(String alias) {
    return alias != null && ALIAS.matcher(alias).matches();
  }

  @Override
  public String toString() {
    return source + "." + port + (alias == null ? "" : " as " + alias);
  }
}
