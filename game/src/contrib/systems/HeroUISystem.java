package contrib.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.UIComponent;
import contrib.components.XPComponent;
import contrib.hud.UITools;
import contrib.hud.heroUI.HeroUITools;

import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

import java.util.HashMap;

/**
 * creates an experience bar and Level for a playable Entity with an XPComponent. They are placed at
 * the Center of the window.
 */
public final class HeroUISystem extends System {
    private final HashMap<Entity, HeroUI> map;

    private record HeroUI(ProgressBar pb, Label level, Entity ui) {}

    public HeroUISystem() {
        super(XPComponent.class, PlayerComponent.class);
        map = new HashMap<>();
        onEntityAdd = (x) -> map.put(x, createNewHeroUI(x));
        onEntityRemove = (x) -> Game.remove(map.remove(x).ui());
    }

    @Override
    public void execute() {
        entityStream().forEach(this::update);
    }

    private void update(Entity entity) {
        XPComponent xc =
                entity.fetch(XPComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, XPComponent.class));
        HeroUI ui = map.get(entity);
        ui.level.setText("Level: " + xc.characterLevel());
        ui.pb.setValue(calculatePercentage(xc));
    }

    private static float calculatePercentage(XPComponent xc) {
        return (float) xc.currentXP() / ((float) xc.xpToNextCharacterLevel() + xc.currentXP());
    }

    private HeroUI createNewHeroUI(Entity e) {
        XPComponent xc =
                e.fetch(XPComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(e, XPComponent.class));
        float xpPercentage = calculatePercentage(xc);
        long level = xc.characterLevel();
        Entity entity = new Entity();

        ProgressBar.ProgressBarStyle experienceStyle =
                HeroUITools.createNewPBStyleWhichShouldBeInAtlasAndIsAToDoYesItIsUglyToAnnoyAll(
                        Color.GREEN);

        HeroUI ui =
                new HeroUI(
                        new ProgressBar(0f, 1f, 0.01f, false, experienceStyle),
                        new Label("Level: " + level, UITools.DEFAULT_SKIN),
                        entity);
        ui.pb.setValue(xpPercentage);
        Group uiGroup = new Group();
        uiGroup.addActor(ui.pb);
        ui.pb.setPosition(calculateCenterX(ui.pb.getWidth()), 40);
        uiGroup.addActor(ui.level);
        ui.level.setPosition(calculateCenterX(ui.level.getWidth()), 60);
        entity.addComponent(new UIComponent(uiGroup, false, false));
        Game.add(entity);
        return ui;
    }

    private float calculateCenterX(float elementWidth) {
        return (Game.windowWidth() - elementWidth) / 2f;
    }
}
