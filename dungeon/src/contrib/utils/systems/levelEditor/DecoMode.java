package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.systems.PositionSync;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.utils.Point;
import core.utils.Vector2;

import java.util.*;

public class DecoMode extends LevelEditorMode {

  private static final float HOVER_DISTANCE = 0.75f;
  private static final float PREVIEW_ALPHA = 0.3f;

  private static int selectedDecoIndex = 0;
  private static SnapMode decoSnapMode = SnapMode.OnGrid;
  private static DecoEntityData decoPreviewEntity = null;
  private static DecoEntityData decoHeldEntity = null;
  private static DecoEntityData decoHoveredEntity = null;

  public DecoMode() {
    super("Deco Mode");
  }

  @Override
  public void execute() {
    // Change selected deco
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex + 1, Deco.values().length);
      previewEntityChanged();
    } else if (Gdx.input.isKeyJustPressed(PRIMARY_DOWN)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex - 1, Deco.values().length);
      previewEntityChanged();
    }

    // Change snap mode
    if (Gdx.input.isKeyJustPressed(SECONDARY_UP)) {
      decoSnapMode = decoSnapMode.nextMode();
    }

    // Mouse interactions:
    // - LMB on deco: pickup deco
    // - LMB anywhere [holding a deco]: place deco
    // - LMB anywhere [not holding a deco]: place new instance of deco
    // - RMB on deco: remove deco
    // - Mouse move [holding a deco]: show preview of deco at cursor position
    Point cursorPos = getCursorPosition();
    Point snapPos = decoSnapMode.getPosition(cursorPos);
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      if (decoHeldEntity != null) {
        // Place held deco
        setPosition(decoHeldEntity.entity, snapPos);
        decoHeldEntity = null;
        setupPreviewEntity(snapPos);
      } else {
        // Place new deco instance
        Deco decoType = Deco.values()[selectedDecoIndex];
        Vector2 offset = getEntityOffset(decoPreviewEntity.entity);
        Entity newDeco = DecoFactory.createDeco(snapPos.translate(offset.scale(-1)), decoType);
        Game.add(newDeco);
        syncPlacedDecos();
      }
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && decoHeldEntity == null) {
      // Pickup deco on cursor
      Optional<DecoEntityData> clickedDeco = getDecoOnPosition(cursorPos);
      if (clickedDeco.isPresent()) {
        decoHeldEntity = clickedDeco.get();
        removePreviewEntity();
      }
    } else if (Gdx.input.isKeyPressed(TERTIARY)) {
      // Delete deco on cursor
      getDecoOnPosition(cursorPos).map(DecoEntityData::entity).ifPresent(Game::remove);
      syncPlacedDecos();
    } else if (Gdx.input.isKeyJustPressed(SECONDARY_DOWN)) {
      // Pipette tool to pick deco type on cursor
      Optional<DecoEntityData> clickedDeco = getDecoOnPosition(cursorPos);
      if (clickedDeco.isPresent()) {
        DecoComponent dc = clickedDeco.get().dc;
        for (int i = 0; i < Deco.values().length; i++) {
          if (Deco.values()[i] == dc.type()) {
            selectedDecoIndex = i;
            previewEntityChanged();
            break;
          }
        }
      }
    }

    // Update hovered entity
    updateHoveredEntity(cursorPos);

    // Update preview entity position
    if (decoHeldEntity != null) {
      setPosition(decoHeldEntity.entity, snapPos);
      return;
    }

    // No held entity, show preview entity
    setPosition(decoPreviewEntity.entity, snapPos);
  }

  @Override
  public void onEnter() {
    setupPreviewEntity(new Point(0, 0));
  }

  @Override
  public void onExit() {
    removePreviewEntity();
  }

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder();
    int decoCount = Deco.values().length;
    Deco currentDeco = Deco.values()[selectedDecoIndex];
    status
        .append("Current Deco: ")
        .append(selectedDecoIndex + 1)
        .append("/")
        .append(decoCount)
        .append(" (")
        .append(currentDeco.name())
        .append(")");
    status.append("\nSnap Mode: ").append(decoSnapMode.name());
    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next Deco");
    controls.put(PRIMARY_DOWN, "Prev Deco");
    controls.put(SECONDARY_UP, "Change Grid Snap");
    controls.put(SECONDARY_DOWN, "Delete on Cursor");
    controls.put(Input.Buttons.LEFT, "Place Deco");
    controls.put(Input.Buttons.RIGHT, "Pickup Deco");
    controls.put(TERTIARY, "Pick Deco on Cursor");
    return controls;
  }

  private void updateHoveredEntity(Point cursorPos) {
    Optional<DecoEntityData> hoveredDeco = getDecoOnPosition(cursorPos);

    boolean isHovering = hoveredDeco.isPresent();
    boolean sameEntity =
        decoHoveredEntity != null
            && hoveredDeco.isPresent()
            && decoHoveredEntity.entity.equals(hoveredDeco.get().entity);
    boolean wasHovering = decoHoveredEntity != null;

    // Not hovering, clear hovered
    if ((!isHovering && wasHovering) || (isHovering && wasHovering && !sameEntity)) {
      setEntityColor(decoHoveredEntity.entity, Color.WHITE);
      decoHoveredEntity = null;
    }
    if (isHovering && !wasHovering || (isHovering && !sameEntity)) {
      decoHoveredEntity = hoveredDeco.get();
      setEntityColor(decoHoveredEntity.entity, Color.YELLOW);
    }
  }

  private void setEntityColor(Entity entity, Color color) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.tintColor(Color.rgba8888(color));
            });
  }

  private void setupPreviewEntity(Point pos) {
    Entity deco = DecoFactory.createDeco(pos, Deco.values()[selectedDecoIndex]);
    deco.fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.tintColor(Color.rgba8888(1, 1, 1, PREVIEW_ALPHA));
            });
    deco.fetch(CollideComponent.class).ifPresent(dc -> {
      dc.isSolid(false);
    });
    Game.add(deco);
    decoPreviewEntity = DecoEntityData.of(deco);
  }

  private void removePreviewEntity() {
    if (decoPreviewEntity != null) {
      Game.remove(decoPreviewEntity.entity);
      decoPreviewEntity = null;
    }
  }

  private void previewEntityChanged() {
    Point currentPos = decoPreviewEntity.pc.position();
    removePreviewEntity();
    setupPreviewEntity(currentPos);
  }

  private void setPosition(Entity entity, Point position) {
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Vector2 offset = getEntityOffset(entity);
              pc.position(position.translate(offset.scale(-1)));
              PositionSync.syncPosition(entity);
            });
  }

  /**
   * Get the offset of the entity based on its CollideComponent size. If smaller than 1.0f in any
   * dimension, center it within the tile. Otherwise, the bottom-left corner of the collider is
   * used.
   *
   * @param entity The entity to get the offset for
   * @return The offset vector
   */
  private Vector2 getEntityOffset(Entity entity) {
    return entity
        .fetch(CollideComponent.class)
        .map(
            cc -> {
              float x = 0, y = 0;
              if (cc.collider().size().x() < 1.0f) {
                x = -0.5f + cc.collider().size().x() / 2.0f;
              }
              if (cc.collider().size().y() < 1.0f) {
                y = -0.5f + cc.collider().size().y() / 2.0f;
              }
              return cc.collider().offset().add(Vector2.of(x, y));
            })
        .orElse(Vector2.ZERO);
  }

  private Optional<DecoEntityData> getDecoOnPosition(Point position) {
    return getSystem()
        .filteredEntityStream(DecoComponent.class)
        .map(DecoEntityData::of)
        .filter(
            ded ->
                !ded.equals(decoPreviewEntity)
                    && EntityUtils.getPosition(ded.entity).distance(position) < HOVER_DISTANCE)
        .findFirst();
  }

  /**
   * Puts all placed decos into the level handler object for serialization.
   */
  private void syncPlacedDecos(){
    DungeonLevel level = getLevel();
    level.decorations().clear();
    Game.levelEntities(Set.of(DecoComponent.class)).map(DecoEntityData::of).forEach(ded -> {
      // Filter out preview and held entities
      if (Objects.equals(ded, decoPreviewEntity) || Objects.equals(ded, decoHeldEntity)) {
        return;
      }
      Point pos = ded.pc.position();
      Deco decoType = ded.dc.type();
      level.addDecoration(decoType, pos);
    });
  }

  private record DecoEntityData(Entity entity, DecoComponent dc, PositionComponent pc) {
    public static DecoEntityData of(Entity entity) {
      return new DecoEntityData(
          entity,
          entity.fetch(DecoComponent.class).orElseThrow(),
          entity.fetch(PositionComponent.class).orElseThrow());
    }
  }
}
