package wizard.runner.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import contrib.entities.CharacterClass;
import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.HintSeverity;
import foundation.definition.NumericInputDefinition;
import foundation.definition.TimerMode;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.room.model.FoundationRoom;
import foundation.room.model.RoomPoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
    assertEquals(1, room.sections().size());
    ComposedRiddleDefinition riddle =
        (ComposedRiddleDefinition) room.sections().getFirst().riddles().getFirst();
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
    assertEquals(30, room.timer().limitMinutes());
    assertEquals(TimerMode.HARD, room.timer().mode());
    assertEquals("s_exit_door", room.door().id());
    assertEquals("n_exit", room.exit().id());
    assertEquals("s_exit_door", room.exit().doorId());

    ComposedPresentation composed = (ComposedPresentation) room.presentation().riddles().getFirst();
    var source = composed.informationSources().getFirst();
    assertEquals("s_desk", source.surfaceId());
    assertEquals("s_exit_keypad", composed.inputs().getFirst().surfaceId());
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

    assertEquals(1, room.minimumPlayers());
    assertEquals(4, room.maximumPlayers());
    assertEquals(
        List.of(CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03),
        room.playableCharacterClasses());
    assertEquals(1, room.createDefinition().minimumPlayers());
    assertEquals(
        List.of(1, 2, 3, 4),
        room.createDefinition().roster().slots().stream().map(slot -> slot.number()).toList());
    assertEquals(new RoomPoint(1, 1), room.layout().startPoint());
    assertEquals(
        List.of("r_exit_code"),
        room.layout().riddlePlacements().stream().map(placement -> placement.riddleId()).toList());
    assertEquals(
        List.of("s_desk", "s_exit_keypad"),
        room.layout().riddlePlacements().getFirst().components().stream()
            .map(component -> component.surfaceId())
            .toList());
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
    assertEquals(first.sections(), repeated.sections());
    assertEquals(before, snapshot(project));
    assertEquals(List.of("assets", "deer.json"), children(project));
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
    Path examples = Path.of("examples", "foundation-v0.3").toAbsolutePath().normalize();
    Path project = temporaryDirectory.resolve("project");
    Path custom = project.resolve("assets").resolve("custom");
    Files.createDirectories(custom);
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    Files.copy(
        examples.resolve(CUSTOM_ASSET.replace('/', java.io.File.separatorChar)),
        project.resolve(CUSTOM_ASSET.replace('/', java.io.File.separatorChar)));
    return project;
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
