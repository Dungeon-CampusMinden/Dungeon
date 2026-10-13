package rooms.soulweaver.level;

import engine.Entity;
import engine.components.DrawComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import feature.utils.EntityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** A movable Soulweaver workbench or plinth with its decorative contents. */
public final class SoulweaverTablePrefab extends Prefab {
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  public static final PrefabProperty<String> FURNITURE =
      PrefabProperty.selection(
          "furniture",
          "Furniture",
          "SoulweaverWorkbench",
          List.of(
              "SoulweaverWorkbench",
              "SoulweaverRunePlinth",
              "SoulweaverAltarPlinth",
              "SoulweaverSourceBench",
              "SoulweaverSmallPlinth",
              "SoulweaverSourcePlinth",
              "SoulweaverHeartPlinth"));
  private static final List<String> ITEMS =
      List.of(
          "none",
          "SoulweaverTabletopVase",
          "SoulweaverTabletopTools",
          "SoulweaverTablet",
          "SoulweaverDrainedVase",
          "SoulweaverNote",
          "SoulweaverSourceCrystal",
          "SoulweaverStrengthStone",
          "SoulweaverSourceTools",
          "SoulweaverSourceKettle",
          "SoulweaverCauldron",
          "SoulweaverSealBook",
          "SoulweaverSealGem");
  public static final List<PrefabProperty<String>> ITEM =
      IntStream.rangeClosed(1, 4)
          .mapToObj(i -> PrefabProperty.selection("item-" + i, "Item " + i, "none", ITEMS))
          .toList();
  public static final List<PrefabProperty<Vector2>> OFFSET =
      IntStream.rangeClosed(1, 4)
          .mapToObj(
              i -> PrefabProperty.vector2("offset-" + i, "Item " + i + " offset", Vector2.ZERO))
          .toList();
  private static final List<PrefabProperty<?>> PROPERTIES = propertiesList();
  // Sorts a tabletop item just in front of its furniture, but not in front of anyone else.
  private static final float RESTING_GAP = 0.01f;

  private static List<PrefabProperty<?>> propertiesList() {
    List<PrefabProperty<?>> properties = new ArrayList<>(List.of(POSITION, FURNITURE));
    for (int i = 0; i < ITEM.size(); i++) {
      properties.add(ITEM.get(i));
      properties.add(OFFSET.get(i));
    }
    return List.copyOf(properties);
  }

  /** Creates the editor definition. */
  public SoulweaverTablePrefab() {
    super("soulweaver-table", "Soulweaver table", PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a typed view of an authored table.
   *
   * @param level owning level
   * @param name instance name
   */
  public SoulweaverTablePrefab(ILevel level, String name) {
    super("soulweaver-table", "Soulweaver table", PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /** Registers the room prefab before any server, client, or editor level parsing. */
  public static void register() {
    if (PrefabRegistry.find("soulweaver-table").isEmpty())
      PrefabRegistry.register(new SoulweaverTablePrefab(), SoulweaverTablePrefab::new);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point position = value(instance, POSITION);
    List<Entity> entities = new ArrayList<>();
    String furniture = value(instance, FURNITURE);
    entities.add(deco(context, position, furniture));
    for (int i = 0; i < ITEM.size(); i++) {
      String item = value(instance, ITEM.get(i));
      if (!item.equals("none")) {
        Entity entity = deco(context, position.translate(value(instance, OFFSET.get(i))), item);
        restOn(entity, position, Deco.valueOf(furniture));
        entities.add(entity);
      }
    }
    SoulweaverTableAnchors.changed(context.level());
    return entities;
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    SoulweaverTableAnchors.changed(context.level());
  }

  /**
   * Draws an item resting on furniture in the furniture's depth layer, sorted just in front of it:
   * characters in front of the furniture cover both, characters behind it are covered by both.
   *
   * @param item entity on the furniture, already at its final position and scale
   * @param position furniture position
   * @param furniture furniture decoration
   */
  static void restOn(Entity item, Point position, Deco furniture) {
    // Furniture sorts by its collider centre; without a collider the item sorts before its base.
    Rectangle collider = furniture.defaultCollider();
    float furnitureY = position.y() + (collider == null ? 0 : collider.y() + collider.height() / 2);
    DrawComponent draw = item.fetch(DrawComponent.class).orElseThrow();
    draw.depth(furniture.defaultDepth());
    draw.sortOffset(furnitureY - RESTING_GAP - EntityUtils.getPosition(item).y());
  }

  private static Entity deco(PrefabCreationContext context, Point position, String name) {
    Entity entity = context.createEntity(name);
    DecoFactory.createDeco(entity, position, Deco.valueOf(name));
    // The prefab owns persistence; the editor must not save these again as loose decorations.
    entity.remove(DecoComponent.class);
    return entity;
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }
}
