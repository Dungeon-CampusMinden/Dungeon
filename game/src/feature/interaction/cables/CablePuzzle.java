package feature.interaction.cables;

import com.badlogic.gdx.graphics.Color;
import feature.hud.dialogs.DialogContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Loose cables and the sockets they belong in: every cable has exactly one socket.
 *
 * <p>A cable shows a label and a color, a socket only a label. Labels may be translation keys, the
 * displaying client translates them. A socket label may differ from the label of its cable, e.g. a
 * term and its definition.
 *
 * @param cables the cables, from top to bottom
 * @param sockets the socket labels, from top to bottom
 */
public record CablePuzzle(List<Cable> cables, List<String> sockets) {

  /** Fewest cables of a puzzle. */
  public static final int MIN_CABLES = 2;

  /** Most cables of a puzzle; more do not fit on the screen. */
  public static final int MAX_CABLES = 8;

  private static final String LABELS = "cablesLabels";
  private static final String COLORS = "cablesColors";
  private static final String TARGETS = "cablesTargets";
  private static final String SOCKETS = "cablesSockets";

  /**
   * A loose cable.
   *
   * @param label text on its plug
   * @param color color of the cable and its plug
   * @param socket index of the socket it belongs in
   */
  public record Cable(String label, Color color, int socket) {}

  /**
   * Creates a puzzle.
   *
   * @throws IllegalArgumentException if there are fewer than {@link #MIN_CABLES} or more than
   *     {@link #MAX_CABLES} cables, or if the sockets do not take exactly one cable each
   */
  public CablePuzzle {
    cables = List.copyOf(cables);
    sockets = List.copyOf(sockets);
    if (cables.size() < MIN_CABLES || cables.size() > MAX_CABLES) {
      throw new IllegalArgumentException(
          "A cable puzzle has " + MIN_CABLES + " to " + MAX_CABLES + " cables: " + cables.size());
    }
    List<Integer> taken = cables.stream().map(Cable::socket).sorted().toList();
    if (!taken.equals(IntStream.range(0, sockets.size()).boxed().toList())) {
      throw new IllegalArgumentException(
          "Every socket takes exactly one cable: " + cables + ", " + sockets);
    }
  }

  /**
   * Reads the puzzle that {@link #put(DialogContext.Builder)} put into a dialog context.
   *
   * @param context dialog context of a cable dialog
   * @return the puzzle
   */
  public static CablePuzzle from(DialogContext context) {
    String[] labels = context.require(LABELS, String[].class);
    String[] colors = context.require(COLORS, String[].class);
    int[] targets = context.require(TARGETS, int[].class);
    List<Cable> cables = new ArrayList<>();
    for (int cable = 0; cable < labels.length; cable++) {
      cables.add(new Cable(labels[cable], Color.valueOf(colors[cable]), targets[cable]));
    }
    return new CablePuzzle(cables, List.of(context.require(SOCKETS, String[].class)));
  }

  /**
   * Puts the puzzle into the context of a dialog, so the client can show it and check every plug
   * right away.
   *
   * @param context builder of the dialog context
   */
  public void put(DialogContext.Builder context) {
    context
        .put(LABELS, cables.stream().map(Cable::label).toArray(String[]::new))
        .put(COLORS, cables.stream().map(cable -> cable.color().toString()).toArray(String[]::new))
        .put(TARGETS, solution())
        .put(SOCKETS, sockets.toArray(String[]::new));
  }

  /**
   * Checks whether a cable belongs in a socket.
   *
   * @param cable index of the cable
   * @param socket index of the socket
   * @return true if the socket takes the cable
   */
  public boolean fits(int cable, int socket) {
    return cables.get(cable).socket() == socket;
  }

  /**
   * Checks a plug a client reported as wrong.
   *
   * @param plug index of the cable and index of the socket
   * @return true if both exist and the cable belongs in another socket
   */
  public boolean wrong(int[] plug) {
    return plug.length == 2
        && plug[0] >= 0
        && plug[0] < cables.size()
        && plug[1] >= 0
        && plug[1] < sockets.size()
        && !fits(plug[0], plug[1]);
  }

  /**
   * Checks a finished wiring.
   *
   * @param wiring index of the socket of every cable
   * @return true if every cable sits in its socket
   */
  public boolean connected(int[] wiring) {
    return Arrays.equals(wiring, solution());
  }

  private int[] solution() {
    return cables.stream().mapToInt(Cable::socket).toArray();
  }
}
