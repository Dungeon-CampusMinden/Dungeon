package core.hud.heroUI;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.HealthComponent;
import contrib.components.XPComponent;

import java.util.logging.Logger;

/** This class represents the UI of the hero */
public class HeroUI {
    // Logger
    private final Logger LOGGER = Logger.getLogger(HeroUI.class.getName());
    // Hero UI enthält expbalken und level
    private HeroXPBar xpBar;
    private Label level;

    public HeroUI(long level, float xpPercentage){

        this.level = new Label("Level: " + level, new Skin());

        xpBar = new HeroXPBar();
        xpBar.setValue(xpPercentage);
    }

    // benutzt um zu erkennen ob es eine Änderung in der Erfahrung gab.
    private long previousTotalXP;


    public void updateLevel(int level){
        this.level.setText("Level: " + level);
    }

    public void updateXPbar(float xpPercentage){
        xpBar.updateXPBar(xpPercentage);
    }

}
