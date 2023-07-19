package contrib.hud.heroUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

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

    /**
     * creates a new style based on the default Progressbar skin which allows a size which is
     * smaller than 54x54.
     *
     * <p>this should be in the skin itself but if there are multiple changes in the skin git is not
     * able to merge them. TODO: merge into skin
     *
     * @param color to apply to the area before the value
     * @return a new style where the min values are changed
     */
    public static ProgressBar.ProgressBarStyle
            createNewPBStyleWhichShouldBeInAtlasAndIsAToDoYesItIsUglyToAnnoyAll(Color color) {
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

    /**
     * Creates a popup on the screen with how much hp the hero lost or gained
     *
     * @param hpChange the amount of HP to display
     */
    public void createHPPopup(int hpChange) {
        Label hpPopup = new Label("%+d HP".formatted(hpChange), UITools.DEFAULT_SKIN);
        hpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        Game.stage().get().addActor(hpPopup);
    }
}
