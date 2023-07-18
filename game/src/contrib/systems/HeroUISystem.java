package contrib.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

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

        ProgressBar.ProgressBarStyle experienceStyle =
                createNewPBStyleWhichShouldBeInAtlasAndIsAToDoYesItIsUglyToAnnoyAll(Color.GREEN);

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
        new UIComponent(entity, uiGroup, false);
        return ui;
    }

    public static ProgressBar.ProgressBarStyle
            createNewPBStyleWhichShouldBeInAtlasAndIsAToDoYesItIsUglyToAnnoyAll(Color color) {
        // TODO: temporary addon so no changes needed in skin which will always conflict
        var pbstyle =
                UITools.DEFAULT_SKIN.get("default-horizontal", ProgressBar.ProgressBarStyle.class);
        // copy for experience
        var experiencepbStyle = new ProgressBar.ProgressBarStyle(pbstyle);
        experiencepbStyle.background =
                new NinePatchDrawable((NinePatchDrawable) experiencepbStyle.background);
        experiencepbStyle.knobBefore =
                ((TextureRegionDrawable) experiencepbStyle.knobBefore).tint(color);
        ((NinePatchDrawable) experiencepbStyle.background).getPatch().scale(0.2f, 0.2f);
        experiencepbStyle.background.setMinWidth(1);
        experiencepbStyle.background.setMinHeight(12);
        experiencepbStyle.knobBefore.setMinWidth(1);
        experiencepbStyle.knobBefore.setMinHeight(10);
        return experiencepbStyle;
    }

    private float calculateCenterX(float elementWidth) {
        return (Game.windowWidth() - elementWidth) / 2f;
    }
}
