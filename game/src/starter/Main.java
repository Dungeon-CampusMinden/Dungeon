package starter;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.systems.*;
import level.LevelAPI;
import level.elements.ILevel;
import mp.client.IMultiplayerClientObserver;
import mp.client.MultiplayerClient;
import mp.packages.request.LoadMapRequest;

public class Main implements IMultiplayerClientObserver {

    private static MultiplayerClient client;

    public Main() {
        setupClient();
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void onLevelReceived(ILevel level) {
        // start the game
        DesktopLauncher.run(new Game(level));
    }

    private void setupClient() {
        client = new MultiplayerClient();
        client.addObservers(this);
        LoadMapRequest loadMapRequest = new LoadMapRequest();
        client.send(loadMapRequest);

        while (true) {

        }
    }
}
