package mp.packages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import level.elements.tile.Tile;

// Custom Serializer to avoid cyclic dependencies between ILevel and Tile
public class TileSerializer extends TaggedFieldSerializer<Tile> {
    public TileSerializer(Kryo kryo, Class<?> type) {
        super(kryo, type);
    }

    @Override
    public void write(Kryo kryo, Output output, Tile object) {
        object.setLevel(null);
        super.write(kryo, output, object);
    }
}
