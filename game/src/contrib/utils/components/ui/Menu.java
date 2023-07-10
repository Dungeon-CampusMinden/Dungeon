package contrib.utils.components.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import core.Entity;
import core.Game;
import core.components.UIComponent;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.logging.Logger;

/** Base class for the menus / container for the menu elements. */
public class Menu {
    // logger xD
    private static final Logger menuLogger = Logger.getLogger(Menu.class.getName());

    public static Entity createMenu(String title, LinkedHashSet<Actor> elements) {
        Entity Menue = new Entity();
        // Tabelle für Design
        Table container = new Table();
        // Title
        String headline = title;
        // Alle sub elemente
        LinkedHashSet<Actor> items = elements;

        container.setFillParent(true);
        container.setRound(false);

        Label menuTitle = new Label(headline, new Skin());

        menuTitle.setFontScale(2.5f);

        container.add(menuTitle).spaceBottom(20.0f);
        container.row();

        for (Actor item : items) {
            container.add(item).spaceBottom(10.0f);
            container.row();
        }

        container.center();

        container.setVisible(false);

        new UIComponent(Menue, container, true);
        return Menue;
    }

    /**
     * Generates all elements needed to build the Main Menu
     *
     * @return a set of elements that make up the main menu
     */
    public static LinkedHashSet<Actor> generateMainMenu() {
        LinkedHashSet<Actor> mainMenuItems = new LinkedHashSet<>();

        TextButton startGameButton =
                createMenuButton(
                        "Start Game",
                        (event) -> {
                            menuLogger.info("Starting game ...");
                            // hide MainMenu
                            hideActiveMenu();
                            // prepare the systems
                            startGame();
                            // not used at the moment
                            return true;
                        });

        // change to optionsmenu
        TextButton optionsButton =
                createMenuButton(
                        "Options",
                        (event) -> {
                            menuLogger.info("Closing main menu and opening options ...");
                            changeMenu(MenueStates.Options);
                            return true;
                        });

        TextButton closeGameButton =
                createMenuButton(
                        "Close",
                        (event) -> {
                            menuLogger.info("Closing game ...");
                            closeGame();
                            return true;
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
    public static LinkedHashSet<Actor> generateOptionsMenu() {
        LinkedHashSet<Actor> optionsMenuItems = new LinkedHashSet<>();

        TextButton optionA =
                createMenuButton(
                        "Option A",
                        (event) -> {
                            menuLogger.info("'Option A' button was clicked.");
                            return false;
                        });

        TextButton optionB =
                createMenuButton(
                        "Option B",
                        (event) -> {
                            menuLogger.info("'Option B' button was clicked.");

                            return false;
                        });

        TextButton optionC =
                createMenuButton(
                        "Option C",
                        (event) -> {
                            menuLogger.info("'Option C' button was clicked.");
                            return false;
                        });

        TextButton backButton =
                createMenuButton(
                        "Back",
                        inputEvent -> {
                            menuLogger.info("Closing the options and opening the main menu ...");
                            changeMenu(MenueStates.MainMenu);

                            return true;
                        });

        optionsMenuItems.add(optionA);
        optionsMenuItems.add(optionB);
        optionsMenuItems.add(optionC);
        optionsMenuItems.add(backButton);

        return optionsMenuItems;
    }

    private static HashMap<MenueStates, Entity> menues = new HashMap<>();
    private static Entity activeMenu;

    private static void startGame() {
        // do more magic =)
        Game.systems().forEach((k, v) -> v.run());
    }

    private static void closeGame() {
        Gdx.app.exit();
    }

    private static void changeMenu(MenueStates state) {
        var newMenu = menues.get(state);
        if (newMenu != null) {
            hideActiveMenu();
            newMenu.fetch(UIComponent.class).orElseThrow().dialog().setVisible(true);
            activeMenu = newMenu;
        }
    }

    private static void hideActiveMenu() {
        activeMenu.fetch(UIComponent.class).orElseThrow().dialog().setVisible(false);
    }

    public static TextButton createMenuButton(
            String description, Function<InputEvent, Boolean> onClick) {
        // TODO: Skin anhängen
        TextButton textButton = new TextButton(description, new Skin());
        // allows defining button behaviour
        textButton.addListener(createClickListener(onClick));

        return textButton;
    }

    private static ClickListener createClickListener(Function<InputEvent, Boolean> onClick) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.apply(event);
            }
        };
    }
}
