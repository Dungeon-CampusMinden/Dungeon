package wizard.runner.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.entities.CharacterClass;
import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.HintSeverity;
import foundation.definition.NumericInputDefinition;
import foundation.definition.TimerMode;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.room.model.FoundationRoom;
import foundation.room.model.RoomPoint;
import foundation.runtime.Authority;
import foundation.runtime.Projection.ProgressStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

class RoomDeriverTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String CUSTOM_ASSET = "assets/custom/3b50ea522803-foundation-note.png";
  private static final String BUILT_IN_CHEST = "objects/treasurechest/treasurechest.png";

  @TempDir Path temporaryDirectory;

  @Test
  void derivesCompleteSharedRoom() throws IOException {
    Path project = materializeExample();
    Map<String, String> before = snapshot(project);
    ValidationResult validation = new ProjectValidationPipeline().validate(project);

    FoundationRoom room = new RoomDeriver().derive(validation);

    assertTrue(validation.valid());
    assertEquals(1, room.definition().progression().riddleNodes().size());
    ComposedRiddleDefinition riddle =
        room.definition().progression().riddleNodes().getFirst().riddle();
    NumericInputDefinition numeric = (NumericInputDefinition) riddle.inputs().getFirst();
    assertEquals(
        List.of("res_keypad_code", "res_note_image"),
        riddle.informationSources().getFirst().resourceIds());
    assertEquals("s_desk", riddle.informationSources().getFirst().surfaceId());
    assertEquals("Der Hinweis liegt nicht offen im Raum.", riddle.hints().getFirst().text());
    assertEquals(HintSeverity.ORIENTATION, riddle.hints().getFirst().severity());
    assertEquals("3758", numeric.answer());
    assertEquals("s_exit_keypad", numeric.surfaceId());
    assertTrue(numeric.showDigitCount());
    assertEquals("Der Code besteht aus vier Ziffern.", riddle.hints().get(1).text());
    assertEquals(HintSeverity.APPROACH, riddle.hints().get(1).severity());
    assertEquals(HintSeverity.SOLUTION, riddle.hints().get(2).severity());
    assertEquals(30, room.definition().timer().limitMinutes());
    assertEquals(TimerMode.HARD, room.definition().timer().mode());
    assertEquals("s_exit_door", room.definition().door().id());
    assertEquals("n_exit", room.definition().exit().id());
    assertEquals("s_exit_door", room.definition().exit().doorId());

    ComposedPresentation composed = (ComposedPresentation) room.presentation().riddles().getFirst();
    var source = composed.informationSources().getFirst();
    assertEquals("s_desk", source.surfaceId());
    assertEquals("Schreibtisch", source.title());
    assertEquals("s_exit_keypad", composed.inputs().getFirst().surfaceId());
    assertEquals("Tür-Keypad", composed.inputs().getFirst().title());
    assertEquals(BUILT_IN_CHEST, source.runtimeAssetPath());
    assertTrue(source.resources().getFirst().runtimeAssetPath().isEmpty());
    assertEquals(CUSTOM_ASSET, source.resources().get(1).runtimeAssetPath().orElseThrow());
    assertEquals("Die markierten Zahlen ergeben 3758.", source.resources().getFirst().text());
    assertEquals("Papiernotiz", source.resources().get(1).title());
    assertEquals(
        List.of(
            "Die Ausgangstür ist verschlossen. Im Raum muss ein verwertbarer Hinweis versteckt sein."),
        room.presentation().introText());
    assertEquals(
        "Findet den Zugangscode und verlasst gemeinsam den Raum.", room.presentation().mission());
    assertEquals(CUSTOM_ASSET, room.assets().getFirst().logicalPath());
    assertEquals("image/png", room.assets().getFirst().mediaType());

    assertEquals(1, room.definition().minimumPlayers());
    assertEquals(4, room.definition().roster().slots().size());
    assertEquals(
        List.of(CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03),
        room.playableCharacterClasses());
    assertEquals(
        List.of(1, 2, 3, 4),
        room.definition().roster().slots().stream().map(slot -> slot.number()).toList());
    assertEquals(new RoomPoint(1, 1), room.layout().startPoint());
    assertEquals(
        List.of("r_exit_code"),
        room.layout().riddlePlacements().stream().map(placement -> placement.riddleId()).toList());
    assertEquals(
        List.of("s_desk", "s_exit_keypad"),
        room.layout().riddlePlacements().getFirst().components().stream()
            .map(component -> component.surfaceId())
            .toList());
    GamePresentation missingFailure =
        new GamePresentation(
            room.presentation().riddles(),
            room.presentation().introText(),
            room.presentation().mission(),
            room.presentation().successText(),
            Optional.empty());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new FoundationRoom(
                room.title(),
                room.seed(),
                room.inputSha256(),
                room.playableCharacterClasses(),
                room.definition(),
                missingFailure,
                room.layout(),
                room.assets()));
    assertEquals(before, snapshot(project));
  }

  @Test
  void derivesDeterministicCompleteRoomWithoutOutput() throws IOException {
    Path project = materializeExample();
    Map<String, String> before = snapshot(project);
    ProjectValidationPipeline pipeline = new ProjectValidationPipeline();
    RoomDeriver deriver = new RoomDeriver();

    FoundationRoom first = deriver.derive(pipeline.validate(project));
    FoundationRoom repeated = deriver.derive(pipeline.validate(project));

    assertEquals(first.inputSha256(), repeated.inputSha256());
    assertEquals(first.layout(), repeated.layout());
    assertEquals(first.definition(), repeated.definition());
    assertEquals(before, snapshot(project));
    assertEquals(List.of("assets", "deer.json"), children(project));
  }

  @Test
  void preservesExactStaggeredDagAndAlignsAllRiddleOrders() {
    Path project = Path.of("examples", "the-last-hour-v0.4").toAbsolutePath().normalize();
    ValidationResult validation = new ProjectValidationPipeline().validate(project);

    FoundationRoom room = new RoomDeriver().derive(validation);
    List<String> expectedOrder =
        List.of("r_connect_vent", "r_recover_access", "r_open_storage", "r_unlock_exit");

    assertTrue(validation.valid(), validation.issues().toString());
    assertEquals(
        expectedOrder,
        room.definition().progression().riddleNodes().stream()
            .map(node -> node.riddle().id())
            .toList());
    assertEquals(
        Set.of(
            "n_start->n_recover_access",
            "n_start->n_connect_vent",
            "n_recover_access->n_open_storage",
            "n_open_storage->n_unlock_exit",
            "n_connect_vent->n_unlock_exit",
            "n_unlock_exit->n_exit"),
        room.definition().progression().edges().stream()
            .map(edge -> edge.from() + "->" + edge.to())
            .collect(Collectors.toSet()));
    assertEquals(
        expectedOrder, room.presentation().riddles().stream().map(riddle -> riddle.id()).toList());
    assertEquals(
        expectedOrder,
        room.layout().riddlePlacements().stream().map(placement -> placement.riddleId()).toList());
  }

  @Test
  void normalizesPermutedProjectArraysIntoTheSameDefinitionAndInitialProjection()
      throws IOException {
    Path originalProject = materializeTheLastHour("original-dag");
    Path permutedProject = materializeTheLastHour("permuted-dag");
    ObjectNode permuted =
        (ObjectNode) MAPPER.readTree(permutedProject.resolve("deer.json").toFile());
    ObjectNode graph = (ObjectNode) permuted.required("riddleGraph");
    reverse((ArrayNode) graph.required("nodes"));
    reverse((ArrayNode) graph.required("edges"));
    reverse((ArrayNode) permuted.required("riddles"));
    Files.write(permutedProject.resolve("deer.json"), MAPPER.writeValueAsBytes(permuted));

    RoomDeriver deriver = new RoomDeriver();
    ProjectValidationPipeline pipeline = new ProjectValidationPipeline();
    FoundationRoom original = deriver.derive(pipeline.validate(originalProject));
    FoundationRoom reordered = deriver.derive(pipeline.validate(permutedProject));
    Authority originalAuthority = new Authority(original.definition());
    Authority reorderedAuthority = new Authority(reordered.definition());
    originalAuthority.connect("slot_1");
    originalAuthority.markSpawned("slot_1");
    reorderedAuthority.connect("slot_1");
    reorderedAuthority.markSpawned("slot_1");

    assertEquals(original.definition(), reordered.definition());
    assertEquals(original.layout(), reordered.layout());
    assertEquals(originalAuthority.projection(), reorderedAuthority.projection());
    assertEquals(
        List.of(
            ProgressStatus.ACTIVE,
            ProgressStatus.ACTIVE,
            ProgressStatus.LOCKED,
            ProgressStatus.LOCKED),
        originalAuthority.projection().riddles().stream().map(riddle -> riddle.status()).toList());
  }

  @Test
  void derivesBundledRuntimePathWithoutBoundCustomBytes() throws IOException {
    Path project = materializeExample();
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    ((ObjectNode) deer.required("assets").required(0)).put("path", "items/puzzle-piece.png");
    Files.write(project.resolve("deer.json"), MAPPER.writeValueAsBytes(deer));
    Files.delete(project.resolve(CUSTOM_ASSET));
    Files.delete(project.resolve("assets/custom"));
    Files.delete(project.resolve("assets"));

    FoundationRoom room =
        new RoomDeriver().derive(new ProjectValidationPipeline().validate(project));
    ComposedPresentation presentation =
        (ComposedPresentation) room.presentation().riddles().getFirst();

    assertEquals(
        "items/puzzle-piece.png",
        presentation
            .informationSources()
            .getFirst()
            .resources()
            .get(1)
            .runtimeAssetPath()
            .orElseThrow());
    assertTrue(room.assets().isEmpty());
  }

  private Path materializeExample() throws IOException {
    Path examples = Path.of("examples", "foundation-v0.4").toAbsolutePath().normalize();
    Path project = temporaryDirectory.resolve("project");
    Path custom = project.resolve("assets").resolve("custom");
    Files.createDirectories(custom);
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    Files.copy(
        examples.resolve(CUSTOM_ASSET.replace('/', File.separatorChar)),
        project.resolve(CUSTOM_ASSET.replace('/', File.separatorChar)));
    return project;
  }

  private Path materializeTheLastHour(final String directoryName) throws IOException {
    Path examples = Path.of("examples", "the-last-hour-v0.4").toAbsolutePath().normalize();
    Path project = Files.createDirectory(temporaryDirectory.resolve(directoryName));
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    return project;
  }

  private static void reverse(final ArrayNode array) {
    ArrayNode reversed = MAPPER.createArrayNode();
    for (int index = array.size() - 1; index >= 0; index--) {
      reversed.add(array.get(index));
    }
    array.removeAll();
    array.addAll(reversed);
  }

  private static Map<String, String> snapshot(final Path root) throws IOException {
    Map<String, String> result = new LinkedHashMap<>();
    try (var paths = Files.walk(root)) {
      for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
        result.put(root.relativize(path).toString(), hash(Files.readAllBytes(path)));
      }
    }
    return result;
  }

  private static List<String> children(final Path root) throws IOException {
    try (var paths = Files.list(root)) {
      return paths.map(path -> path.getFileName().toString()).sorted().toList();
    }
  }

  private static String hash(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
