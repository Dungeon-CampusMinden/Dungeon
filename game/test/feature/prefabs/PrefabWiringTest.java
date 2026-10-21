package feature.prefabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.level.DungeonLevel;
import engine.level.loader.LevelParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Tests for prefab ports, links and their persistence. */
public class PrefabWiringTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    if (PrefabRegistry.find(Producer.TYPE).isEmpty()) PrefabRegistry.register(new Producer());
    if (PrefabRegistry.find(Consumer.TYPE).isEmpty()) PrefabRegistry.register(new Consumer());
  }

  /** Prefab producing a signal, a player event and a text value. */
  private static final class Producer extends Prefab {
    static final String TYPE = "test-producer";
    static final PrefabEvent<Void> SIGNAL = PrefabEvent.signal("signal", "Signal");
    static final PrefabEvent<Entity> PLAYER = PrefabEvent.withPlayer("player", "Player");
    static final ArtifactOutput<String> TEXT = ArtifactOutput.of("text", "Text", ArtifactType.TEXT);
    static final ArtifactOutput<Integer> NUMBER =
        ArtifactOutput.of("number", "Number", ArtifactType.INTEGER);

    Producer() {
      super(TYPE, "Test Producer", PrefabSide.SERVER, List.of());
    }

    @Override
    protected List<PrefabPort> declaredPorts() {
      return List.of(SIGNAL, PLAYER, TEXT, NUMBER);
    }

    @Override
    public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
      return List.of();
    }
  }

  /** Prefab consuming signals and values, and producing a value itself. */
  private static final class Consumer extends Prefab {
    static final String TYPE = "test-consumer";
    static final PrefabAction PLAIN = PrefabAction.of("plain", "Plain", context -> {});
    static final PrefabAction FOR_PLAYER =
        PrefabAction.forPlayer("for-player", "For Player", context -> {});
    static final ArtifactSlot<String> ONE = ArtifactSlot.single("one", "One", ArtifactType.TEXT);
    static final ArtifactSlot<String> MANY =
        ArtifactSlot.multiple("many", "Many", ArtifactType.TEXT);
    static final ArtifactSlot<Integer> NUMBER =
        ArtifactSlot.single("number", "Number", ArtifactType.INTEGER);
    static final ArtifactOutput<String> OUT = ArtifactOutput.of("out", "Out", ArtifactType.TEXT);

    Consumer() {
      super(TYPE, "Test Consumer", PrefabSide.SERVER, List.of());
    }

    @Override
    protected List<PrefabPort> declaredPorts() {
      return List.of(PLAIN, FOR_PLAYER, ONE, MANY, NUMBER, OUT);
    }

    @Override
    public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
      return List.of();
    }
  }

  private static PrefabInstance producer(String name) {
    return PrefabRegistry.require(Producer.TYPE).newInstance(name);
  }

  private static PrefabInstance consumer(String name) {
    return PrefabRegistry.require(Consumer.TYPE).newInstance(name);
  }

  private static PrefabInstance normalized(PrefabInstance instance) {
    return PrefabRegistry.require(instance.type()).normalize(instance);
  }

  /** Every prefab has the lifecycle actions after its declared ports. */
  @Test
  public void portsIncludeLifecycleActions() {
    List<PrefabPort> ports = PrefabRegistry.require(Producer.TYPE).ports();
    assertEquals(
        List.of("signal", "player", "text", "number", "activate", "deactivate", "reset"),
        ports.stream().map(PrefabPort::key).toList());
  }

  /** Links of unknown ports and links into output ports are rejected. */
  @Test
  public void normalizeRejectsUnknownAndOutputPorts() {
    PrefabInstance unknown = consumer("c").withLink("missing", new PrefabLink("p", "signal"));
    assertThrows(IllegalArgumentException.class, () -> normalized(unknown));
    PrefabInstance output = consumer("c").withLink("out", new PrefabLink("p", "text"));
    assertThrows(IllegalArgumentException.class, () -> normalized(output));
  }

  /** Duplicate links are merged, aliases are derived and made unique, single slots are checked. */
  @Test
  public void normalizeLinks() {
    PrefabInstance instance =
        consumer("c")
            .withLink("plain", new PrefabLink("p", "signal"))
            .withLink("plain", new PrefabLink("p", "signal"))
            .withLink("many", new PrefabLink("area/code", "text"))
            .withLink("many", new PrefabLink("other/code", "text"));
    PrefabInstance result = normalized(instance);
    assertEquals(List.of(new PrefabLink("p", "signal")), result.links("plain"));
    assertEquals(
        List.of(
            new PrefabLink("area/code", "text", "code"),
            new PrefabLink("other/code", "text", "code_2")),
        result.links("many"));

    PrefabInstance twoValues =
        consumer("c")
            .withLink("one", new PrefabLink("a", "text"))
            .withLink("one", new PrefabLink("b", "text"));
    assertThrows(IllegalArgumentException.class, () -> normalized(twoValues));
    PrefabInstance self = consumer("c").withLink("one", new PrefabLink("c", "out"));
    assertThrows(IllegalArgumentException.class, () -> normalized(self));
  }

  /** Problems are reported for missing sources, unknown ports, type mismatches and cycles. */
  @Test
  public void problems() {
    List<PrefabInstance> level =
        List.of(
            producer("p"),
            consumer("a")
                .withLink("plain", new PrefabLink("missing", "signal"))
                .withLink("for-player", new PrefabLink("p", "signal"))
                .withLink("number", new PrefabLink("p", "text"))
                .withLink("one", new PrefabLink("b", "out")),
            consumer("b")
                .withLink("plain", new PrefabLink("p", "nope"))
                .withLink("one", new PrefabLink("a", "out")));
    List<PrefabWiring.Problem> problems = PrefabWiring.problems(level);
    assertTrue(has(problems, "a", "plain", PrefabWiring.Severity.ERROR));
    assertTrue(has(problems, "a", "for-player", PrefabWiring.Severity.WARNING));
    assertTrue(has(problems, "a", "number", PrefabWiring.Severity.ERROR));
    assertTrue(has(problems, "a", "one", PrefabWiring.Severity.ERROR));
    assertTrue(has(problems, "b", "plain", PrefabWiring.Severity.ERROR));
    assertTrue(has(problems, "b", "one", PrefabWiring.Severity.ERROR));

    List<String> warnings = new ArrayList<>();
    List<PrefabInstance> cleaned = PrefabWiring.withoutBrokenLinks(level, warnings::add);
    assertEquals(5, warnings.size());
    assertEquals(List.of("for-player"), List.copyOf(cleaned.get(1).inputs().keySet()));
    assertTrue(cleaned.get(2).inputs().isEmpty());
  }

  /** Integer values may be used as text, text may not be used as integer. */
  @Test
  public void artifactTypeConversions() {
    assertTrue(ArtifactType.INTEGER.convertibleTo(ArtifactType.TEXT));
    assertTrue(ArtifactType.INTEGER.convertibleTo(ArtifactType.FLOAT));
    assertFalse(ArtifactType.TEXT.convertibleTo(ArtifactType.INTEGER));
    assertEquals("42", ArtifactType.INTEGER.convert(42, ArtifactType.TEXT).orElseThrow());
    assertEquals(42f, ArtifactType.INTEGER.convert(42, ArtifactType.FLOAT).orElseThrow());
    assertTrue(ArtifactType.TEXT.convert("x", ArtifactType.INTEGER).isEmpty());
  }

  /** Renaming and deleting instances keeps links consistent. */
  @Test
  public void renameAndRemoveLinks() {
    List<PrefabInstance> level =
        List.of(
            producer("new"),
            consumer("c")
                .withLink("plain", new PrefabLink("old", "signal"))
                .withLink("one", new PrefabLink("old", "text")));
    List<PrefabInstance> renamed = PrefabWiring.renameSources(level, Map.of("old", "new"));
    assertEquals(List.of(new PrefabLink("new", "signal")), renamed.get(1).links("plain"));
    assertEquals(2, PrefabWiring.countLinksTo(renamed, Set.of("new")));
    List<PrefabInstance> removed = PrefabWiring.removeLinksTo(renamed, Set.of("new"));
    assertTrue(removed.get(1).inputs().isEmpty());
  }

  /** Artifact producers are spawned before their consumers, the rest keeps its order. */
  @Test
  public void spawnOrderPutsProducersFirst() {
    List<PrefabInstance> level =
        List.of(
            consumer("first").withLink("one", new PrefabLink("second", "out")),
            consumer("second").withLink("one", new PrefabLink("p", "text")),
            consumer("unrelated"),
            producer("p"));
    assertEquals(
        List.of("unrelated", "p", "second", "first"),
        PrefabWiring.spawnOrder(level).stream().map(PrefabInstance::name).toList());
    assertTrue(PrefabWiring.wouldCreateArtifactCycle(level, "first", "second"));
    assertFalse(PrefabWiring.wouldCreateArtifactCycle(level, "unrelated", "first"));
  }

  /** The JSON record omits default values and keeps the active flag and links. */
  @Test
  public void codecRoundTrip() {
    PrefabInstance instance =
        normalized(
            consumer("c")
                .withActive(false)
                .withLink("many", new PrefabLink("p", "text", "code"))
                .withLink("plain", new PrefabLink("p", "signal")));
    ObjectNode json = PrefabInstanceCodec.encode(MAPPER, instance);
    assertFalse(json.get("active").booleanValue());
    assertEquals("code", json.get("inputs").get("many").get(0).get("alias").asText());
    assertEquals(instance, PrefabInstanceCodec.decode(json, warning -> {}));

    ObjectNode plain = PrefabInstanceCodec.encode(MAPPER, normalized(consumer("d")));
    assertFalse(plain.has("active"));
    assertFalse(plain.has("inputs"));
  }

  /** Version 3 files are read, files are written as version 5 and broken links are dropped. */
  @Test
  public void levelFilesKeepLinks() {
    String v3 =
        """
        {
          "version": 3,
          "design": "DEFAULT",
          "layout": ["FF"],
          "startTiles": [],
          "namedPoints": {},
          "decorations": [],
          "prefabs": [
            { "name": "p", "type": "test-producer", "properties": {} },
            { "name": "c", "type": "test-consumer", "properties": {}, "active": false,
              "inputs": {
                "plain": [ { "source": "p", "port": "signal" } ],
                "one": [ { "source": "gone", "port": "text" } ],
                "unknown": [ { "source": "p", "port": "signal" } ]
              } }
          ]
        }
        """;
    DungeonLevel level = LevelParser.parseLevel(v3, "prefab-wiring-test");
    PrefabInstance consumer = level.prefabs().get(1);
    assertFalse(consumer.active());
    assertEquals(Set.of("plain"), consumer.inputs().keySet());

    String saved = LevelParser.serializeLevel(level);
    assertTrue(saved.contains("\"version\" : 5"));
    DungeonLevel reloaded = LevelParser.parseLevel(saved, "prefab-wiring-test");
    assertEquals(level.prefabs(), reloaded.prefabs());
  }

  private static boolean has(
      List<PrefabWiring.Problem> problems,
      String instance,
      String port,
      PrefabWiring.Severity severity) {
    return problems.stream()
        .anyMatch(
            problem ->
                problem.instance().equals(instance)
                    && problem.port().equals(port)
                    && problem.severity() == severity);
  }
}
