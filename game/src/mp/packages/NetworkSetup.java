package mp.packages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.EndPoint;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.elements.astar.TileHeuristic;
import level.elements.tile.*;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import mp.packages.request.DataChunk;
import mp.packages.request.LoadMapRequest;
import mp.packages.request.PingRequest;
import mp.packages.response.LoadMapResponse;
import mp.packages.response.PingResponse;

import java.util.ArrayList;

public class NetworkSetup {

    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        DefaultSerializers.EnumSetSerializer enumNullableSerializer = new DefaultSerializers.EnumSetSerializer();
        enumNullableSerializer.setAcceptsNull(false);

        kryo.addDefaultSerializer(Tile.class, TileSerializer.class);
        kryo.register(byte[].class);
        kryo.register(DataChunk.class);
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        kryo.register(LoadMapRequest.class);
        kryo.register(LoadMapResponse.class);
        kryo.register(ArrayList.class);
        kryo.register(Coordinate.class);
        kryo.register(ILevel.class);
        kryo.register(Tile.class);
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
        kryo.register(DesignLabel.class, enumNullableSerializer);
        kryo.register(LevelElement.class, enumNullableSerializer);
    }
}
