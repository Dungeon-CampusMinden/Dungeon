package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.CursorPositionTargetSelection;
import contrib.utils.components.skill.ITargetSelection;


public class ITargetSelectionSerializer extends Serializer<ITargetSelection> {
    @Override
    public void write(Kryo kryo, Output output, ITargetSelection object) {

    }

    @Override
    public ITargetSelection read(Kryo kryo, Input input, Class<ITargetSelection> type) {
        // Read CursorPositionTargetSelection because it is the only implementation of ITargetSelection yet
        return kryo.readObject(input, CursorPositionTargetSelection.class);
    }
}
