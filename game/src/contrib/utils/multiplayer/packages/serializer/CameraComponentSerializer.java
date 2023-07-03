package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.components.CameraComponent;

public class CameraComponentSerializer extends Serializer<CameraComponent> {
    @Override
    public void write(Kryo kryo, Output output, CameraComponent object) {

    }

    @Override
    public CameraComponent read(Kryo kryo, Input input, Class<CameraComponent> type) {
        return null;
    }
}
