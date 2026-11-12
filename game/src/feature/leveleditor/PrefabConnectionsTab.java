package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import feature.leveleditor.ui.EditorTheme;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabLink;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabWiring;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds the "Connections" tab of the prefab editor: every port of the selected instance with its
 * links, and the controls to add, remove, follow and test links.
 *
 * <p>Links are stored on the receiving instance, but the tab shows and edits them from both ends.
 */
final class PrefabConnectionsTab {

  /** Color of event and action links. */
  static final Color SIGNAL_COLOR = new Color(1f, 0.6f, 0.2f, 1f);

  /** Color of artifact links. */
  static final Color ARTIFACT_COLOR = new Color(0.4f, 0.72f, 1f, 1f);

  static final Color ERROR_COLOR = EditorTheme.ERROR;
  static final Color WARNING_COLOR = EditorTheme.WARNING;
  static final Color MUTED_COLOR = EditorTheme.TEXT_MUTED;

  private static final Color SUCCESS = EditorTheme.SUCCESS;
  private static final int PORT_FONT_SIZE = 15;
  private static final int TEXT_FONT_SIZE = 14;
  private static final int HINT_FONT_SIZE = 12;
  private static final float ROW_HEIGHT = 28f;
  private static final float BUTTON_SIZE = 26f;

  // Whether the lifecycle actions are listed when none of them is linked. Remembered between
  // selections.
  private static boolean showLifecycle;

  /**
   * One end of a link that is being created: a port of the selected instance.
   *
   * @param instance name of the instance owning the port
   * @param port the port
   */
  record LinkEnd(String instance, PrefabPort port) {}

  /** Operations of the prefab editor used by the tab. */
  interface Host {
    /**
     * Returns the authored instances of the level.
     *
     * @return instances in load order
     */
    List<PrefabInstance> instances();

    /**
     * Opens the list of ports that can be linked to a port.
     *
     * @param end port of the selected instance
     * @param anchor actor the list is shown next to
     */
    void connect(LinkEnd end, Actor anchor);

    /**
     * Waits for a click on a prefab in the world to link a port to.
     *
     * @param end port of the selected instance
     */
    void pickInWorld(LinkEnd end);

    /**
     * Removes a link.
     *
     * @param receiver name of the instance storing the link
     * @param port input port key
     * @param link the link
     */
    void removeLink(String receiver, String port, PrefabLink link);

    /**
     * Changes the alias of a link into a multi-value artifact slot.
     *
     * @param receiver name of the instance storing the link
     * @param port input port key
     * @param link the link
     * @param alias new alias
     */
    void setAlias(String receiver, String port, PrefabLink link, String alias);

    /**
     * Selects an instance and moves the camera to it.
     *
     * @param instance instance name
     */
    void focus(String instance);

    /**
     * Runs an action or raises an event of an instance in the running game.
     *
     * @param instance instance name
     * @param port action or event
     */
    void test(String instance, PrefabPort port);

    /**
     * Returns whether ports can be tested in the running game.
     *
     * @return true if actions can be run
     */
    boolean canTest();

    /** Rebuilds the tab, e.g. after a section was expanded. */
    void refresh();
  }

  private final Host host;

  /**
   * Creates the tab builder.
   *
   * @param host prefab editor operations
   */
  PrefabConnectionsTab(Host host) {
    this.host = Objects.requireNonNull(host, "host");
  }

  /**
   * Fills a table with the ports of an instance.
   *
   * @param content table to fill
   * @param instance normalized selected instance
   * @param problems wiring problems of the level
   */
  void build(Table content, PrefabInstance instance, List<PrefabWiring.Problem> problems) {
    Prefab prefab = PrefabRegistry.require(instance.type());
    section(content, instance, prefab.ports(instance, PrefabPort.Kind.EVENT), problems, "Events");
    List<PrefabPort> actions = prefab.ports(instance, PrefabPort.Kind.ACTION);
    section(
        content,
        instance,
        actions.stream().filter(port -> !((PrefabAction) port).isLifecycle()).toList(),
        problems,
        "Actions");
    section(
        content,
        instance,
        prefab.ports(instance, PrefabPort.Kind.ARTIFACT_OUTPUT),
        problems,
        "Artifact Outputs");
    section(
        content,
        instance,
        prefab.ports(instance, PrefabPort.Kind.ARTIFACT_SLOT),
        problems,
        "Artifact Inputs");
    List<PrefabPort> lifecycle =
        actions.stream().filter(port -> ((PrefabAction) port).isLifecycle()).toList();
    boolean lifecycleLinked = lifecycle.stream().anyMatch(port -> !instance.links(port).isEmpty());
    if (!lifecycle.isEmpty()) {
      boolean open = showLifecycle || lifecycleLinked;
      Table header = new Table();
      Image chevron =
          new Image(
              EditorTheme.icon(open ? "chevron-down" : "chevron-right", EditorTheme.TEXT_MUTED));
      header.add(chevron).size(9f).padRight(6f);
      header.add(EditorTheme.caption("Lifecycle Actions")).left().growX();
      header.setTouchable(Touchable.enabled);
      header.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              showLifecycle = !open;
              host.refresh();
            }
          });
      EditorTheme.tooltip(header, "Activate, deactivate or reset this prefab from other prefabs.");
      content.add(header).growX().padTop(16f).padLeft(2f).row();
      if (open) {
        for (PrefabPort port : lifecycle)
          content.add(portBlock(instance, port, problems)).padTop(2f).row();
      }
    }
  }

  /**
   * Counts the links of an instance, in both directions.
   *
   * @param instances instances of the level
   * @param instance normalized instance
   * @return number of links
   */
  static int linkCount(List<PrefabInstance> instances, PrefabInstance instance) {
    int incoming = instance.inputs().values().stream().mapToInt(List::size).sum();
    int outgoing = PrefabWiring.outgoing(instances, instance.name()).size();
    return incoming + outgoing;
  }

  private void section(
      Table content,
      PrefabInstance instance,
      List<PrefabPort> ports,
      List<PrefabWiring.Problem> problems,
      String title) {
    if (ports.isEmpty()) return;
    content.add(EditorTheme.caption(title)).left().padTop(16f).padLeft(2f).padBottom(2f).row();
    for (PrefabPort port : ports) content.add(portBlock(instance, port, problems)).padTop(2f).row();
  }

  private Table portBlock(
      PrefabInstance instance, PrefabPort port, List<PrefabWiring.Problem> problems) {
    Table block = new Table();
    block.defaults().growX().minWidth(0f);

    List<PrefabWiring.Connection> outgoing =
        port.kind().isInput()
            ? List.of()
            : PrefabWiring.outgoing(host.instances(), instance.name(), port.key());
    boolean linked = port.kind().isInput() ? !instance.links(port).isEmpty() : !outgoing.isEmpty();

    Table header = new Table();
    Label name =
        EditorTheme.label(
            portTitle(instance, port),
            PORT_FONT_SIZE,
            linked ? EditorTheme.TEXT : EditorTheme.TEXT_MUTED);
    name.setEllipsis(true);
    header.add(name).growX().minWidth(0f).left().padLeft(2f);
    // Send and Run are only offered for ports that are part of a connection.
    if (port.kind().isSignal() && linked) {
      ImageButton test =
          iconButton(
              "play",
              SUCCESS,
              port.kind() == PrefabPort.Kind.EVENT
                  ? "Send: raise this event in the running game"
                  : "Run: run this action in the running game",
              () -> host.test(instance.name(), port));
      test.setDisabled(!host.canTest());
      header.add(test).size(BUTTON_SIZE);
    }
    boolean replaces =
        port instanceof ArtifactSlot<?> slot && !slot.multiple() && !instance.links(port).isEmpty();
    LinkEnd end = new LinkEnd(instance.name(), port);
    ImageButton connect =
        iconButton(
            "plus",
            EditorTheme.TEXT,
            replaces ? "Replace the link..." : "Link to a prefab of the list...",
            null);
    connect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            host.connect(end, connect);
          }
        });
    header.add(connect).size(BUTTON_SIZE);
    header
        .add(
            iconButton(
                "target",
                EditorTheme.TEXT,
                "Pick in the level: click the prefab to link",
                () -> host.pickInWorld(end)))
        .size(BUTTON_SIZE);
    block.add(header).height(ROW_HEIGHT).row();
    portNote(instance, port)
        .ifPresent(
            note -> block.add(wrapped(note, HINT_FONT_SIZE, MUTED_COLOR)).padLeft(14f).row());

    Color linkColor = port.kind().isSignal() ? SIGNAL_COLOR : ARTIFACT_COLOR;
    if (port.kind().isInput()) {
      for (PrefabLink link : instance.links(port)) {
        Optional<PrefabWiring.Problem> problem =
            problemOf(problems, instance.name(), port.key(), link);
        String output = outputName(host.instances(), link);
        block
            .add(
                linkRow(
                    "from",
                    link.source() + "  >  " + output,
                    problem.isPresent() ? severityColor(problem.get()) : linkColor,
                    link.source(),
                    () -> host.removeLink(instance.name(), port.key(), link),
                    port instanceof ArtifactSlot<?> slot && slot.multiple()
                        ? aliasField(instance.name(), port.key(), link)
                        : null))
            .padTop(1f)
            .row();
        problem.ifPresent(
            p ->
                block
                    .add(wrapped(p.message(), HINT_FONT_SIZE, severityColor(p)))
                    .padLeft(14f)
                    .row());
      }
    } else {
      for (PrefabWiring.Connection connection : outgoing) {
        Optional<PrefabWiring.Problem> problem =
            problemOf(
                problems,
                connection.receiver().name(),
                connection.input().key(),
                connection.link());
        block
            .add(
                linkRow(
                    "to",
                    connection.receiver().name() + "  >  " + connection.input().displayName(),
                    problem.isPresent() ? severityColor(problem.get()) : linkColor,
                    connection.receiver().name(),
                    () ->
                        host.removeLink(
                            connection.receiver().name(),
                            connection.input().key(),
                            connection.link()),
                    null))
            .padTop(1f)
            .row();
        problem.ifPresent(
            p ->
                block
                    .add(wrapped(p.message(), HINT_FONT_SIZE, severityColor(p)))
                    .padLeft(14f)
                    .row());
      }
    }
    return block;
  }

  private Table linkRow(
      String direction,
      String text,
      Color color,
      String other,
      Runnable remove,
      TextField aliasField) {
    Table row = new Table();
    row.setBackground(
        EditorTheme.padded(EditorTheme.rounded(EditorTheme.alpha(color, 0.10f), null, 4), 6f, 1f));
    Image arrow = new Image(EditorTheme.icon(direction.equals("to") ? "go" : "link", color));
    arrow.setScaling(Scaling.fit);
    row.add(arrow).size(12f).padLeft(2f).padRight(6f);
    if (aliasField != null) row.add(aliasField).width(80f).height(22f).padRight(6f);
    Label label = EditorTheme.label(text, TEXT_FONT_SIZE, color);
    label.setEllipsis(true);
    row.add(label).growX().minWidth(0f).left();
    row.add(iconButton("go", EditorTheme.TEXT_MUTED, "Select " + other, () -> host.focus(other)))
        .size(22f);
    row.add(iconButton("close", EditorTheme.ERROR, "Remove this link", remove)).size(22f);
    Table indented = new Table();
    indented.add(row).growX().minWidth(0f).height(26f).padLeft(12f);
    return indented;
  }

  private TextField aliasField(String receiver, String port, PrefabLink link) {
    TextField field = EditorTheme.textField(link.alias(), 13);
    field.setMessageText("alias");
    field.setTextFieldFilter(
        (textField, character) ->
            Character.isLetterOrDigit(character) || "_-".indexOf(character) >= 0);
    String[] committed = {link.alias()};
    Runnable commit =
        () -> {
          String value = field.getText().strip();
          if (value.equals(committed[0])) return;
          committed[0] = value;
          host.setAlias(receiver, port, link, value);
        };
    field.setTextFieldListener(
        (textField, character) -> {
          if (character == '\r' || character == '\n') commit.run();
        });
    field.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
            if (!focused) commit.run();
          }
        });
    EditorTheme.tooltip(field, "Alias of this value, used as {alias} in texts.");
    return field;
  }

  private static String portTitle(PrefabInstance instance, PrefabPort port) {
    String name = PrefabRegistry.require(instance.type()).portDisplayName(instance, port);
    return switch (port) {
      case ArtifactOutput<?> output -> name + "  (" + output.type().displayName() + ")";
      case ArtifactSlot<?> slot ->
          name + "  (" + slot.type().displayName() + (slot.multiple() ? ", any number" : "") + ")";
      default -> name;
    };
  }

  /**
   * Returns the editor-facing name of the output port a link starts at.
   *
   * @param instances instances of the level
   * @param link the link
   * @return name of the output port, or its key if the source or port does not exist
   */
  static String outputName(List<PrefabInstance> instances, PrefabLink link) {
    for (PrefabInstance instance : instances) {
      if (!instance.name().equals(link.source())) continue;
      Optional<Prefab> prefab = PrefabRegistry.find(instance.type());
      Optional<PrefabPort> port = PrefabWiring.outputPort(instances, link);
      if (prefab.isPresent() && port.isPresent()) {
        return prefab.get().portDisplayName(instance, port.get());
      }
    }
    return link.port();
  }

  private static Optional<String> portNote(PrefabInstance instance, PrefabPort port) {
    if (port instanceof PrefabAction action && action.needsPlayer(instance)) {
      return Optional.of("Runs for the player that caused the event.");
    }
    if (port instanceof ArtifactSlot<?> slot) {
      if (slot.property().isPresent()) {
        return Optional.of(
            "Replaces the property '" + slot.property().get().displayName() + "' while linked.");
      }
      if (slot.multiple()) {
        return Optional.of("Each value is named by its alias, e.g. for {alias} placeholders.");
      }
    }
    return Optional.empty();
  }

  private static Optional<PrefabWiring.Problem> problemOf(
      List<PrefabWiring.Problem> problems, String receiver, String port, PrefabLink link) {
    return problems.stream()
        .filter(
            problem ->
                problem.instance().equals(receiver)
                    && problem.port().equals(port)
                    && problem.link().equals(link))
        .min((a, b) -> b.severity().compareTo(a.severity()));
  }

  private static Color severityColor(PrefabWiring.Problem problem) {
    return problem.severity() == PrefabWiring.Severity.ERROR ? ERROR_COLOR : WARNING_COLOR;
  }

  private static Label wrapped(String text, int fontSize, Color color) {
    Label label = EditorTheme.label(text, fontSize, color);
    label.setWrap(true);
    label.setAlignment(Align.left);
    return label;
  }

  private static ImageButton iconButton(String icon, Color color, String tooltip, Runnable action) {
    ImageButton button =
        EditorTheme.iconButton(EditorTheme.icon(icon, color), EditorTheme.Variant.GHOST, tooltip);
    button.getImageCell().size(13f);
    if (action != null) {
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              action.run();
            }
          });
    }
    return button;
  }
}
