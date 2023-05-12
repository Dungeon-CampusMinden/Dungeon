package mp.packages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.elements.astar.TileHeuristic;
import level.elements.tile.*;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import mp.packages.GameState;
import mp.packages.request.*;
import mp.packages.response.*;
import mp.packages.event.GameStateUpdateEvent;
import mp.packages.serializer.*;
import tools.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSetup {

    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        kryo.addDefaultSerializer(Tile.class, new TileSerializer());
        kryo.addDefaultSerializer(ILevel.class, new ILevelSerializer());

        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(InitServerRequest.class);
        kryo.register(InitServerResponse.class, new InitServerResponseSerializer());
        kryo.register(LoadMapRequest.class, new LoadMapRequestSerializer());
        kryo.register(LoadMapResponse.class, new LoadMapResponseSerializer());
        kryo.register(JoinSessionRequest.class);
        kryo.register(JoinSessionResponse.class, new JoinSessionResponseSerializer());
        kryo.register(ArrayList.class);
        kryo.register(Coordinate.class, new CoordinateSerializer());
        kryo.register(Point.class, new PointSerializer());
        kryo.register(ILevel.class);
        kryo.register(Tile[].class);
        kryo.register(Tile[][].class);
        kryo.register(TileLevel.class);
        kryo.register(TileHeuristic.class);
        kryo.register(ExitTile.class);
        kryo.register(DoorTile.class);
        kryo.register(FloorTile.class);
        kryo.register(WallTile.class);
        kryo.register(HoleTile.class);
        kryo.register(SkipTile.class);
        kryo.register(DesignLabel.class);
        kryo.register(LevelElement.class);
        kryo.register(UpdateOwnPositionRequest.class, new UpdateOwnPositionRequestSerializer());
        kryo.register(HashMap.class);
        kryo.register(GameStateUpdateEvent.class, new GameStateUpdateEventSerializer());
        kryo.register(GameState.class, new GameStateSerializer());
        kryo.register(UpdateOwnPositionResponse.class);
        kryo.register(ChangeMapRequest.class);
        kryo.register(ChangeMapResponse.class);
    }
}
