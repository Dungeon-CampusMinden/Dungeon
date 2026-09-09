package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import java.util.Objects;

/**
 * Server-to-client: replaces the shader state for a scene, level, or depth-layer target.
 *
 * @param target the render target
 * @param depth the depth layer, only used for {@link Target#DEPTH_LAYER}
 * @param shaderComponent the complete shader state for the target
 */
public record ShaderTargetStateMessage(
    Target target, int depth, ShaderComponentState shaderComponent) implements NetworkMessage {

  /** Render target for a managed shader collection. */
  public enum Target {
    SCENE,
    LEVEL,
    DEPTH_LAYER
  }

  /** Validates a shader target state message. */
  public ShaderTargetStateMessage {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(shaderComponent, "shaderComponent");
    if (target != Target.DEPTH_LAYER && depth != 0) {
      throw new IllegalArgumentException("Only depth-layer targets may specify a depth");
    }
  }
}
