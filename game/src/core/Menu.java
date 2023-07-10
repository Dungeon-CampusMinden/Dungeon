package core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import core.utils.Constants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

/** Sample screen for demonstration and manual testing of multiplayer mode. */
public class Menu extends ScreenAdapter {

    private static final Logger LOGGER = Logger.getLogger("Menu");
    private static Menu INSTANCE;
    private final Stage stage;
    private final Skin skin;
    private final Table table;
    private final TextButton buttonSinglePlayer;
    private final TextButton buttonMultiPlayer;
    private final TextButton buttonStartNewSession;
    private final TextButton buttonJoinExistingSession;
    private final TextButton buttonExit;
    private final TextButton buttonNavigateBack;
    private final TextField inputHostIpPort;
    private final TextButton buttonJoin;
    private final Label textInvalidAddress;
    private final ArrayList<IMenuScreenObserver> observers;
    private final String deviceIpAddress;
    private MenuType menuTypeCurrent;

    private enum MenuType {
        GameModeChoice,
        MultiplayerStartOrJoinSession,
        MultiplayerJoinSession
    }

    private Menu() {
        skin = new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG));
        stage = new Stage(new ScreenViewport());
        buttonSinglePlayer = new TextButton("SinglePlayer", skin);
        buttonMultiPlayer = new TextButton("MultiPlayer", skin);
        buttonStartNewSession = new TextButton("Start session", skin);
        buttonJoinExistingSession = new TextButton("Join session", skin);
        buttonExit = new TextButton("Exit", skin);
        buttonNavigateBack = new TextButton("Back", skin);
        inputHostIpPort = new TextField("", skin);
        buttonJoin = new TextButton("Connect", skin);
        textInvalidAddress = new Label("Invalid Address", skin);
        textInvalidAddress.setColor(Color.RED);

        table = new Table();

        observers = new ArrayList<>();

        registerListeners();

        String tempDeviceIpAddress;
        try {
            tempDeviceIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            tempDeviceIpAddress = "???";
        }
        deviceIpAddress = tempDeviceIpAddress;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        table.setFillParent(true);
        stage.addActor(table);

        setActiveMenu(MenuType.GameModeChoice);
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell our stage to do actions and draw itself
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void addListener(IMenuScreenObserver observer) {
        observers.add(observer);
    }

    public void removeListener(IMenuScreenObserver observer) {
        observers.remove(observer);
    }

    public static Menu getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Menu();
        }

        return INSTANCE;
    }

    private void registerListeners() {
        buttonExit.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.exit();
                    }
                });

        buttonNavigateBack.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        navigateBack();
                    }
                });

        buttonSinglePlayer.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        observers.forEach(
                                (IMenuScreenObserver observer) ->
                                        observer.onSinglePlayerModeChosen());
                    }
                });

        buttonMultiPlayer.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        setActiveMenu(MenuType.MultiplayerStartOrJoinSession);
                    }
                });

        buttonStartNewSession.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        observers.forEach(
                                (IMenuScreenObserver observer) ->
                                        observer.onMultiPlayerHostModeChosen());
                    }
                });

        buttonJoinExistingSession.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        setActiveMenu(MenuType.MultiplayerJoinSession);
                    }
                });

        buttonJoin.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        String[] temp = inputHostIpPort.getText().split(":");
                        try (Socket socket = new Socket()) {
                            String address = temp[0];
                            int port = Integer.parseInt(temp[1]);
                            socket.connect(new InetSocketAddress(address, port), 1000);
                            socket.close();
                            observers.forEach(
                                    (IMenuScreenObserver observer) ->
                                            observer.onMultiPlayerClientModeChosen(address, port));
                        } catch (Exception e) {
                            textInvalidAddress.setVisible(true);
                        }
                    }
                });
    }

    private void setActiveMenu(MenuType menuType) {
        table.clear();
        switch (menuType) {
            case GameModeChoice -> {
                table.add(buttonSinglePlayer).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonMultiPlayer).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonExit).fillX().uniformX();
                menuTypeCurrent = MenuType.GameModeChoice;
            }
            case MultiplayerStartOrJoinSession -> {
                table.add(buttonStartNewSession).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonJoinExistingSession).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonNavigateBack).fillX().uniformX();
                menuTypeCurrent = MenuType.MultiplayerStartOrJoinSession;
            }
            case MultiplayerJoinSession -> {
                inputHostIpPort.setText(String.format("%s:%s", deviceIpAddress, "xxxxx"));
                table.add(inputHostIpPort).fillX().uniformX();
                table.row();
                table.add(textInvalidAddress).fillX().uniformX();
                textInvalidAddress.setVisible(false);
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonJoin).fillX().uniformX();
                table.row().pad(10, 0, 10, 0);
                table.row();
                table.add(buttonNavigateBack).fillX().uniformX();
                menuTypeCurrent = MenuType.MultiplayerJoinSession;
            }
            default -> {
                final String message = "Failed to switch menu elements. Invalid menu type given.";
                LOGGER.severe(message);
                throw new RuntimeException("Invalid menu type.");
            }
        }
    }

    private void navigateBack() {
        switch (menuTypeCurrent) {
            case MultiplayerStartOrJoinSession -> setActiveMenu(MenuType.GameModeChoice);
            case MultiplayerJoinSession -> setActiveMenu(MenuType.MultiplayerStartOrJoinSession);
        }
    }
}
