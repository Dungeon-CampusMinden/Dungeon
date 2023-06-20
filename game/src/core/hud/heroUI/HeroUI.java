package core.hud.heroUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.HealthComponent;
import contrib.components.XPComponent;

import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/** This class represents the UI of the hero */
public class HeroUI {
    private static final HeroUI heroUI = new HeroUI();
    private final Logger LOGGER = Logger.getLogger(HeroUI.class.getName());
    private final Set<EnemyHealthBar> enemyHealthBars;
    private Label level;
    private HeroHealthBar healthBar;
    private HeroXPBar xpBar;
    private BitmapFont font;
    private int previousHealthPoints;
    private long previousTotalXP;

    private record HeroData(HealthComponent hc, XPComponent xc) {}

    private HeroUI() {
        enemyHealthBars = new HashSet<>();
        setup();
    }

    /** Updates the UI with the current data of the hero */
    public void update() {
        updateEnemyHealthBars();
        HeroData hd = buildDataObject();
        if (hd.xc != null) {
            if (hd.xc.getTotalXP() != previousTotalXP)
                xpBar.createXPPopup(hd.xc.getTotalXP() - previousTotalXP, font);
            previousTotalXP = hd.xc.getTotalXP();
            level.setText("Level: " + hd.xc.currentLevel());
            xpBar.updateXPBar(hd.xc);
        }

        if (hd.hc != null) {
            if (hd.hc.currentHealthpoints() != previousHealthPoints)
                healthBar.createHPPopup(hd.hc.currentHealthpoints() - previousHealthPoints, font);
            previousHealthPoints = hd.hc.currentHealthpoints();
            healthBar.updateHealthBar(hd.hc);
        }
    }

    /**
     * Creates a HealthBar for each entity that has a HealthComponent, PositionComponent and is not
     * the hero
     */
    public void createEnemyHealthBars() {
        this.clearEnemyHealthBars();
        Game.entityStream()
                .filter(e -> e.isPresent(HealthComponent.class))
                .filter(e -> e.isPresent(PositionComponent.class))
                .filter(e -> !e.isPresent(PlayerComponent.class))
                .forEach(
                        e -> {
                            EnemyHealthBar enemyHealthBar = new EnemyHealthBar(e);
                            enemyHealthBars.add(enemyHealthBar);
                        });
    }

    private void clearEnemyHealthBars() {
        enemyHealthBars.forEach(Actor::remove);
        enemyHealthBars.clear();
    }

    private void updateEnemyHealthBars() {
        enemyHealthBars.forEach(EnemyHealthBar::update);
    }

    private HeroData buildDataObject() {
        HealthComponent hc = Game.hero().flatMap(e -> e.fetch(HealthComponent.class)).orElse(null);
        XPComponent xc = Game.hero().flatMap(e -> e.fetch(XPComponent.class)).orElse(null);

        return new HeroData(hc, xc);
    }

    private void setup() {
        HeroData hd = buildDataObject();

        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("skin/DungeonFont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 21;
        font = generator.generateFont(parameter);

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

    /**
     * Returns the instance of the HeroUI
     *
     * @return the instance of the HeroUI
     */
    public static HeroUI getHeroUI() {
        return heroUI;
    }
}
