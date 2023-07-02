package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.Skill;

public class SkillSerializer extends Serializer<Skill> {
    @Override
    public void write(Kryo kryo, Output output, Skill object) {
//        output.writeFloat(object.getCoolDownInSeconds());
//        kryo.writeObject(output, object.getSkillFunction());
    }

    @Override
    public Skill read(Kryo kryo, Input input, Class<Skill> type) {
//        float coolDownInSeconds = input.readFloat();
//        ISkillFunction skillFunction = kryo.readObject(input, ISkillFunction.class);
//        return new Skill(skillFunction, coolDownInSeconds);
        return null;
    }
}
