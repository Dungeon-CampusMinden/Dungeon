package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.ScreenButton;
import graphic.hud.widgets.TextButtonListener;
import graphic.hud.widgets.TextButtonStyleBuilder;
import tools.Constants;
import tools.Point;

import java.util.ArrayList;

public class StartMenu<T extends Actor> extends Menu<T> {

    private enum MenuType {
        GameMode,
        MultiplayerStartOrJoinSession,
        MultiplayerJoinSession
    }

    private MenuType menuTypeCurrent;
    private final ScreenButton buttonNavigateBack;
    private final GameModeMenu gameModeMenu;
    private final MultiplayerStartOrJoinSessionMenu multiplayerModeMenu;
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
        multiplayerModeMenu = new MultiplayerStartOrJoinSessionMenu(batch, this.stage);
        multiplayerJoinSessionMenu = new MultiplayerJoinSessionMenu(batch, this.stage);

        buttonNavigateBack = new ScreenButton(
            "<",
            new Point(15, Constants.WINDOW_HEIGHT - 30),
            new TextButtonListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    navigateBack();
                }
            },
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.WHITE)
                .build()
        );
        buttonNavigateBack.getLabel().setFontScale(buttonTextLabelScale);
        add((T)buttonNavigateBack);

        gameModeMenu.getButtonSinglePlayer().addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               observers.forEach((IStartMenuObserver observer) -> observer.onSinglePlayerModeChosen());
           }
        });
        gameModeMenu.getButtonMultiPlayer().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerStartOrJoinSession);
            }
        });
        multiplayerModeMenu.getButtonStartSession().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                observers.forEach((IStartMenuObserver observer) -> observer.onMultiPlayerHostModeChosen());
            }
        });
        multiplayerModeMenu.getButtonJoinSession().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveMenu(MenuType.MultiplayerJoinSession);
            }
        });
        multiplayerJoinSessionMenu.getButtonJoin().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String[] temp = multiplayerJoinSessionMenu.getInputHostIpPort().getText().split(":");
                // length has to be 2 => address and port
                if (temp.length == 2) {
                    String address = temp[0];
                    try {
                        Integer port = Integer.parseInt(temp[1]);
                        observers.forEach((IStartMenuObserver observer) -> observer.onMultiPlayerClientModeChosen(address, port));
                    } catch (NumberFormatException e) {
                        // TODO: show error message, that port is invalid
                    }
                }
            }
        });

        setActiveMenu(MenuType.GameMode);
    }

    @Override
    public void hideMenu() {
        super.hideMenu();
        gameModeMenu.hideMenu();
        multiplayerModeMenu.hideMenu();
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
        multiplayerJoinSessionMenu.hideMenu();
        menuTypeCurrent = menuType;

        switch (menuType) {
            case GameMode -> {
                gameModeMenu.showMenu();
            }
            case MultiplayerStartOrJoinSession -> {
                multiplayerModeMenu.showMenu();
            }
            case MultiplayerJoinSession -> {
                multiplayerJoinSessionMenu.showMenu();
            }
            default -> throw new RuntimeException("Invalid menu type");
        }

        buttonNavigateBack.setVisible(menuType != MenuType.GameMode);
        this.update();
    }

    private void navigateBack() {
        switch (menuTypeCurrent) {
            case MultiplayerStartOrJoinSession -> {
                setActiveMenu(MenuType.GameMode);
            }
            case MultiplayerJoinSession -> {
                setActiveMenu(MenuType.MultiplayerStartOrJoinSession);
            }
        }
    }
}
