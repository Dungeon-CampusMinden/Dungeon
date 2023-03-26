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
import mp.packages.request.InitializeServerRequest;
import mp.packages.request.JoinSessionRequest;
import mp.packages.request.PingRequest;
import mp.packages.request.UpdateOwnPositionRequest;
import mp.packages.response.InitializeServerResponse;
import mp.packages.response.JoinSessionResponse;
import mp.packages.response.PingResponse;
import mp.packages.serializer.*;
import tools.Point;

import java.util.ArrayList;

public class NetworkSetup {

    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        kryo.addDefaultSerializer(Tile.class, new TileSerializer());
        kryo.addDefaultSerializer(ILevel.class, new ILevelSerializer());

        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(InitializeServerRequest.class, new InitializeServerRequestSerializer());
        kryo.register(InitializeServerResponse.class, new InitializeServerResponseSerializer());
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
    }
}
