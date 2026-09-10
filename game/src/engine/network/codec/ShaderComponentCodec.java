package engine.network.codec;

import com.badlogic.gdx.graphics.Color;
import engine.network.messages.s2c.ShaderComponentState;
import engine.network.messages.s2c.ShaderComponentState.ShaderEntryState;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.LevelHideShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.PassthroughShader;
import engine.utils.components.draw.shader.ShineShader;
import feature.shader.ShaderComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, String> properties = new LinkedHashMap<>();
    String type;
    if (shader instanceof OutlineShader outline) {
      type = TYPE_OUTLINE;
      properties.put("width", Integer.toString(outline.width()));
      putColor(properties, outline.color());
      properties.put("beatSpeed", Float.toString(outline.beatSpeed()));
      properties.put("beatIntensity", Float.toString(outline.beatIntensity()));
      properties.put("rainbow", Boolean.toString(outline.isRainbow()));
    } else if (shader instanceof ColorGradeShader colorGrade) {
      type = TYPE_COLOR_GRADE;
      putRectangle(properties, colorGrade.region());
      properties.put("hue", Float.toString(colorGrade.hue()));
      properties.put(
          "saturationMultiplier", Float.toString(colorGrade.saturationMultiplier()));
      properties.put("valueMultiplier", Float.toString(colorGrade.valueMultiplier()));
      properties.put("transitionSize", Float.toString(colorGrade.transitionSize()));
      properties.put("invert", Boolean.toString(colorGrade.invert()));
    } else if (shader instanceof HueRemapShader hueRemap) {
      type = TYPE_HUE_REMAP;
      properties.put("startingHue", Float.toString(hueRemap.startingHue()));
      properties.put("targetHue", Float.toString(hueRemap.targetHue()));
      properties.put("tolerance", Float.toString(hueRemap.tolerance()));
    } else if (shader instanceof EnergyFillShader energyFill) {
      type = TYPE_ENERGY_FILL;
      properties.put("fillPercentage", Float.toString(energyFill.fillPercentage()));
      putColor(properties, energyFill.color());
      if (energyFill.texturePath() != null) {
        properties.put("texturePath", energyFill.texturePath());
      }
    } else if (shader instanceof ShineShader shine) {
      type = TYPE_SHINE;
      properties.put("padding", Integer.toString(shine.padding()));
      properties.put("sliceCount", Integer.toString(shine.sliceCount()));
      properties.put("gapSize", Float.toString(shine.gapSize()));
      properties.put("rotationSpeed", Float.toString(shine.rotationSpeed()));
      putColor(properties, shine.shineColor());
    } else if (shader instanceof PassthroughShader passthrough) {
      type = TYPE_PASSTHROUGH;
      properties.put("debugPMA", Boolean.toString(passthrough.debugPMA()));
      properties.put("debugWorldPos", Boolean.toString(passthrough.debugWorldPos()));
    } else if (shader instanceof LevelHideShader levelHide) {
      type = TYPE_LEVEL_HIDE;
      properties.put("hiding", Boolean.toString(levelHide.hiding()));
      putRectangle(properties, levelHide.region());
      properties.put("transitionSize", Float.toString(levelHide.transitionSize()));
    } else {
      throw new IllegalArgumentException(
          "Unsupported synchronized shader type: " + shader.getClass().getName());
    }
    return new ShaderEntryState(
        identifier, order, type, shader.enabled(), shader.upscaling(), properties);
  }

  private static AbstractShader fromState(ShaderEntryState entry) {
    Map<String, String> properties = entry.properties();
    AbstractShader shader =
        switch (entry.type()) {
          case TYPE_OUTLINE ->
              new OutlineShader(
                      intProperty(properties, "width"),
                      colorProperty(properties),
                      floatProperty(properties, "beatSpeed"),
                      floatProperty(properties, "beatIntensity"))
                  .isRainbow(booleanProperty(properties, "rainbow"));
          case TYPE_COLOR_GRADE ->
              new ColorGradeShader()
                  .region(rectangleProperty(properties))
                  .hue(floatProperty(properties, "hue"))
                  .saturationMultiplier(floatProperty(properties, "saturationMultiplier"))
                  .valueMultiplier(floatProperty(properties, "valueMultiplier"))
                  .transitionSize(floatProperty(properties, "transitionSize"))
                  .invert(booleanProperty(properties, "invert"));
          case TYPE_HUE_REMAP ->
              new HueRemapShader(
                  floatProperty(properties, "startingHue"),
                  floatProperty(properties, "targetHue"),
                  floatProperty(properties, "tolerance"));
          case TYPE_ENERGY_FILL ->
              new EnergyFillShader(
                  floatProperty(properties, "fillPercentage"),
                  colorProperty(properties),
                  properties.get("texturePath"));
          case TYPE_SHINE ->
              new ShineShader()
                  .padding(intProperty(properties, "padding"))
                  .sliceCount(intProperty(properties, "sliceCount"))
                  .gapSize(floatProperty(properties, "gapSize"))
                  .rotationSpeed(floatProperty(properties, "rotationSpeed"))
                  .shineColor(colorProperty(properties));
          case TYPE_PASSTHROUGH ->
              new PassthroughShader()
                  .debugPMA(booleanProperty(properties, "debugPMA"))
                  .debugWorldPos(booleanProperty(properties, "debugWorldPos"));
          case TYPE_LEVEL_HIDE ->
              new LevelHideShader(
                      booleanProperty(properties, "hiding"), rectangleProperty(properties))
                  .transitionSize(floatProperty(properties, "transitionSize"));
          default -> throw new IllegalArgumentException("Unsupported shader type: " + entry.type());
        };
    shader.enabled(entry.enabled());
    shader.upscaling(entry.upscaling());
    return shader;
  }

  private static void putColor(Map<String, String> properties, Color color) {
    if (color == null) {
      throw new IllegalArgumentException("Synchronized shader color must not be null.");
    }
    properties.put("red", Float.toString(color.r));
    properties.put("green", Float.toString(color.g));
    properties.put("blue", Float.toString(color.b));
    properties.put("alpha", Float.toString(color.a));
  }

  private static Color colorProperty(Map<String, String> properties) {
    return new Color(
        floatProperty(properties, "red"),
        floatProperty(properties, "green"),
        floatProperty(properties, "blue"),
        floatProperty(properties, "alpha"));
  }

  private static void putRectangle(Map<String, String> properties, Rectangle rectangle) {
    if (rectangle == null) {
      throw new IllegalArgumentException("Synchronized shader rectangle must not be null.");
    }
    properties.put("width", Float.toString(rectangle.width()));
    properties.put("height", Float.toString(rectangle.height()));
    properties.put("x", Float.toString(rectangle.x()));
    properties.put("y", Float.toString(rectangle.y()));
  }

  private static Rectangle rectangleProperty(Map<String, String> properties) {
    return new Rectangle(
        floatProperty(properties, "width"),
        floatProperty(properties, "height"),
        floatProperty(properties, "x"),
        floatProperty(properties, "y"));
  }

  private static String property(Map<String, String> properties, String name) {
    String value = properties.get(name);
    if (value == null) {
      throw new IllegalArgumentException("Missing synchronized shader property: " + name);
    }
    return value;
  }

  private static int intProperty(Map<String, String> properties, String name) {
    return Integer.parseInt(property(properties, name));
  }

  private static float floatProperty(Map<String, String> properties, String name) {
    return Float.parseFloat(property(properties, name));
  }

  private static boolean booleanProperty(Map<String, String> properties, String name) {
    String value = property(properties, name);
    if (!"true".equals(value) && !"false".equals(value)) {
      throw new IllegalArgumentException(
          "Invalid boolean synchronized shader property '" + name + "': " + value);
    }
    return Boolean.parseBoolean(value);
  }
}
