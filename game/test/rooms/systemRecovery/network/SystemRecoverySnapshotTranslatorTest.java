package rooms.systemRecovery.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.systems.LevelSystem;
import engine.utils.Point;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.OutlineShader;
import feature.interaction.InteractionComponent;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.shader.ShaderComponent;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.entities.EnergyEntityFactory;
import rooms.systemRecovery.entities.ModuleEntityFactory;
import rooms.systemRecovery.entities.ScannerEntityFactory;
import rooms.systemRecovery.entities.SystemCoreEntityFactory;
import rooms.systemRecovery.entities.TransportEntityFactory;

/** Tests synchronization of System Recovery questlog and entity render state. */
public class SystemRecoverySnapshotTranslatorTest {

  /** Large questlogs are never serialized as initial entity-spawn metadata. */
  @Test
  void largeQuestLogIsExcludedFromEntitySpawnPayload() {
    QuestLogComponent questLog = new QuestLogComponent();
    for (int i = 0; i < 50; i++) {
      questLog.add(
          "Rätsel " + i,
          new QuestLogEntry("A long quest instruction " + "x".repeat(100), i, false, "AI", false));
    }
    Entity questLogEntity = new Entity("shared-quest-log");
    questLogEntity.add(questLog);

    assertFalse(
        new SystemRecoveryEntitySpawnStrategy().buildSpawnEvent(questLogEntity).isPresent());
    assertTrue(new SystemRecoverySnapshotTranslator().snapshotMetadata(questLogEntity).isEmpty());
  }

  /** Custom System Recovery spawn metadata must not discard synchronized entity shaders. */
  @Test
  void entitySpawnMetadataPreservesShaderComponent() {
    Entity energyCrate = EnergyEntityFactory.cryoBox(new Point(0, 0), false);
    energyCrate.add(new InteractionComponent());
    energyCrate.add(
        new ShaderComponent("energieShader", 0, new EnergyFillShader(0.4f, Color.BLUE)));

    EntitySpawnEvent event =
        new SystemRecoveryEntitySpawnStrategy().buildSpawnEvent(energyCrate).orElseThrow();

    assertNotNull(event.shaderComponent());
    assertEquals("energy_fill", event.shaderComponent().shaders().get(0).type());
    assertEquals(
        "0.4", event.shaderComponent().shaders().get(0).properties().get("fillPercentage"));
  }

  /** The final-riddle completion shader is projected from a compact synchronized stage value. */
  @Test
  void systemCoreStageAppliesCompletionOutlineAtTheRequiredStage() {
    Entity module = SystemCoreEntityFactory.moduleEntry(new Point(0, 0), 0, "CPU");

    SystemCoreVisualSync.applyCompletionMetadata(
        module, Map.of(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_STAGE, "1"));
    assertNull(module.fetch(DrawComponent.class).orElseThrow().shaders().get("systemCoreComplete"));

    SystemCoreVisualSync.applyCompletionMetadata(
        module, Map.of(SystemRecoveryEntitySpawnStrategy.METADATA_SYSTEM_CORE_STAGE, "2"));
    assertNotNull(
        module.fetch(DrawComponent.class).orElseThrow().shaders().get("systemCoreComplete"));
    assertTrue(
        module.fetch(DrawComponent.class).orElseThrow().shaders().get("systemCoreComplete")
            instanceof OutlineShader);
    assertEquals(0x66FF66FF, module.fetch(DrawComponent.class).orElseThrow().tintColor());
  }

  /** A late join after sorting still applies package colors when no comparison is active. */
  @Test
  void completedConveyorSnapshotAppliesAndRefreshesPackageColors() {
    Game.removeAllEntities();
    try {
      Entity packageEntity = TransportEntityFactory.packageEntity(new Point(0, 0), 15);
      Game.add(packageEntity);
      ConveyorSortVisualSync sync = new ConveyorSortVisualSync();
      Map<String, String> metadata =
          Map.of(
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_LEFT_PACKAGE, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_RIGHT_PACKAGE, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_SCANNER, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_PACKAGES, packageEntity.id() + ":15");

      sync.apply(metadata);

      HueRemapShader initial =
          (HueRemapShader)
              packageEntity
                  .fetch(DrawComponent.class)
                  .orElseThrow()
                  .shaders()
                  .get("beltPackageColor");
      assertNotNull(initial, "the no-comparison snapshot must still restore final package colors");
      assertEquals(0.0f, initial.targetHue());

      sync.apply(
          Map.of(
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_LEFT_PACKAGE, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_RIGHT_PACKAGE, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_SCANNER, "-1",
              SystemRecoveryEntitySpawnStrategy.METADATA_BELT_PACKAGES,
                  packageEntity.id() + ":40"));

      HueRemapShader refreshed =
          (HueRemapShader)
              packageEntity
                  .fetch(DrawComponent.class)
                  .orElseThrow()
                  .shaders()
                  .get("beltPackageColor");
      assertEquals(0.6f, refreshed.targetHue());
      sync.reset();
      assertNull(
          packageEntity.fetch(DrawComponent.class).orElseThrow().shaders().get("beltPackageColor"));
    } finally {
      Game.removeAllEntities();
    }
  }

  /** A new scanner with an unchanged fault value still reconstructs its local GPU outline. */
  @Test
  void restartedModuleScanReappliesFaultOutlineToTheNewGpu() {
    Game.removeAllEntities();
    try {
      ModuleScannerVisualSync sync = new ModuleScannerVisualSync();
      Entity firstScanner = ScannerEntityFactory.moduleScanner(new Point(0, 0), 1f);
      Entity firstGpu = ModuleEntityFactory.moduleChip(new Point(1, 0), "GPU");
      Game.add(firstScanner);
      Game.add(firstGpu);
      Map<String, String> faultMetadata =
          Map.of(
              SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_RUNNING, "false",
              SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SCAN_FAULT, "true");

      sync.apply(firstScanner, faultMetadata);
      assertTrue(hasFaultOutline(firstGpu));

      Game.remove(firstScanner);
      Game.remove(firstGpu);
      Entity restartedScanner = ScannerEntityFactory.moduleScanner(new Point(0, 0), 1f);
      Entity restartedGpu = ModuleEntityFactory.moduleChip(new Point(1, 0), "GPU");
      Game.add(restartedScanner);
      Game.add(restartedGpu);

      sync.apply(restartedScanner, faultMetadata);

      assertTrue(hasFaultOutline(restartedGpu));
    } finally {
      Game.removeAllEntities();
    }
  }

  /** A late-join cell snapshot restores its filled value on the local level tile. */
  @Test
  void storageCellSnapshotRestoresTheFilledCoordinate() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new LevelSystem());
    Game.currentLevel(
        new DungeonLevel(
            new LevelElement[][] {
              {LevelElement.FLOOR, LevelElement.FLOOR},
              {LevelElement.FLOOR, LevelElement.FLOOR}
            },
            DesignLabel.DEFAULT));
    try {
      Entity cell = new Entity("storage_matrix_cell_1_1");
      cell.add(new PositionComponent(new Point(1, 1)));

      new StorageVisualSync()
          .apply(
              cell,
              Map.of(
                  SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_STATE, "filled",
                  SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_VALUE, "2"));

      assertEquals(0xFFB000FF, Game.tileAt(new Point(1, 1)).orElseThrow().tintColor());
    } finally {
      Game.currentLevel(null);
      Game.removeAllSystems();
      Game.removeAllEntities();
    }
  }

  /** A synchronized target cell keeps the color assigned to its required value. */
  @Test
  void storageCellSnapshotRestoresTheTargetValueColor() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new LevelSystem());
    Game.currentLevel(
        new DungeonLevel(
            new LevelElement[][] {
              {LevelElement.FLOOR, LevelElement.FLOOR},
              {LevelElement.FLOOR, LevelElement.FLOOR}
            },
            DesignLabel.DEFAULT));
    try {
      Entity cell = new Entity("storage_matrix_cell_0_0_target");
      cell.add(new PositionComponent(new Point(1, 1)));

      new StorageVisualSync()
          .apply(
              cell,
              Map.of(
                  SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_STATE, "target",
                  SystemRecoveryEntitySpawnStrategy.METADATA_STORAGE_CELL_VALUE, "3"));

      assertEquals(0xFF00FFFF, Game.tileAt(new Point(1, 1)).orElseThrow().tintColor());
    } finally {
      Game.currentLevel(null);
      Game.removeAllSystems();
      Game.removeAllEntities();
    }
  }

  private static boolean hasFaultOutline(Entity entity) {
    return entity.fetch(DrawComponent.class).orElseThrow().shaders().get("moduleScannerFault")
        instanceof OutlineShader;
  }
}
