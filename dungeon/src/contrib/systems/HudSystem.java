package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.System;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The basic handling of any UIComponent. Adds them to the Stage, updates the Stage each Frame to
 * allow EventHandling.
 *
 * <p>Entities with the {@link UIComponent} will be processed by this system.
 */
public final class HudSystem extends System {
  private boolean ipaused = false;

  /**
   * The removeListener only gets the Entity after its Component is removed. Which means no longer
   * any access to the Group. This is why we need the last group an entity had as a mapping.
   */
  private final Map<Entity, Group> entityGroupMap = new HashMap<>();

  private final Map<Entity, UIComponent> entityUIComponentMap = new HashMap<>();

  /** Create a new HudSystem. */
  public HudSystem() {
    super(AuthoritativeSide.CLIENT, UIComponent.class);
    onEntityAdd = this::addListener;
    onEntityRemove = this::removeListener;
  }

  /**
   * Returns the topmost closeable UI.
   *
   * @return a Tuple of the Entity and its UIComponent
   */
  public Optional<Tuple<Entity, UIComponent>> topmostCloseableUI() {
    return entityUIComponentMap.entrySet().stream()
        .filter(entry -> entry.getValue().isVisible() && entry.getValue().canBeClosed())
        .max(Comparator.comparingInt(entry -> entry.getValue().dialog().getZIndex()))
        .map(entry -> Tuple.of(entry.getKey(), entry.getValue()));
  }

  /**
   * Once a UIComponent is removed, its Dialog has to be removed from the Stage.
   *
   * @param entity Entity which no longer has a UIComponent.
   */
  private void removeListener(final Entity entity) {
    Group remove = entityGroupMap.remove(entity);
    if (remove != null) {
      remove.remove();
    }
    UIComponent component = entityUIComponentMap.remove(entity);
    if (component != null) {
      component.onClose().execute();
    }
  }

  /**
   * When an Entity with a UIComponent is added, its dialog has to be added to the Stage for UI
   * Representation.
   *
   * @param entity Entity which now has a UIComponent.
   */
  private void addListener(final Entity entity) {
    UIComponent component =
        entity
            .fetch(UIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, UIComponent.class));
    Group dialog = component.dialog();

    Game.stage()
        .ifPresent(
            stage -> {
              addDialogToStage(dialog, stage);
              addMapping(entity, dialog, component);
            });
  }

  private void addMapping(final Entity entity, final Group dialog, final UIComponent component) {
    Group previous = entityGroupMap.put(entity, dialog);
    if (previous != null) {
      previous.remove();
    }
    UIComponent previousUiComponent = entityUIComponentMap.put(entity, component);
    if (previousUiComponent != null) {
      previousUiComponent.onClose().execute();
    }
  }

  private void addDialogToStage(final Group group, final Stage stage) {
    if (!stage.getActors().contains(group, true)) {
      stage.addActor(group);
    }
  }

  @Override
  public void execute() {
    if (filteredEntityStream(UIComponent.class).anyMatch(this::pausesGame)) pauseGame();
    else unpauseGame();

    // clean up any entities that no longer have a UIComponent
    entityUIComponentMap.keySet().removeIf(entity -> !entity.isPresent(UIComponent.class));
  }

  private boolean pausesGame(final Entity entity) {
    Optional<UIComponent> uiComponent = entity.fetch(UIComponent.class);
    return uiComponent
        .filter(component -> component.isVisible() && component.willPauseGame())
        .isPresent();
  }

  private void pauseGame() {
    ipaused = true;
    Game.systems().values().forEach(System::stop);
  }

  private void unpauseGame() {
    if (ipaused) Game.systems().values().forEach(System::run);
    ipaused = false;
  }

  /** HudSystem can´t be paused. */
  @Override
  public void stop() {}
}
