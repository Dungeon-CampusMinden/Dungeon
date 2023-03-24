package graphic.hud.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryo.NotNull;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.ScreenButton;
import graphic.hud.widgets.TextButtonListener;
import graphic.hud.widgets.TextButtonStyleBuilder;
import tools.Constants;
import tools.Point;

public class StartMenu<T extends Actor> extends Menu<T> {

    private enum MenuType {
        GameMode,
        MultiplayerHostOrJoin,
        MultiplayerOpenToLan,
        MultiplayerJoinSession
    }

    private Menu menuPrevious;
    private Menu menuCurrent;
    private final ScreenButton buttonNavigateBack;
    private final GameModeMenu gameModeMenu;
    private final MultiplayerMenu multiplayerModeMenu;
    private final MultiplayerOpenToLanMenu multiplayerOpenToLanMenu;
    private final MultiplayerJoinSessionMenu multiplayerJoinSessionMenu;

    public StartMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public StartMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        gameModeMenu = new GameModeMenu(batch, this.stage);
        multiplayerModeMenu = new MultiplayerMenu(batch, this.stage);
        multiplayerOpenToLanMenu = new MultiplayerOpenToLanMenu(batch, this.stage);
        multiplayerJoinSessionMenu = new MultiplayerJoinSessionMenu(batch, this.stage);

        buttonNavigateBack = new ScreenButton(
            "Back",
            new Point(10, Constants.WINDOW_HEIGHT - 25),
            new TextButtonListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    navigateBack();
                }
            },
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.YELLOW)
                .build()
        );
        buttonNavigateBack.setVisible(false);
        add((T)buttonNavigateBack);

        gameModeMenu.getButtonSinglePlayer().addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
           // TODO: emit Single player decision
           }
        });
        gameModeMenu.getButtonMultiPlayer().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerHostOrJoin);
                menuPrevious = gameModeMenu;
            }
        });
        multiplayerModeMenu.getButtonOpenToLan().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerOpenToLan);
                menuPrevious = multiplayerModeMenu;
            }
        });
        multiplayerModeMenu.getButtonJoinSession().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerJoinSession);
                menuPrevious = multiplayerModeMenu;
            }
        });
        multiplayerOpenToLanMenu.getButtonOpen().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: emit start multiplayer AS HOST
            }
        });
        multiplayerJoinSessionMenu.getButtonJoin().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: emit start multiplayer ONLY AS CLIENT
            }
        });

        setActiveMenu(MenuType.GameMode);
    }

    private void setActiveMenu(MenuType menuType) {
        gameModeMenu.hideMenu();
        multiplayerModeMenu.hideMenu();
        multiplayerOpenToLanMenu.hideMenu();
        multiplayerJoinSessionMenu.hideMenu();

        switch (menuType) {
            case GameMode -> {
                menuCurrent = gameModeMenu;
                gameModeMenu.showMenu();
            }
            case MultiplayerHostOrJoin -> {
                menuCurrent = multiplayerModeMenu;
                multiplayerModeMenu.showMenu();
            }
            case MultiplayerOpenToLan -> {
                menuCurrent = multiplayerOpenToLanMenu;
                multiplayerOpenToLanMenu.showMenu();
            }
            case MultiplayerJoinSession -> {
                menuCurrent = multiplayerJoinSessionMenu;
                multiplayerJoinSessionMenu.showMenu();
            }
            default -> throw new RuntimeException("Invalid menu type");
        }

        buttonNavigateBack.setVisible(menuType != MenuType.GameMode);
    }

    private void navigateBack() {
        if (menuPrevious != null) {
            Menu menuTemp = menuCurrent;
            menuCurrent = menuPrevious;
            menuPrevious = menuTemp;
            menuPrevious.hideMenu();
            menuCurrent.showMenu();
        }
    }
}
