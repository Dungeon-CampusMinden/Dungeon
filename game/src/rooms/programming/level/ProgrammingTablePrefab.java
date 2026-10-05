package rooms.programming.level;

import engine.Entity;
import engine.components.DrawComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** A movable Programming workbench or plinth with its decorative contents. */
public final class ProgrammingTablePrefab extends Prefab {
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  public static final PrefabProperty<String> FURNITURE =
      PrefabProperty.selection(
          "furniture",
          "Furniture",
          "ProgrammingWorkbench",
          List.of(
              "ProgrammingWorkbench",
              "ProgrammingRunePlinth",
              "ProgrammingAltarPlinth",
              "ProgrammingSourceBench",
              "ProgrammingSmallPlinth",
              "ProgrammingSourcePlinth",
              "ProgrammingHeartPlinth"));
  private static final List<String> ITEMS =
      List.of(
          "none",
          "ProgrammingTabletopVase",
          "ProgrammingTabletopTools",
          "ProgrammingTablet",
          "ProgrammingDrainedVase",
          "ProgrammingNote",
          "ProgrammingSourceCrystal",
          "ProgrammingStrengthStone",
          "ProgrammingSourceTools",
          "ProgrammingSourceKettle",
          "ProgrammingCauldron",
          "ProgrammingSealBook",
          "ProgrammingSealGem");
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

  private static List<PrefabProperty<?>> propertiesList() {
    List<PrefabProperty<?>> properties = new ArrayList<>(List.of(POSITION, FURNITURE));
    for (int i = 0; i < ITEM.size(); i++) {
      properties.add(ITEM.get(i));
      properties.add(OFFSET.get(i));
    }
    return List.copyOf(properties);
  }

  /** Creates the editor definition. */
  public ProgrammingTablePrefab() {
    super("programming-table", "Programming table", PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a typed view of an authored table.
   *
   * @param level owning level
   * @param name instance name
   */
  public ProgrammingTablePrefab(ILevel level, String name) {
    super("programming-table", "Programming table", PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /** Registers the room prefab before any server, client, or editor level parsing. */
  public static void register() {
    if (PrefabRegistry.find("programming-table").isEmpty())
      PrefabRegistry.register(new ProgrammingTablePrefab(), ProgrammingTablePrefab::new);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point position = value(instance, POSITION);
    List<Entity> entities = new ArrayList<>();
    Entity furniture = deco(context, position, value(instance, FURNITURE));
    entities.add(furniture);
    int itemDepth = furniture.fetch(DrawComponent.class).orElseThrow().depth() + 1;
    for (int i = 0; i < ITEM.size(); i++) {
      String item = value(instance, ITEM.get(i));
      if (!item.equals("none")) {
        Entity entity = deco(context, position.translate(value(instance, OFFSET.get(i))), item);
        DrawComponent draw = entity.fetch(DrawComponent.class).orElseThrow();
        draw.depth(Math.max(draw.depth(), itemDepth));
        entities.add(entity);
      }
    }
    ProgrammingTableAnchors.changed(context.level());
    return entities;
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    ProgrammingTableAnchors.changed(context.level());
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
