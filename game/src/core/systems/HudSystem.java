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

/**
 * The basic handling of any UIComponent. Adds them to the Stage, updates the Stage each Frame to
 * allow EventHandling.
 *
 * <p>Issue #727 would add the ability to remove Dialogs from the Stage when the UIComponent is no
 * longer available
 */
public final class HudSystem extends System {
    private Map<Integer, Group> idGroupMap = new HashMap<>();

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
    private void removeListener(Entity entity) {}

    /**
     * when an Entity with a UIComponent is added its dialog has to be added to the Stage for UI
     * Representation
     *
     * @param entity which now has a UIComponent
     */
    private void addListener(Entity entity) {
        Group dialog =
                entity.fetch(UIComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, UIComponent.class))
                        .dialog();
        Game.stage().ifPresent(stage -> addDialogToStage(dialog, stage));
    }

    private static void addDialogToStage(Group d, Stage stage) {
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
        UIComponent uiComponent =
                x.fetch(UIComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(x, UIComponent.class));
        return uiComponent.isVisible() && uiComponent.willPauseGame();
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
