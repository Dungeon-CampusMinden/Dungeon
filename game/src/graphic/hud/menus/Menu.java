package graphic.hud.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controller.ScreenController;
import graphic.hud.*;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import starter.Game;
import tools.Point;

/**
 * Base class for the menus / container for the menu elements.
 *
 * @param <T> any class that extends or is an Actor
 */
public class Menu<T extends Actor> extends ScreenController<T> {

    private Table container = new Table();
    private String headline;
    private LinkedHashSet<IMenuItem> items;
    private boolean isVisible = false;

    private static final Logger menuLogger = Logger.getLogger(Menu.class.getName());

    /**
     * The Menu constructor. Builds a new menu screen from a given headline and a set of screen
     * elements. Creates a Screencontroller with a ScalingViewport which stretches the
     * ScreenElements on resize.
     *
     * @param title the menu headline
     * @param elements the set of elements that make up the menu screen
     */
    public Menu(String title, LinkedHashSet<IMenuItem> elements) {
        this(new SpriteBatch(), title, elements);
    }

    /**
     * The Menu constructor. Builds a new menu screen from a given headline and a set of screen
     * elements. Creates a Screencontroller with a ScalingViewport which stretches the
     * ScreenElements on resize.
     *
     * @param batch the batch which should be used to draw with
     * @param title the menu headline
     * @param elements the set of elements that make up the menu screen
     */
    public Menu(SpriteBatch batch, String title, LinkedHashSet<IMenuItem> elements) {
        super(batch);

        headline = title;
        items = elements;

        container.setFillParent(true);
        container.setRound(false);

        container.add(setUpHeadline(headline)).spaceBottom(20.0f);
        container.row();

        for (IMenuItem item : items) {
            container.add((T) item).spaceBottom(10.0f);
            container.row();
        }

        container.center();

        add((T) container);

        hideMenu();
    }

    /**
     * Generates all elements needed to build the Main Menu
     *
     * @return a set of elements that make up the main menu
     */
    public static LinkedHashSet<IMenuItem> generateMainMenu() {
        LinkedHashSet<IMenuItem> mainMenuItems = new LinkedHashSet<>();

        MenuButton startGameButton =
                new MenuButton(
                        "Start",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        startGameButton.getLabel().setFontScale(1.5f);
        startGameButton.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("Starting game ...");
                        Game.getGame().toggleMainMenu();
                        Game.toggleSystems();
                    }
                });

        MenuButton optionsButton =
                new MenuButton(
                        "Options",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        optionsButton.getLabel().setFontScale(1.5f);
        optionsButton.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("Closing main menu and opening options ...");
                        Game.getGame().toggleMainMenu();
                        Game.getGame().toggleOptions();
                    }
                });

        MenuButton closeGameButton =
                new MenuButton(
                        "Close",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        closeGameButton.getLabel().setFontScale(1.5f);
        closeGameButton.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("Closing game ...");
                        Gdx.app.exit();
                    }
                });

        mainMenuItems.add(startGameButton);
        mainMenuItems.add(optionsButton);
        mainMenuItems.add(closeGameButton);

        return mainMenuItems;
    }

    /**
     * Generates all elements needed to build the Options Menu
     *
     * @return a set of elements that make up the options menu
     */
    public static LinkedHashSet<IMenuItem> generateOptionsMenu() {
        LinkedHashSet<IMenuItem> optionsMenuItems = new LinkedHashSet<>();

        MenuButton optionA =
                new MenuButton(
                        "Option A",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        optionA.getLabel().setFontScale(1.5f);
        optionA.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("'Option A' button was clicked.");
                    }
                });

        MenuButton optionB =
                new MenuButton(
                        "Option B",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        optionB.getLabel().setFontScale(1.5f);
        optionB.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("'Option B' button was clicked.");
                    }
                });

        MenuButton optionC =
                new MenuButton(
                        "Option C",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        optionC.getLabel().setFontScale(1.5f);
        optionC.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("'Option C' button was clicked.");
                    }
                });

        MenuButton backButton =
                new MenuButton(
                        "Back",
                        new Point(0.0f, 0.0f),
                        new TextButtonListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {}
                        });

        backButton.getLabel().setFontScale(1.5f);
        backButton.executeAction(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        menuLogger.info("Closing the options and opening the main menu ...");
                        Game.getGame().toggleOptions();
                        Game.getGame().toggleMainMenu();
                    }
                });

        optionsMenuItems.add(optionA);
        optionsMenuItems.add(optionB);
        optionsMenuItems.add(optionC);
        optionsMenuItems.add(backButton);

        return optionsMenuItems;
    }

    /** Makes all elements inside of a and thus the menu itself visible. */
    public void showMenu() {
        this.setVisible(true);
        this.forEach((Actor s) -> s.setVisible(true));
    }

    /** Makes all elements inside of a and thus the menu itself invisible. */
    public void hideMenu() {
        this.setVisible(false);
        this.forEach((Actor s) -> s.setVisible(false));
    }

    /**
     * Returns if a menu is visible or not.
     *
     * @return visibility of the menu
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Sets a menu's visibility attribute.
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    private ScreenText setUpHeadline(String title) {
        ScreenText menuTitle =
                new ScreenText(
                        title,
                        new Point(0.0f, 0.0f),
                        3,
                        new LabelStyleBuilder((FontBuilder.DEFAULT_FONT))
                                .setFontcolor(Color.RED)
                                .build());

        menuTitle.setFontScale(2.5f);

        return menuTitle;
    }

    private void addItem(IMenuItem item) {
        items.add(item);
    }

    private void removeItem(IMenuItem item) {
        items.remove(item);
    }
}
