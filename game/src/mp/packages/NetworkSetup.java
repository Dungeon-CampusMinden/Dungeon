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
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;
import mp.packages.serializer.*;

import java.util.ArrayList;

public class NetworkSetup {

    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        kryo.addDefaultSerializer(Tile.class, new TileSerializer());
        kryo.addDefaultSerializer(ILevel.class, new ILevelSerializer());

        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(LoadMapRequest.class);
        kryo.register(LoadMapResponse.class, new LoadMapResponseSerializer());
        kryo.register(ArrayList.class);
        kryo.register(Coordinate.class, new CoordinateSerializer());
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
    }
}
