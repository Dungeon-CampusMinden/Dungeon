package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import level.elements.ILevel;
import level.elements.tile.Tile;

import java.lang.reflect.Constructor;

public class ILevelSerializer extends Serializer<ILevel> {
    @Override
    public void write(Kryo kryo, Output output, ILevel object) {
        Class<? extends ILevel> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
        kryo.writeObject(output, object.getLayout());
    }

    @Override
    public ILevel read(Kryo kryo, Input input, Class<ILevel> type) {
        Class<? extends ILevel> concreteClass = kryo.readClass(input).getType();
        Tile[][] layout = kryo.readObject(input, Tile[][].class);
        try {
            Constructor<? extends ILevel> constructor = concreteClass.getConstructor(Tile[][].class);
            ILevel instance = constructor.newInstance(new Object[]{layout});
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
