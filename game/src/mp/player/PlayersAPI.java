package mp.player;

import java.util.ArrayList;

/** Manages the players. */
public class PlayersAPI {
    private final ArrayList<IPlayer> players = new ArrayList<>();

    public ArrayList<IPlayer> getPlayers() {
        return players;
    }

    public void addPlayer(IPlayer player) {
        players.add(player);
    }
}
