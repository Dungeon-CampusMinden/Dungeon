package core.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import contrib.components.HealthComponent;
import contrib.components.XPComponent;

import core.Game;
import core.utils.Constants;
import core.utils.Point;
import core.utils.controller.ScreenController;

/** This class represents the UI of the hero */
public class HeroUI<T extends Actor> extends ScreenController<T> {

    private static final HeroUI<Actor> heroUI = new HeroUI<>(new SpriteBatch());
    private ScreenText level;
    private ScreenImage healthBar, xpBar;
    private BitmapFont font;

    private record HeroData(HealthComponent hc, XPComponent xc) {}

    private HeroUI(SpriteBatch batch) {
        super(batch);
        setup();
    }

    /** Updates the UI with the current data of the hero */
    @Override
    public void update() {
        super.update();
        HeroData hd = buildDataObject();
        if (hd.xc != null) {
            level.setText("Level: " + hd.xc.getCurrentLevel());
            float xpPercentage =
                    (float) hd.xc.getCurrentXP()
                            / (hd.xc.getXPToNextLevel() + hd.xc.getCurrentXP())
                            * 100;
            if (xpPercentage <= 10) {
                xpBar.setTexture("hud/xpBar/xpBar_1.png");
            } else if (xpPercentage <= 20) {
                xpBar.setTexture("hud/xpBar/xpBar_2.png");
            } else if (xpPercentage <= 36) {
                xpBar.setTexture("hud/xpBar/xpBar_3.png");
            } else if (xpPercentage <= 52) {
                xpBar.setTexture("hud/xpBar/xpBar_4.png");
            } else if (xpPercentage <= 68) {
                xpBar.setTexture("hud/xpBar/xpBar_5.png");
            } else if (xpPercentage <= 84) {
                xpBar.setTexture("hud/xpBar/xpBar_6.png");
            } else if (xpPercentage <= 100) {
                xpBar.setTexture("hud/xpBar/xpBar_7.png");
            }
        }

        if (hd.hc != null) {
            float hpPercentage =
                    (float) hd.hc.getCurrentHealthpoints() / hd.hc.getMaximalHealthpoints() * 100;
            if (hpPercentage <= 0) {
                healthBar.setTexture("hud/healthBar/healthBar_7.png");
            } else if (hpPercentage <= 20) {
                healthBar.setTexture("hud/healthBar/healthBar_6.png");
            } else if (hpPercentage <= 36) {
                healthBar.setTexture("hud/healthBar/healthBar_5.png");
            } else if (hpPercentage <= 52) {
                healthBar.setTexture("hud/healthBar/healthBar_4.png");
            } else if (hpPercentage <= 68) {
                healthBar.setTexture("hud/healthBar/healthBar_3.png");
            } else if (hpPercentage <= 84) {
                healthBar.setTexture("hud/healthBar/healthBar_2.png");
            } else if (hpPercentage <= 100) {
                healthBar.setTexture("hud/healthBar/healthBar_1.png");
            }
        }
    }

    private HeroData buildDataObject() {
        HealthComponent hc = null;
        XPComponent xc = null;

        if (Game.getHero().flatMap(hero -> hero.getComponent(HealthComponent.class)).isPresent())
            hc =
                    (HealthComponent)
                            Game.getHero()
                                    .flatMap(hero -> hero.getComponent(HealthComponent.class))
                                    .orElseThrow();

        if (Game.getHero().flatMap(hero -> hero.getComponent(XPComponent.class)).isPresent())
            xc =
                    (XPComponent)
                            Game.getHero()
                                    .flatMap(hero -> hero.getComponent(XPComponent.class))
                                    .orElseThrow();

        return new HeroData(hc, xc);
    }

    /**
     * Creates a popup on the screen with the given lootXP
     *
     * @param lootXP the amount of XP to display
     */
    public void createXPPopup(long lootXP) {
        ScreenText xpPopup =
                new ScreenText(
                        "+" + lootXP + " XP",
                        new Point(
                                (float) Constants.WINDOW_WIDTH / 2,
                                (float) Constants.WINDOW_HEIGHT / 2),
                        1,
                        new LabelStyleBuilder(font).setFontcolor(Color.GREEN).build());
        xpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        this.add((T) xpPopup);
    }

    /**
     * Creates a popup on the screen with how much hp the hero lost
     *
     * @param damageAmount the amount of HP to display
     */
    public void createDamagePopup(int damageAmount) {
        ScreenText hpPopup =
                new ScreenText(
                        "-" + damageAmount + " HP",
                        new Point(Constants.WINDOW_WIDTH - 55, 30),
                        1,
                        new LabelStyleBuilder(font).setFontcolor(Color.RED).build());
        hpPopup.addAction(Actions.sequence(Actions.moveBy(0, 50, 1), Actions.removeActor()));
        this.add((T) hpPopup);
    }

    private void setup() {
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("skin/DungeonFont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 21;
        font = generator.generateFont(parameter);

        level =
                new ScreenText(
                        "Level: ",
                        new Point(3, 35),
                        1,
                        new LabelStyleBuilder(font).setFontcolor(Color.GREEN).build());
        this.add((T) level);

        xpBar = new ScreenImage("hud/xpBar/xpBar_1.png", new Point(0, 5), 1.9f);
        this.add((T) xpBar);

        healthBar =
                new ScreenImage(
                        "hud/healthBar/healthBar_7.png",
                        new Point(Constants.WINDOW_WIDTH - 195, 5),
                        1.9f);
        this.add((T) healthBar);
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
