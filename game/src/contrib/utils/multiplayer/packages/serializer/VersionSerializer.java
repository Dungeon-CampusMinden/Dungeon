package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.StatsComponent;
import contrib.utils.multiplayer.packages.Version;

/**
 * Custom serializer to send and retrieve objects of {@link Version}.
 */
public class VersionSerializer extends Serializer<Version> {
    @Override
    public void write(Kryo kryo, Output output, Version version) {
        output.writeInt(version.major());
        output.writeInt(version.minor());
        output.writeInt(version.patch());
    }

    @Override
    public Version read(Kryo kryo, Input input, Class<Version> version) {
        final int major = input.readInt();
        final int minor = input.readInt();
        final int patch = input.readInt();
        return new Version(major, minor, patch);
    }
}
