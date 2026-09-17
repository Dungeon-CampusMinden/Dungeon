package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.ShaderList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.display.DisplayTextStatusShader;
import rooms.systemRecovery.modules.display.DoorLabelComponent;

/** Verifies live progress metadata without initializing graphics on the server. */
class DoorLabelComponentTest {
  @Test
  void replacesOutlineAndRemovesWholeSpriteTint() {
    Entity label = new Entity();
    DrawComponent draw = mock(DrawComponent.class);
    ShaderList shaders = new ShaderList();
    shaders.add("doorLabelStatus", new OutlineShader(1));
    when(draw.shaders()).thenReturn(shaders);
    label.add(draw);
    try (var game = mockStatic(Game.class)) {
      game.when(Game::isHeadless).thenReturn(false);
      DoorLabelComponent.updateAppearance(label, false);
      var shader = assertInstanceOf(DisplayTextStatusShader.class, shaders.get("doorLabelStatus"));
      assertEquals(0, shader.padding());
      DoorLabelComponent.updateAppearance(label, true);
      assertSame(shader, shaders.get("doorLabelStatus"));
      verify(draw, times(2)).tintColor(-1);
    }
  }

  @Test
  void exportsInitialAndCompletedStateForSnapshotsAndLateJoiners() {
    AtomicBoolean completed = new AtomicBoolean();
    Entity label = new Entity();
    label.add(new DoorLabelComponent(completed::get));
    var metadata = new HashMap<String, String>();
    DoorLabelComponent.appendMetadata(label, metadata);
    assertEquals("false", metadata.get(DoorLabelComponent.METADATA_KEY));
    completed.set(true);
    DoorLabelComponent.appendMetadata(label, metadata);
    assertEquals("true", metadata.get(DoorLabelComponent.METADATA_KEY));
    var lateJoinMetadata = new HashMap<String, String>();
    DoorLabelComponent.appendMetadata(label, lateJoinMetadata);
    assertEquals(metadata, lateJoinMetadata);
    try (var game = mockStatic(Game.class)) {
      game.when(Game::isHeadless).thenReturn(true);
      assertDoesNotThrow(() -> DoorLabelComponent.applyMetadata(label, metadata));
    }
  }
}
