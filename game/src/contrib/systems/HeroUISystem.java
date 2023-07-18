package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.XPComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.UIComponent;
import core.hud.UITools;

import java.util.HashMap;

public final class HeroUISystem extends System {
    private final HashMap<Entity, HeroUI> map;

    private record HeroUI(ProgressBar pb, Label level, Entity ui) {}

    public HeroUISystem() {
        super(XPComponent.class, PlayerComponent.class);
        map = new HashMap<>();
        onEntityAdd = (x) -> map.put(x, createNewHeroUI(x));
        onEntityRemove = (x) -> Game.removeEntity(map.remove(x).ui());
    }

    @Override
    public void execute() {
        entityStream().forEach(this::update);
    }

    private void update(Entity x) {
        XPComponent xc = x.fetch(XPComponent.class).orElseThrow();
        HeroUI ui = map.get(x);
        ui.level.setText("Level: " + xc.characterLevel());
        ui.pb.setValue(calculatePercentage(xc));
    }

    private static float calculatePercentage(XPComponent xc) {
        return (float) xc.currentXP() / ((float) xc.xpToNextCharacterLevel() + xc.currentXP());
    }

    private HeroUI createNewHeroUI(Entity e) {
        XPComponent xc = e.fetch(XPComponent.class).orElseThrow();
        float xpPercentage = calculatePercentage(xc);
        long level = xc.characterLevel();
        Entity entity = new Entity();
        // TODO: positionieren
        HeroUI ui =
                new HeroUI(
                        new ProgressBar(0f, 1f, 0.01f, false, UITools.DEFAULT_SKIN, "experience"),
                        new Label("Level: " + level, UITools.DEFAULT_SKIN),
                        entity);
        ui.pb.setValue(xpPercentage);
        Group uiGroup = new Group();
        uiGroup.addActor(ui.pb);
        ui.pb.setPosition(calculateCenterX(ui.pb.getWidth()), 40 );
        uiGroup.addActor(ui.level);
        ui.level.setPosition(calculateCenterX(ui.level.getWidth()), 60);
        new UIComponent(entity, uiGroup, false);
        return ui;
    }

    private float calculateCenterX(float elementWidth){
        return (Game.windowWidth() - elementWidth)/ 2f;
    }
}
