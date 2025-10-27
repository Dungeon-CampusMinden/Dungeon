package contrib.utils.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.systems.PositionSync;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DecoMode extends LevelEditorMode {

  private static int selectedDecoIndex = 0;
  private static SnapMode decoSnapMode = SnapMode.OnGrid;
  private static DecoEntityData decoPreviewEntity = null;
  private static DecoEntityData decoHeldEntity = null;

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
        // Check if clicking on existing deco to pick up
        Optional<DecoEntityData> clickedDeco = getDecoOnPosition(cursorPos);
        if (clickedDeco.isPresent()) {
          decoHeldEntity = clickedDeco.get();
          removePreviewEntity();
        } else {
          // Place new deco instance
          Deco decoType = Deco.values()[selectedDecoIndex];
          Entity newDeco = DecoFactory.createDeco(snapPos, decoType);
          Game.add(newDeco);
        }
      }
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
      getDecoOnPosition(cursorPos).map(DecoEntityData::entity).ifPresent(Game::remove);
    }

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
    StringBuilder status = new StringBuilder("--- Edit Decos Mode ---");
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next Deco");
    controls.put(PRIMARY_DOWN, "Prev Deco");
    controls.put(SECONDARY_UP, "Change Grid Snap");
    addControlsToStatus(status, controls);
    status.append("\n\nSettings:");
    int decoCount = Deco.values().length;
    Deco currentDeco = Deco.values()[selectedDecoIndex];
    status
        .append("\nCurrent Deco: ")
        .append(selectedDecoIndex + 1)
        .append("/")
        .append(decoCount)
        .append(" (")
        .append(currentDeco.name())
        .append(")");
    status.append("\nSnap Mode: ").append(decoSnapMode.name());
    return status.toString();
  }

  private void setupPreviewEntity(Point pos) {
    Entity deco = DecoFactory.createDeco(pos, Deco.values()[selectedDecoIndex]);
    deco.fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.tintColor(Color.rgba8888(1, 1, 1, 0.3f));
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
              Point basePos = pc.position();
              Point entityPos = EntityUtils.getPosition(entity);
              Vector2 offset = basePos.vectorTo(entityPos);
              pc.position(position.translate(offset.scale(-1)));
              PositionSync.syncPosition(entity);
            });
  }

  private Optional<DecoEntityData> getDecoOnPosition(Point position) {
    return getSystem()
        .filteredEntityStream(DecoComponent.class)
        .map(DecoEntityData::of)
        .filter(
            ded ->
                !ded.equals(decoPreviewEntity)
                    && EntityUtils.getPosition(ded.entity).distance(position) < 1.0f)
        .findFirst();
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
