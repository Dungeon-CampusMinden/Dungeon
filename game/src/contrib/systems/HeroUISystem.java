package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.XPComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.hud.heroUI.HeroUI;
import core.hud.heroUI.HeroXPBar;

public final class HeroUISystem extends System {

    public HeroUISystem() {
        super(XPComponent.class, PlayerComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(x->{});
    }

    private HeroUI createNewHeroUI(Entity e){
        XPComponent xc = e.fetch(XPComponent.class).orElseThrow();
        float xpPercentage = (float) xc.currentXP() / (xc.xpToNextLevel() + xc.currentXP()) * 100;
        HeroUI ui = new HeroUI(xc.currentLevel(), xc.currentXP());


        return ui;
    }
}
