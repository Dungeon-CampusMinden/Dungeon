package contrib.utils.multiplayer.network.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.skill.Skill;

/** Custom serializer to send and retrieve objects of {@link RangeAI}. */
public class RangeAiSerializer extends Serializer<RangeAI> {
    @Override
    public void write(Kryo kryo, Output output, RangeAI object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.attackRange());
        output.writeFloat(object.distance());
        kryo.writeObject(output, object.skill());
    }

    @Override
    public RangeAI read(Kryo kryo, Input input, Class<RangeAI> type) {
        float attackRange = input.readFloat();
        float distance = input.readFloat();
        Skill skill = kryo.readObject(input, Skill.class);
        return new RangeAI(attackRange, distance, skill);
    }
}
