package rooms.morphco.puzzles;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The fuse box in the control room: somebody has pulled out its cables.
 *
 * <p>Every cable belongs in the socket that carries the same label. Once all cables are in, the
 * client sends the wiring encoded with {@link #encode(Set)}. The server checks it with {@link
 * #wired(String)} and only then switches on the emergency lighting.
 */
public final class FuseBox {

  /** The cables, from top to bottom; each is labelled with the part of the factory it supplies. */
  public enum Cable {
    /** The inflection line. */
    F04,
    /** The affix sorter. */
    A12,
    /** The corridor. */
    CORRIDOR,
    /** The control room. */
    CONTROL,
    /** The emergency lighting. */
    EMERGENCY
  }

  private FuseBox() {}

  /**
   * Encodes the wiring for the dialog callback.
   *
   * @param plugged the cables that sit in their sockets
   * @return the answer for the server
   */
  public static String encode(Set<Cable> plugged) {
    return plugged.stream().map(Cable::name).collect(Collectors.joining(";"));
  }

  /**
   * Checks an encoded wiring.
   *
   * @param answer answer from {@link #encode(Set)}
   * @return whether exactly all cables sit in their sockets
   */
  public static boolean wired(String answer) {
    List<String> plugged = List.of(answer.split(";"));
    return plugged.size() == Cable.values().length
        && Arrays.stream(Cable.values()).allMatch(cable -> plugged.contains(cable.name()));
  }
}
