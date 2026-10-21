package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import feature.leveleditor.ui.ModeDetailsPanel;
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
  static final Color SIGNAL_COLOR = new Color(1f, 0.55f, 0.1f, 1f);

  /** Color of artifact links. */
  static final Color ARTIFACT_COLOR = new Color(0.25f, 0.65f, 1f, 1f);

  static final Color ERROR_COLOR = new Color(0.85f, 0.12f, 0.1f, 1f);
  static final Color WARNING_COLOR = new Color(0.8f, 0.45f, 0f, 1f);
  static final Color MUTED_COLOR = ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .6f);

  private static final int PORT_FONT_SIZE = 16;
  private static final int TEXT_FONT_SIZE = 14;
  private static final int HINT_FONT_SIZE = 12;
  private static final int BUTTON_FONT_SIZE = 13;
  private static final float BUTTON_HEIGHT = 26f;

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
    section(content, instance, prefab.ports(PrefabPort.Kind.EVENT), problems, "Events");
    List<PrefabPort> actions = prefab.ports(PrefabPort.Kind.ACTION);
    section(
        content,
        instance,
        actions.stream().filter(port -> !((PrefabAction) port).isLifecycle()).toList(),
        problems,
        "Actions");
    section(
        content,
        instance,
        actions.stream().filter(port -> ((PrefabAction) port).isLifecycle()).toList(),
        problems,
        "Lifecycle Actions");
    section(
        content,
        instance,
        prefab.ports(PrefabPort.Kind.ARTIFACT_OUTPUT),
        problems,
        "Artifact Outputs");
    section(
        content,
        instance,
        prefab.ports(PrefabPort.Kind.ARTIFACT_SLOT),
        problems,
        "Artifact Inputs");
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
    content.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(12f).row();
    content
        .add(Scene2dElementFactory.createLabel(title, 18, ModeDetailsPanel.TEXT_COLOR))
        .left()
        .padTop(4f)
        .padLeft(4f)
        .row();
    for (PrefabPort port : ports) content.add(portBlock(instance, port, problems)).padTop(6f).row();
  }

  private Table portBlock(
      PrefabInstance instance, PrefabPort port, List<PrefabWiring.Problem> problems) {
    Table block = new Table();
    block.defaults().growX().minWidth(0f);

    Table header = new Table();
    Label name =
        Scene2dElementFactory.createLabel(
            portTitle(port), PORT_FONT_SIZE, ModeDetailsPanel.TEXT_COLOR);
    name.setEllipsis(true);
    header.add(name).growX().minWidth(0f).left().padLeft(4f);
    if (port.kind().isSignal()) {
      TextButton test =
          button(
              port.kind() == PrefabPort.Kind.EVENT ? "Send" : "Run",
              () -> host.test(instance.name(), port));
      test.setDisabled(!host.canTest());
      header.add(test).width(54f).height(BUTTON_HEIGHT);
    }
    block.add(header).row();
    portNote(port).ifPresent(note -> block.add(wrapped(note, HINT_FONT_SIZE, MUTED_COLOR)).row());

    Color linkColor = port.kind().isSignal() ? SIGNAL_COLOR : ARTIFACT_COLOR;
    if (port.kind().isInput()) {
      for (PrefabLink link : instance.links(port)) {
        Optional<PrefabWiring.Problem> problem =
            problemOf(problems, instance.name(), port.key(), link);
        String output =
            PrefabWiring.outputPort(host.instances(), link)
                .map(PrefabPort::displayName)
                .orElse(link.port());
        block
            .add(
                linkRow(
                    "<- " + link.source() + " > " + output,
                    problem.isPresent() ? severityColor(problem.get()) : linkColor,
                    link.source(),
                    () -> host.removeLink(instance.name(), port.key(), link),
                    port instanceof ArtifactSlot<?> slot && slot.multiple()
                        ? aliasField(instance.name(), port.key(), link)
                        : null))
            .padTop(2f)
            .row();
        problem.ifPresent(
            p -> block.add(wrapped(p.message(), HINT_FONT_SIZE, severityColor(p))).row());
      }
    } else {
      for (PrefabWiring.Connection connection :
          PrefabWiring.outgoing(host.instances(), instance.name(), port.key())) {
        Optional<PrefabWiring.Problem> problem =
            problemOf(
                problems,
                connection.receiver().name(),
                connection.input().key(),
                connection.link());
        block
            .add(
                linkRow(
                    "-> " + connection.receiver().name() + " > " + connection.input().displayName(),
                    problem.isPresent() ? severityColor(problem.get()) : linkColor,
                    connection.receiver().name(),
                    () ->
                        host.removeLink(
                            connection.receiver().name(),
                            connection.input().key(),
                            connection.link()),
                    null))
            .padTop(2f)
            .row();
        problem.ifPresent(
            p -> block.add(wrapped(p.message(), HINT_FONT_SIZE, severityColor(p))).row());
      }
    }

    Table actions = new Table();
    actions.defaults().growX().uniformX().height(BUTTON_HEIGHT).pad(0f, 1f, 0f, 1f);
    boolean replaces =
        port instanceof ArtifactSlot<?> slot && !slot.multiple() && !instance.links(port).isEmpty();
    LinkEnd end = new LinkEnd(instance.name(), port);
    TextButton connect =
        Scene2dElementFactory.createButton(
            replaces ? "Replace..." : "+ Link...", "default", BUTTON_FONT_SIZE);
    connect.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            host.connect(end, connect);
          }
        });
    actions.add(connect);
    actions.add(button("Pick in World", () -> host.pickInWorld(end)));
    block.add(actions).padTop(3f).row();
    return block;
  }

  private Table linkRow(
      String text, Color color, String other, Runnable remove, TextField aliasField) {
    Table row = new Table();
    if (aliasField != null) row.add(aliasField).width(86f).height(BUTTON_HEIGHT).padRight(4f);
    Label label = Scene2dElementFactory.createLabel(text, TEXT_FONT_SIZE, color);
    label.setEllipsis(true);
    row.add(label).growX().minWidth(0f).left().padLeft(aliasField == null ? 10f : 0f);
    row.add(button("Go", () -> host.focus(other))).width(36f).height(BUTTON_HEIGHT);
    TextButton removeButton = button("X", remove, "red-outline");
    row.add(removeButton).width(30f).height(BUTTON_HEIGHT).padLeft(2f);
    return row;
  }

  private TextField aliasField(String receiver, String port, PrefabLink link) {
    TextField field = Scene2dElementFactory.createTextField(link.alias(), 14);
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
    return field;
  }

  private static String portTitle(PrefabPort port) {
    return switch (port) {
      case ArtifactOutput<?> output ->
          port.displayName() + " (" + output.type().displayName() + ")";
      case ArtifactSlot<?> slot ->
          port.displayName()
              + " ("
              + slot.type().displayName()
              + (slot.multiple() ? ", any number" : "")
              + ")";
      default -> port.displayName();
    };
  }

  private static Optional<String> portNote(PrefabPort port) {
    if (port instanceof PrefabAction action && action.needsPlayer()) {
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
    Label label = Scene2dElementFactory.createLabel(text, fontSize, color);
    label.setWrap(true);
    label.setAlignment(Align.left);
    return label;
  }

  private static TextButton button(String text, Runnable action) {
    return button(text, action, "default");
  }

  private static TextButton button(String text, Runnable action, String style) {
    TextButton button = Scene2dElementFactory.createButton(text, style, BUTTON_FONT_SIZE);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }
}
