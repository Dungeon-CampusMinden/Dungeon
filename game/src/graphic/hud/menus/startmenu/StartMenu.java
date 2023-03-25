package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.menus.startmenu.GameModeMenu;
import graphic.hud.menus.startmenu.MultiplayerHostOrJoinMenu;
import graphic.hud.menus.startmenu.MultiplayerJoinSessionMenu;
import graphic.hud.menus.startmenu.MultiplayerOpenToLanMenu;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.ScreenButton;
import graphic.hud.widgets.TextButtonListener;
import graphic.hud.widgets.TextButtonStyleBuilder;
import mp.client.IMultiplayerClientObserver;
import tools.Constants;
import tools.Point;

import java.util.ArrayList;

public class StartMenu<T extends Actor> extends Menu<T> {

    private enum MenuType {
        GameMode,
        MultiplayerHostOrJoin,
        MultiplayerOpenToLan,
        MultiplayerJoinSession
    }

    private MenuType menuTypeCurrent;
    private final ScreenButton buttonNavigateBack;
    private final GameModeMenu gameModeMenu;
    private final MultiplayerHostOrJoinMenu multiplayerModeMenu;
    private final MultiplayerOpenToLanMenu multiplayerOpenToLanMenu;
    private final MultiplayerJoinSessionMenu multiplayerJoinSessionMenu;
    private final ArrayList<IStartMenuObserver> observers = new ArrayList<>();

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
        multiplayerModeMenu = new MultiplayerHostOrJoinMenu(batch, this.stage);
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
        add((T)buttonNavigateBack);

        gameModeMenu.getButtonSinglePlayer().addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               observers.forEach((IStartMenuObserver observer) -> observer.onGameModeChosen(GameMode.SinglePlayer));
           }
        });
        gameModeMenu.getButtonMultiPlayer().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerHostOrJoin);
            }
        });
        multiplayerModeMenu.getButtonOpenToLan().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerOpenToLan);
            }
        });
        multiplayerModeMenu.getButtonJoinSession().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerJoinSession);
            }
        });
        multiplayerOpenToLanMenu.getButtonOpen().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                observers.forEach((IStartMenuObserver observer) -> observer.onGameModeChosen(GameMode.MultiplayerHost));
            }
        });
        multiplayerJoinSessionMenu.getButtonJoin().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                observers.forEach((IStartMenuObserver observer) -> observer.onGameModeChosen(GameMode.MultiplayerClient));
            }
        });

        setActiveMenu(MenuType.GameMode);
    }

    @Override
    public void hideMenu() {
        super.hideMenu();
        gameModeMenu.hideMenu();
        multiplayerModeMenu.hideMenu();
        multiplayerOpenToLanMenu.hideMenu();
        multiplayerJoinSessionMenu.hideMenu();
    }

    public void resetView() {
        setActiveMenu(MenuType.GameMode);
    }

    public boolean addObserver(IStartMenuObserver observer) {
        return observers.add(observer);
    }

    public boolean removeObserver(IStartMenuObserver observer) {
        return observers.remove(observer);
    }

    private void setActiveMenu(MenuType menuType) {
        gameModeMenu.hideMenu();
        multiplayerModeMenu.hideMenu();
        multiplayerOpenToLanMenu.hideMenu();
        multiplayerJoinSessionMenu.hideMenu();
        menuTypeCurrent = menuType;

        switch (menuType) {
            case GameMode -> {
                gameModeMenu.showMenu();
            }
            case MultiplayerHostOrJoin -> {
                multiplayerModeMenu.showMenu();
            }
            case MultiplayerOpenToLan -> {
                multiplayerOpenToLanMenu.showMenu();
            }
            case MultiplayerJoinSession -> {
                multiplayerJoinSessionMenu.showMenu();
            }
            default -> throw new RuntimeException("Invalid menu type");
        }

        buttonNavigateBack.setVisible(menuType != MenuType.GameMode);
    }

    private void navigateBack() {
        switch (menuTypeCurrent) {
            case MultiplayerHostOrJoin -> {
                setActiveMenu(MenuType.GameMode);
            }
            case MultiplayerOpenToLan, MultiplayerJoinSession -> {
                setActiveMenu(MenuType.MultiplayerHostOrJoin);
            }
        }
    }
}
