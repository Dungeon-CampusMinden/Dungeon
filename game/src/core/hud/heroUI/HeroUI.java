package core.hud.heroUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;

import contrib.components.HealthComponent;
import contrib.components.XPComponent;

import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.hud.LabelStyleBuilder;
import core.hud.ScreenText;
import core.utils.Constants;
import core.utils.Point;
import core.utils.controller.ScreenController;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/** This class represents the UI of the hero */
public class HeroUI<T extends Actor> extends ScreenController<T> {
    private static final HeroUI<Actor> heroUI = new HeroUI<>(new SpriteBatch());
    private final Logger LOGGER = Logger.getLogger(HeroUI.class.getName());
    private final Set<EnemyHealthBar> enemyHealthBars;
    private ScreenText level;
    private HeroHealthBar healthBar;
    private HeroXPBar xpBar;
    private BitmapFont font;
    private int previousHealthPoints;
    private long previousTotalXP;

    private record HeroData(HealthComponent hc, XPComponent xc) {}

    private HeroUI(SpriteBatch batch) {
        super(batch);
        enemyHealthBars = new HashSet<>();
        setup();
    }

    /** Updates the UI with the current data of the hero */
    @Override
    public void update() {
        super.update();
        updateEnemyHealthBars();
        HeroData hd = buildDataObject();
        if (hd.xc != null) {
            if (hd.xc.getTotalXP() != previousTotalXP)
                xpBar.createXPPopup(hd.xc.getTotalXP() - previousTotalXP, font);
            previousTotalXP = hd.xc.getTotalXP();
            level.setText("Level: " + hd.xc.getCurrentLevel());
            xpBar.updateXPBar(hd.xc);
        }

        if (hd.hc != null) {
            if (hd.hc.getCurrentHealthpoints() != previousHealthPoints)
                healthBar.createHPPopup(
                        hd.hc.getCurrentHealthpoints() - previousHealthPoints, font);
            previousHealthPoints = hd.hc.getCurrentHealthpoints();
            healthBar.updateHealthBar(hd.hc);
        }
    }

    /**
     * Creates a HealthBar for each entity that has a HealthComponent, PositionComponent and is not
     * the hero
     */
    public void createEnemyHealthBars() {
        this.clearEnemyHealthBars();
        Game.getEntitiesStream()
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
        enemyHealthBars.forEach(enemyHealthBar -> this.remove((T) enemyHealthBar));
        enemyHealthBars.clear();
    }

    private void updateEnemyHealthBars() {
        enemyHealthBars.forEach(EnemyHealthBar::update);
    }

    private HeroData buildDataObject() {
        HealthComponent hc =
                (HealthComponent)
                        Game.getHero()
                                .flatMap(e -> e.getComponent(HealthComponent.class))
                                .orElse(null);
        XPComponent xc =
                (XPComponent)
                        Game.getHero().flatMap(e -> e.getComponent(XPComponent.class)).orElse(null);

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
            level =
                    new ScreenText(
                            "Level: ",
                            new Point(3, 35),
                            1,
                            new LabelStyleBuilder(font).setFontcolor(Color.GREEN).build());
            this.add((T) level);

            xpBar = new HeroXPBar("hud/xpBar/xpBar_7.png", new Point(0, 5), 1.9f);
            this.add((T) xpBar);
        } else {
            LOGGER.warning("Couldn't create hero xp bar because of missing XPComponent");
        }

        if (hd.hc != null) {
            previousHealthPoints = hd.hc.getCurrentHealthpoints();
            healthBar =
                    new HeroHealthBar(
                            "hud/healthBar/healthBar_7.png",
                            new Point(Constants.WINDOW_WIDTH - 195, 5),
                            1.9f);
            this.add((T) healthBar);
        } else {
            LOGGER.warning("Couldn't create hero health bar because of missing HealthComponent");
        }
    }

    /**
     * Returns the instance of the HeroUI
     *
     * @return the instance of the HeroUI
     */
    public static HeroUI<Actor> getHeroUI() {
        return heroUI;
    }
}
