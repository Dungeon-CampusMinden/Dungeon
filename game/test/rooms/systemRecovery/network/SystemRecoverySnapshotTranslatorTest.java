package rooms.systemRecovery.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.utils.Point;
import engine.utils.components.draw.shader.EnergyFillShader;
import feature.interaction.InteractionComponent;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.shader.ShaderComponent;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.entities.EntityFactory;

/** Tests synchronization of System Recovery questlog and entity render state. */
public class SystemRecoverySnapshotTranslatorTest {

  /** Questlog tabs and entry metadata survive a server-to-client roundtrip. */
  @Test
  void questLogMetadataPreservesTabsAndEntries() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add(
        "Rätsel 1: Energieversorgung",
        new QuestLogEntry(
            "Fülle das Energie-Array.", 17, false, QuestLogEntry.DEFAULT_OWNER, false));

    Map<String, String> metadata = SystemRecoverySnapshotTranslator.questLogMetadata(questLog);

    QuestLogComponent restored =
        SystemRecoverySnapshotTranslator.questLogFromMetadata(metadata).orElseThrow();
    QuestLogEntry entry = restored.get("Rätsel 1: Energieversorgung").get(0);
    assertEquals("Fülle das Energie-Array.", entry.text());
    assertEquals(17, entry.timestamp());
    assertTrue(metadata.containsKey(SystemRecoveryEntitySpawnStrategy.METADATA_QUESTLOG_ENTRIES));
  }

  /** Custom System Recovery spawn metadata must not discard synchronized entity shaders. */
  @Test
  void entitySpawnMetadataPreservesShaderComponent() {
    Entity energyCrate = EntityFactory.cryoBox(new Point(0, 0), false);
    energyCrate.add(new InteractionComponent());
    energyCrate.add(
        new ShaderComponent(
            "energieShader", 0, new EnergyFillShader(0.4f, Color.BLUE)));

    EntitySpawnEvent event =
        new SystemRecoveryEntitySpawnStrategy().buildSpawnEvent(energyCrate).orElseThrow();

    assertNotNull(event.shaderComponent());
    assertEquals("energy_fill", event.shaderComponent().shaders().get(0).type());
    assertEquals(
        "0.4", event.shaderComponent().shaders().get(0).properties().get("fillPercentage"));
  }
}
