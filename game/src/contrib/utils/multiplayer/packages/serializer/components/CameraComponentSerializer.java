package contrib.utils.multiplayer.packages.serializer.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import core.components.CameraComponent;

/** Custom serializer to send and retrieve objects of {@link CameraComponent}. */
public class CameraComponentSerializer extends Serializer<CameraComponent> {
    @Override
    public void write(Kryo kryo, Output output, CameraComponent object) {}

    @Override
    public CameraComponent read(Kryo kryo, Input input, Class<CameraComponent> type) {
        return null;
    }
}
