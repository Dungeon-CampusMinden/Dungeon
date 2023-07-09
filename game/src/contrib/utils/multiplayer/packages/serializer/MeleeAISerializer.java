package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.fight.MeleeAI;
import contrib.utils.components.skill.Skill;
import core.utils.components.draw.Animation;

/**
 * Custom serializer to send and retrieve objects of {@link MeleeAI}.
 */
public class MeleeAISerializer extends Serializer<MeleeAI> {
    @Override
    public void write(Kryo kryo, Output output, MeleeAI object) {
        kryo.writeClass(output, object.getClass());
        output.writeFloat(object.getAttackRange());
        kryo.writeObject(output, object.getFightSkill());
    }

    @Override
    public MeleeAI read(Kryo kryo, Input input, Class<MeleeAI> type) {
        float attackRange = input.readFloat();
        Skill fightSkill = kryo.readObject(input, Skill.class);
        return new MeleeAI(attackRange, fightSkill);
    }
}
