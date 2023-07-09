package graphic.hud.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import core.Game;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.logging.Logger;

/** Base class for the menus / container for the menu elements. */
public class Menu {
    // Tabelle für Design
    private Table container = new Table();
    // Title
    private String headline;
    // Alle sub elemente
    private LinkedHashSet<Actor> items;
    // logger xD
    private static final Logger menuLogger = Logger.getLogger(Menu.class.getName());

    /**
     * The Menu constructor. Builds a new menu screen from a given headline and a set of screen
     * elements. Creates a Screencontroller with a ScalingViewport which stretches the
     * ScreenElements on resize.
     *
     * @param title the menu headline
     * @param elements the set of elements that make up the menu screen
     */
    public Menu(String title, LinkedHashSet<Actor> elements) {

        headline = title;
        items = elements;

        container.setFillParent(true);
        container.setRound(false);

        container.add(setUpHeadline(headline)).spaceBottom(20.0f);
        container.row();

        for (Actor item : items) {
            container.add(item).spaceBottom(10.0f);
            container.row();
        }

        container.center();

        isVisible(false);
    }

    public void isVisible(boolean visible){
        container.setVisible(visible);
    }



    private Label setUpHeadline(String title) {
        Label menuTitle = new Label(title, new Skin());

        menuTitle.setFontScale(2.5f);

        return menuTitle;
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

    private static void hideActiveMenu() {
        activeMenu.isVisible(false);
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

    private static HashMap<MenueStates, Menu> menues = new HashMap<>();
    private static Menu activeMenu;


    private static void startGame() {
        // do more magic =)
        Game.systems().forEach((k, v) -> v.run());
    }

    private static void closeGame() {
        Gdx.app.exit();
    }

    private static void changeMenu(MenueStates state){
        var newMenu = menues.get(state);
        if(newMenu != null){
            activeMenu.isVisible(false);
            newMenu.isVisible(true);
            activeMenu = newMenu;
        }
    }

    public static TextButton createMenuButton(
            String description, Function<InputEvent, Boolean> onClick) {
        // TODO: Skinn anhängen
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
