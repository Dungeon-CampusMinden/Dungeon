package engine.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import engine.network.messages.s2c.ShaderComponentState;
import feature.shader.ShaderComponent;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.shaders.SystemRecoveryAlarmShader;

/** Verifies that room-specific shaders used by multiplayer can cross the shared shader codec. */
class ShaderComponentCodecTest {

  @Test
  void systemRecoveryAlarmShaderCanBeEncodedAndRestored() {
    ShaderComponent source =
        new ShaderComponent("systemRecoveryAlarm", 0, new SystemRecoveryAlarmShader());

    ShaderComponentState state = ShaderComponentCodec.toState(source);
    assertEquals("system_recovery_alarm", state.shaders().getFirst().type());

    ShaderComponent restored = ShaderComponentCodec.fromState(state);
    assertInstanceOf(SystemRecoveryAlarmShader.class, restored.shaders().getFirst().shader());
    assertEquals("systemRecoveryAlarm", restored.shaders().getFirst().identifier());
  }
}
