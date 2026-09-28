package rooms.systemRecovery.entities;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.shader.ShaderComponent;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the conveyor, package and scanner entities used by riddles 4 and 6. */
public final class TransportEntityFactory {

  private static final String PACKAGE_FILL_SHADER_ID = "transportPackageFill";

  private TransportEntityFactory() {}

  /**
   * Creates a scanner at the default source-texture width.
   *
   * @param point scanner position
   * @return configured conveyor scanner
   */
  public static Entity scanner(Point point) {
    return scanner(point, 1f);
  }

  /**
   * Creates a scanner positioned above the conveyor.
   *
   * @param point scanner position
   * @param widthScale horizontal scale relative to the source texture
   * @return configured conveyor scanner
   */
  public static Entity scanner(Point point, float widthScale) {
    Entity entity = new Entity("transport_scanner");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(
        new DrawComponent(
            new Animation(
                new SimpleIPath("objects/tech/module_scanner.png"),
                new AnimationConfig().scaleX(widthScale).scaleY(1f))));
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.AbovePlayer.depth()));
    return entity;
  }

  /**
   * Creates one compact repeating conveyor tile.
   *
   * @param point segment position
   * @return configured conveyor segment
   */
  public static Entity conveyorSegment(Point point) {
    Entity entity = new Entity("transport_conveyor");
    entity.add(new PositionComponent(point));
    entity.add(
        new DrawComponent(
            new Animation(
                new SimpleIPath("objects/tech/transport_conveyor.png"),
                new AnimationConfig(
                        // Use the arrow section of the generated strip as one repeatable sprite.
                        new SpritesheetConfig(650, 230, 1, 1, 300, 430))
                    .scaleX(1.2f)
                    .scaleY(1.0f))));
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.Ground.depth()));
    return entity;
  }

  /**
   * Creates a package with a stable color based on its weight.
   *
   * @param point package position
   * @param weight package weight shown when it is examined
   * @return configured package entity
   */
  public static Entity packageEntity(Point point, int weight) {
    Entity entity = new Entity("transport_package_" + weight);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw =
        new DrawComponent(
            new Animation(new SimpleIPath(EnergyEntityFactory.ENERGY_CRATE_TEXTURE)));
    draw.depth(DepthLayer.ForegroundDeco.depth());
    entity.add(draw);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        SystemRecoveryText.key("world.transport.package", weight),
                        SystemRecoveryText.key("world.transport.title"),
                        who.id()))));
    return entity;
  }

  /**
   * Adds the network-authoritative fill shader used by the transport-storage puzzle.
   *
   * @param entity package whose fill should be synchronized to clients
   * @param weight displayed package weight, also used as the fill percentage
   */
  public static void addAuthoritativePackageFill(Entity entity, int weight) {
    entity.add(new ShaderComponent(PACKAGE_FILL_SHADER_ID, 0, packageFillShader(weight)));
  }

  /**
   * Creates the shared weight-scaled fill shader used by both conveyor puzzles.
   *
   * @param weight package weight
   * @return fill shader configured for the System Recovery crate gauge
   */
  public static EnergyFillShader packageFillShader(int weight) {
    return new EnergyFillShader(
            weight / 100f,
            packageFillColor(weight),
            EnergyEntityFactory.ENERGY_CRATE_FILL_TEXTURE)
        .animMagnitude(0.015f);
  }

  private static Color packageFillColor(int value) {
    Color color = new Color();
    color.fromHsv(packageHue(value) * 360f, 0.85f, 1f);
    return color;
  }

  private static float packageHue(int value) {
    return switch (value) {
      case 15 -> 0.00f;
      case 20 -> 0.14f;
      case 30 -> 0.33f;
      case 40 -> 0.60f;
      case 60 -> 0.85f;
      default -> 0.08f;
    };
  }

}
