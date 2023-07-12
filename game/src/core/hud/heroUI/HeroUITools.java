package core.hud.heroUI;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Game;
import core.hud.UITools;

public class HeroUITools {
    /**
     * Creates a popup on the screen with how much xp the hero lost or gained
     *
     * @param xpChange the amount of XP to display
     */
    public static void createXPPopup(long xpChange) {
        Label xpPopup = new Label("%+d XP".formatted(xpChange), UITools.DEFAULT_SKIN);
        xpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        Game.stage().get().addActor(xpPopup);
    }
}
