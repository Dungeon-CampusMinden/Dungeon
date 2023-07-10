package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.skill.Skill;

/** Custom serializer to send and retrieve objects of {@link SkillSerializer}. */
public class SkillSerializer extends Serializer<Skill> {
    @Override
    public void write(Kryo kryo, Output output, Skill object) {}

    @Override
    public Skill read(Kryo kryo, Input input, Class<Skill> type) {
        return null;
    }
}
