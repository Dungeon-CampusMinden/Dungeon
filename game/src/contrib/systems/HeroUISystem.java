package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.XPComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.UIComponent;

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
        ui.level.setText("Level: " + xc.currentLevel());
        ui.pb.setValue(calculatePercentage(xc));
    }

    private static float calculatePercentage(XPComponent xc) {
        return (float) xc.currentXP() / ((float) xc.xpToNextLevel() + xc.currentXP());
    }

    private HeroUI createNewHeroUI(Entity e) {
        XPComponent xc = e.fetch(XPComponent.class).orElseThrow();
        float xpPercentage = calculatePercentage(xc);
        long level = xc.currentLevel();
        Entity entity = new Entity();
        HeroUI ui =
                new HeroUI(
                        new ProgressBar(0f, 1f, 0.01f, false, new Skin()),
                        new Label("Level: " + level, new Skin()),
                        entity);
        ui.pb.setValue(xpPercentage);
        Group uiGroup = new Group();
        uiGroup.addActor(ui.pb);
        uiGroup.addActor(ui.level);
        new UIComponent(entity, uiGroup, false);
        return ui;
    }
}
