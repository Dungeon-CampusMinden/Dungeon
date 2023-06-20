package core.hud.heroUI;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.HealthComponent;
import contrib.components.XPComponent;

import core.Game;

import java.util.logging.Logger;

/** This class represents the UI of the hero */
public class HeroUI {
    // Logger
    private final Logger LOGGER = Logger.getLogger(HeroUI.class.getName());
    // Hero UI enthält lebensbalken, expbalken und level
    private HeroHealthBar healthBar;
    private HeroXPBar xpBar;
    private Label level;

    // ??
    private int previousHealthPoints;
    private long previousTotalXP;

    private record HeroData(HealthComponent hc, XPComponent xc) {}

    private HeroUI() {
        setup();
    }

    /** Updates the UI with the current data of the hero */
    public void update() {
        HeroData hd = buildDataObject();
        updateExperienceBar(hd);

        updateHealthBar(hd);
    }

    private void updateExperienceBar(HeroData hd) {
        if (hd.xc != null) {
            if (hd.xc.getTotalXP() != previousTotalXP)
                xpBar.createXPPopup(hd.xc.getTotalXP() - previousTotalXP);
            previousTotalXP = hd.xc.getTotalXP();
            level.setText("Level: " + hd.xc.currentLevel());
            xpBar.updateXPBar(hd.xc);
        }
    }

    private void updateHealthBar(HeroData hd) {
        if (hd.hc != null) {
            if (hd.hc.currentHealthpoints() != previousHealthPoints)
                healthBar.createHPPopup(hd.hc.currentHealthpoints() - previousHealthPoints);
            previousHealthPoints = hd.hc.currentHealthpoints();
            healthBar.updateHealthBar(hd.hc);
        }
    }

    private HeroData buildDataObject() {
        HealthComponent hc = Game.hero().flatMap(e -> e.fetch(HealthComponent.class)).orElse(null);
        XPComponent xc = Game.hero().flatMap(e -> e.fetch(XPComponent.class)).orElse(null);

        return new HeroData(hc, xc);
    }

    private void setup() {
        HeroData hd = buildDataObject();

        if (hd.xc != null) {
            previousTotalXP = hd.xc.getTotalXP();
            level = new Label("Level: ", new Skin());
            Game.stage().get().addActor(level);

            xpBar = new HeroXPBar();
            Game.stage().get().addActor(xpBar);
        } else {
            LOGGER.warning("Couldn't create hero xp bar because of missing XPComponent");
        }

        if (hd.hc != null) {
            previousHealthPoints = hd.hc.currentHealthpoints();
            healthBar = new HeroHealthBar();
            Game.stage().get().addActor(healthBar);
        } else {
            LOGGER.warning("Couldn't create hero health bar because of missing HealthComponent");
        }
    }
}
