package engine.network.codec;

import engine.network.messages.s2c.ShaderComponentState;
import engine.network.messages.s2c.ShaderComponentState.ShaderEntryState;
import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.LevelHideShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.PassthroughShader;
import engine.utils.components.draw.shader.ShineShader;
import feature.shader.ShaderComponent;
import java.util.List;

/** Converts synchronized shader components to and from their network representation. */
public final class ShaderComponentCodec {
  private static final String TYPE_OUTLINE = "outline";
  private static final String TYPE_COLOR_GRADE = "color_grade";
  private static final String TYPE_HUE_REMAP = "hue_remap";
  private static final String TYPE_ENERGY_FILL = "energy_fill";
  private static final String TYPE_SHINE = "shine";
  private static final String TYPE_PASSTHROUGH = "passthrough";
  private static final String TYPE_LEVEL_HIDE = "level_hide";

  private ShaderComponentCodec() {}

  /**
   * Converts a runtime shader component into an immutable network state.
   *
   * @param component runtime component, or null
   * @return network state, or null when no component is supplied
   */
  public static ShaderComponentState toState(ShaderComponent component) {
    if (component == null) {
      return null;
    }
    List<ShaderEntryState> entries =
        component.shaders().stream()
            .map(entry -> toState(entry.identifier(), entry.order(), entry.shader()))
            .toList();
    return new ShaderComponentState(entries);
  }

  /**
   * Converts network state into a runtime shader component.
   *
   * @param state network state, or null
   * @return runtime component, or null when no state is supplied
   */
  public static ShaderComponent fromState(ShaderComponentState state) {
    if (state == null) {
      return null;
    }
    return new ShaderComponent(
        state.shaders().stream()
            .map(
                entry ->
                    new ShaderComponent.ShaderEntry(
                        entry.identifier(), entry.order(), fromState(entry)))
            .toList());
  }

  /**
   * Converts a network state into protobuf.
   *
   * @param state network state
   * @return protobuf shader state
   */
  public static engine.network.proto.s2c.ShaderComponentInfo toProto(
      ShaderComponentState state) {
    if (state == null) {
      throw new IllegalArgumentException("Shader component state is required.");
    }
    engine.network.proto.s2c.ShaderComponentInfo.Builder builder =
        engine.network.proto.s2c.ShaderComponentInfo.newBuilder();
    for (ShaderEntryState entry : state.shaders()) {
      builder.addShaders(
          engine.network.proto.s2c.ShaderEntryInfo.newBuilder()
              .setIdentifier(entry.identifier())
              .setOrder(entry.order())
              .setType(entry.type())
              .setEnabled(entry.enabled())
              .setUpscaling(entry.upscaling())
              .putAllProperties(entry.properties())
              .build());
    }
    return builder.build();
  }

  /**
   * Converts protobuf shader state into its immutable network representation.
   *
   * @param proto protobuf shader state
   * @return immutable network shader state
   */
  public static ShaderComponentState fromProto(
      engine.network.proto.s2c.ShaderComponentInfo proto) {
    if (proto == null) {
      throw new IllegalArgumentException("Shader component protobuf state is required.");
    }
    List<ShaderEntryState> entries =
        proto.getShadersList().stream()
            .map(
                entry ->
                    new ShaderEntryState(
                        entry.getIdentifier(),
                        entry.getOrder(),
                        entry.getType(),
                        entry.getEnabled(),
                        entry.getUpscaling(),
                        entry.getPropertiesMap()))
            .toList();
    return new ShaderComponentState(entries);
  }

  private static ShaderEntryState toState(
      String identifier, int order, AbstractShader shader) {
    String type;
    if (shader instanceof OutlineShader) {
      type = TYPE_OUTLINE;
    } else if (shader instanceof ColorGradeShader) {
      type = TYPE_COLOR_GRADE;
    } else if (shader instanceof HueRemapShader) {
      type = TYPE_HUE_REMAP;
    } else if (shader instanceof EnergyFillShader) {
      type = TYPE_ENERGY_FILL;
    } else if (shader instanceof ShineShader) {
      type = TYPE_SHINE;
    } else if (shader instanceof PassthroughShader) {
      type = TYPE_PASSTHROUGH;
    } else if (shader instanceof LevelHideShader) {
      type = TYPE_LEVEL_HIDE;
    } else {
      throw new IllegalArgumentException(
          "Unsupported synchronized shader type: " + shader.getClass().getName());
    }
    return new ShaderEntryState(
        identifier, order, type, shader.enabled(), shader.upscaling(), shader.properties());
  }

  private static AbstractShader fromState(ShaderEntryState entry) {
    AbstractShader shader =
        switch (entry.type()) {
          case TYPE_OUTLINE -> new OutlineShader();
          case TYPE_COLOR_GRADE -> new ColorGradeShader();
          case TYPE_HUE_REMAP -> new HueRemapShader();
          case TYPE_ENERGY_FILL -> new EnergyFillShader();
          case TYPE_SHINE -> new ShineShader();
          case TYPE_PASSTHROUGH -> new PassthroughShader();
          case TYPE_LEVEL_HIDE -> new LevelHideShader();
          default -> throw new IllegalArgumentException("Unsupported shader type: " + entry.type());
        };
    shader.loadProperties(entry.properties());
    shader.enabled(entry.enabled());
    shader.upscaling(entry.upscaling());
    return shader;
  }
}
