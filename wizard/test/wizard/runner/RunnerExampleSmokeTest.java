package wizard.runner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import foundation.room.asset.RuntimeAssetBinder;
import foundation.room.level.RoomLevel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import wizard.runner.room.RoomDeriver;
import wizard.runner.validation.ProjectValidationPipeline;

/** Linear validation-to-client-bootstrap smoke test for the committed example. */
final class RunnerExampleSmokeTest {
  private static final String ASSET_NAME = "3b50ea522803-foundation-note.png";
  private static final String ASSET_PATH = "assets/custom/" + ASSET_NAME;

  @Test
  void embeddedCommittedExampleReachesJoinBootstrap() {
    Path committedProject = Path.of("examples", "foundation-v0.3").toAbsolutePath().normalize();
    DisposableRunnerRuntime.run(
        runtime -> {
          Path embeddedProject = EmbeddedProjectMaterializer.materialize(runtime);
          assertArrayEquals(
              assertDoesNotThrow(() -> Files.readAllBytes(committedProject.resolve("deer.json"))),
              assertDoesNotThrow(() -> Files.readAllBytes(embeddedProject.resolve("deer.json"))));
          assertArrayEquals(
              assertDoesNotThrow(() -> Files.readAllBytes(committedProject.resolve(ASSET_PATH))),
              assertDoesNotThrow(() -> Files.readAllBytes(embeddedProject.resolve(ASSET_PATH))));

          var validation = new ProjectValidationPipeline().validate(embeddedProject);
          assertTrue(validation.valid(), validation.issues().toString());
          var room = new RoomDeriver().derive(validation);
          RoomLevel level = RoomLevel.fromLayout(room.layout());
          assertTrue(level.namedPoints().containsKey("component_source_code_note"));
          assertTrue(level.namedPoints().containsKey("component_input_exit_code"));

          Map<String, byte[]> boundAssets = new LinkedHashMap<>();
          new RuntimeAssetBinder(
                  (logicalPath, bytes) -> boundAssets.put(logicalPath, bytes.clone()))
              .bind(room.assets());

          assertEquals(List.of(ASSET_PATH), List.copyOf(boundAssets.keySet()));
          assertArrayEquals(
              assertDoesNotThrow(() -> Files.readAllBytes(committedProject.resolve(ASSET_PATH))),
              boundAssets.get(ASSET_PATH));
          return null;
        });
  }
}
