package core.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import core.Entity;
import core.Game;
import core.System;
import core.components.UIComponent;
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
     * the removeListener only gets the Entity after its Component is removed. Which means no longer
     * any access to the Group. This is why we need the last group an entity had as a mapping.
     */
    private Map<Entity, Group> entityGroupMap = new HashMap<>();

    private Map<Entity, UIComponent> entityUIComponentMap = new HashMap<>();

    /** The HudSystem needs the UIComponent to work. */
    public HudSystem() {
        super(UIComponent.class);
        onEntityAdd = this::addListener;
        onEntityRemove = this::removeListener;
    }

    /**
     * once a UIComponent is removed its Dialog has to be removed from the Stage
     *
     * @param entity which no longer has a UIComponent
     */
    private void removeListener(Entity entity) {
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
     * when an Entity with a UIComponent is added its dialog has to be added to the Stage for UI
     * Representation
     *
     * @param entity which now has a UIComponent
     */
    private void addListener(Entity entity) {

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

    private void addMapping(Entity entity, Group dialog, UIComponent component) {
        Group previous = entityGroupMap.put(entity, dialog);
        if (previous != null) {
            previous.remove();
        }
        UIComponent previousuicomponent = entityUIComponentMap.put(entity, component);
        if (previousuicomponent != null) {
            previousuicomponent.onClose().execute();
        }
    }

    private void addDialogToStage(Group d, Stage stage) {
        if (!stage.getActors().contains(d, true)) {
            stage.addActor(d);
        }
    }

    @Override
    public void execute() {
        if (entityStream().anyMatch(x -> pausesGame(x))) pauseGame();
        else unpauseGame();
    }

    private boolean pausesGame(Entity x) {
        Optional<UIComponent> uiComponent = x.fetch(UIComponent.class);
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
