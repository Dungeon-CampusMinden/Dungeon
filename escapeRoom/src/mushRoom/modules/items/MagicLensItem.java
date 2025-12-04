package mushRoom.modules.items;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.systems.DrawSystem;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.TextureGenerator;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.OutlineShader;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;
import mushRoom.shaders.MagicLensLayerShader;
import mushRoom.shaders.MagicLensPpShader;

/** Item representing a magic lens that reveals hidden runes when used. */
public class MagicLensItem extends Item {

  private static final String BASE_PATH = "items/rpg/item_magnifying_glass.png";
  private static final String PATH = "items/rpg/item_magnifying_glass.png";

  static {
    ShaderList shaders = new ShaderList();
    shaders.add("outline", new OutlineShader(1).isRainbow(true));
    TextureGenerator.registerRenderShaderTexture(BASE_PATH, PATH, shaders);
  }

  /** Constructs a new MagicLensItem. */
  public MagicLensItem() {
    super(
        "Eine Magische Lupe",
        "Schaut in eine andere Welt. Benutze sie mit <V>.",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    Game.player().ifPresent(MagicLensItem::toggleMagicLens);
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }

  /** Adds the magic lens shaders to the draw system. */
  public static void addMagicLensShaders() {
    DrawSystem ds = DrawSystem.getInstance();
    float lensRadius = 0.075f;
    ds.entityDepthShaders(DepthLayer.Player.depth() - 10)
        .add("magicLens", new MagicLensLayerShader().lensRadius(lensRadius).active(false));
    ds.sceneShaders()
        .add("magicLensPP", new MagicLensPpShader().lensRadius(lensRadius).enabled(false), -5);
    ds.entityDepthShaders(DepthLayer.Player.depth() - 10)
        .add("outline", new OutlineShader(4).isRainbow(true));
  }

  /**
   * Toggles the magic lens effect if the provided entity has the {@link MagicLensItem}.
   *
   * @param player the entity to toggle the magic lens for
   */
  public static void toggleMagicLens(Entity player) {
    player
        .fetch(InventoryComponent.class)
        .ifPresent(
            ic -> {
              if (!ic.hasItem(MagicLensItem.class)) {
                return;
              }

              DrawSystem ds = DrawSystem.getInstance();
              if (ds.entityDepthShaders(DepthLayer.Player.depth() - 10).get("magicLens")
                  instanceof MagicLensLayerShader mlls) {
                mlls.active(!mlls.active());
              }
              if (ds.sceneShaders().get("magicLensPP") instanceof MagicLensPpShader mlpps) {
                mlpps.enabled(!mlpps.enabled());
                if (mlpps.enabled()) {
                  Sounds.MAGIC_LENS_ACTIVATED.play();
                }
              }

              if (InventoryGUI.inPlayerInventory()) {
                player.remove(UIComponent.class);
              }
            });
  }
}
