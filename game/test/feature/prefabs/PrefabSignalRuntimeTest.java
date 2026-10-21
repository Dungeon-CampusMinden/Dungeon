package feature.prefabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for routing events to linked actions and resolving artifact values at runtime. */
public class PrefabSignalRuntimeTest {

  private static final List<String> CALLS = new ArrayList<>();

  static {
    if (PrefabRegistry.find(Node.TYPE).isEmpty()) PrefabRegistry.register(new Node());
  }

  /** Prefab that records its action calls and raises its event from its action. */
  private static final class Node extends Prefab {
    static final String TYPE = "test-signal-node";
    static final PrefabEvent<Entity> PING = PrefabEvent.withPlayer("ping", "Ping");
    static final ArtifactOutput<Integer> NUMBER =
        ArtifactOutput.of("number", "Number", ArtifactType.INTEGER);
    static final ArtifactSlot<String> DIGITS =
        ArtifactSlot.single("digits", "Digits", ArtifactType.TEXT)
            .validatedBy(value -> value.matches("[0-9]+"), "only digits");
    static final ArtifactSlot<String> TEXTS =
        ArtifactSlot.multiple("texts", "Texts", ArtifactType.TEXT);
    static final PrefabAction RECORD =
        PrefabAction.forPlayer(
            "record",
            "Record",
            context ->
                CALLS.add(
                    context.instance().name()
                        + ":"
                        + context.player().map(player -> player.name()).orElse("-")));
    static final PrefabAction FORWARD =
        PrefabAction.of(
            "forward",
            "Forward",
            context -> {
              CALLS.add(context.instance().name() + ":forward");
              fire(
                  context.creationContext(),
                  context.instance(),
                  PING,
                  context.player().orElse(null));
            });

    Node() {
      super(TYPE, "Test Signal Node", PrefabSide.SERVER, List.of());
    }

    @Override
    protected List<PrefabPort> declaredPorts() {
      return List.of(PING, NUMBER, DIGITS, TEXTS, RECORD, FORWARD);
    }

    @Override
    public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
      return List.of();
    }
  }

  private DungeonLevel level;

  @BeforeEach
  void setUp() {
    CALLS.clear();
    level = new DungeonLevel(new LevelElement[][] {{LevelElement.FLOOR}}, DesignLabel.DEFAULT);
  }

  private void add(PrefabInstance instance) {
    level.prefabs().add(PrefabRegistry.require(Node.TYPE).normalize(instance));
  }

  private static PrefabInstance node(String name) {
    return PrefabRegistry.require(Node.TYPE).newInstance(name);
  }

  /** Raising an event runs every linked action with the causing player. */
  @Test
  public void eventsRunLinkedActions() {
    add(node("source"));
    add(node("a").withLink("record", new PrefabLink("source", "ping")));
    add(node("b").withLink("record", new PrefabLink("source", "ping")));
    add(node("inactive").withActive(false).withLink("record", new PrefabLink("source", "ping")));

    PrefabRuntime.fire(level, "source", Node.PING, new Entity("hero"));

    assertEquals(List.of("a:hero", "b:hero"), CALLS);
  }

  /** Prefabs triggering each other stop instead of recursing forever. */
  @Test
  public void actionLoopsStop() {
    add(node("a").withLink("forward", new PrefabLink("b", "ping")));
    add(node("b").withLink("forward", new PrefabLink("a", "ping")));

    PrefabRuntime.runAction(level, "a", "forward", Optional.empty());

    assertEquals(List.of("a:forward", "b:forward"), CALLS);
  }

  /** Linked values are converted, validated, aliased and drop out when they are cleared. */
  @Test
  public void artifactsResolveLinkedValues() {
    add(node("producer"));
    add(
        node("consumer")
            .withLink("digits", new PrefabLink("producer", "number"))
            .withLink("texts", new PrefabLink("producer", "number", "n")));
    PrefabCreationContext context = new PrefabCreationContext(level, PrefabSide.SERVER);
    PrefabInstance consumer = level.prefabs().get(1);
    ArtifactInput<String> digits = Prefab.input(context, consumer, Node.DIGITS);
    ArtifactInput<String> texts = Prefab.input(context, consumer, Node.TEXTS);
    int[] changes = {0};
    digits.onChange(() -> changes[0]++);

    assertTrue(digits.value().isEmpty());
    PrefabRuntime.publish(level, "producer", Node.NUMBER, 42);
    assertEquals(Optional.of("42"), digits.value());
    assertEquals(Map.of("n", "42"), texts.values());
    assertEquals(1, changes[0]);

    PrefabRuntime.publish(level, "producer", Node.NUMBER, -1);
    assertTrue(digits.value().isEmpty(), "rejected by the slot validator");

    PrefabRuntime.despawned(level, "producer");
    assertTrue(texts.values().isEmpty());
    assertEquals(3, changes[0]);
  }
}
