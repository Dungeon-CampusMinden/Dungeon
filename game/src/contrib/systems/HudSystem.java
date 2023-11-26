package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import contrib.components.UIComponent;

import core.Entity;
import core.Game;
import core.System;
import core.utils.components.MissingComponentException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The basic handling of any UIComponent. Adds them to the Stage, updates the Stage each Frame to
 * allow EventHandling.
 */
public final class HudSystem extends System {

    /**
     * The removeListener only gets the Entity after its Component is removed. Which means no longer
     * any access to the Group. This is why we need the last group an entity had as a mapping.
     */
    private final Map<Entity, Group> entityGroupMap = new HashMap<>();

    private final Map<Entity, UIComponent> entityUIComponentMap = new HashMap<>();

    /** The HudSystem needs the UIComponent to work. */
    public HudSystem() {
        super(UIComponent.class);
        onEntityAdd = this::addListener;
        onEntityRemove = this::removeListener;
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
                entity.fetch(UIComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, UIComponent.class));
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
        UIComponent previousuicomponent = entityUIComponentMap.put(entity, component);
        if (previousuicomponent != null) {
            previousuicomponent.onClose().execute();
        }
    }

    private void addDialogToStage(final Group group, final Stage stage) {
        if (!stage.getActors().contains(group, true)) {
            stage.addActor(group);
        }
    }

    @Override
    public void execute() {
        if (entityStream().anyMatch(e -> pausesGame(e))) pauseGame();
        else unpauseGame();
    }

    private boolean pausesGame(final Entity entity) {
        Optional<UIComponent> uiComponent = entity.fetch(UIComponent.class);
        return uiComponent
                .filter(component -> component.isVisible() && component.willPauseGame())
                .isPresent();
    }

    private void pauseGame() {
        Game.systems().values().forEach(System::stop);
    }

    private void unpauseGame() {
        Game.systems().values().forEach(System::run);
    }

    /** HudSystem can´t be paused */
    @Override
    public void stop() {}
}
