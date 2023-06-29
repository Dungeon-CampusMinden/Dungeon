package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.components.PlayerComponent;

public class PlayerComponentSerializer extends Serializer<PlayerComponent> {
    private Entity entity;

    public PlayerComponentSerializer(){
        super();
    }

    public PlayerComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, PlayerComponent object) {
        kryo.writeObjectOrNull(output, object.getSkillSlot1().orElse(null), Skill.class);
        kryo.writeObjectOrNull(output, object.getSkillSlot2().orElse(null), Skill.class);
    }

    @Override
    public PlayerComponent read(Kryo kryo, Input input, Class<PlayerComponent> type) {
        Skill skillSlot1 = kryo.readObjectOrNull(input, Skill.class);
        Skill skillSlot2 = kryo.readObjectOrNull(input, Skill.class);
        PlayerComponent playerComp;
        if(skillSlot1 == null  || skillSlot2 == null){
            playerComp = new PlayerComponent(entity);
            if (skillSlot1 != null){
                playerComp.setSkillSlot1(skillSlot1);
            }
            if (skillSlot2 != null){
                playerComp.setSkillSlot2(skillSlot2);
            }
        } else {
            playerComp = new PlayerComponent(entity, skillSlot1, skillSlot2);
        }
        assert(playerComp == null);
        return playerComp;
    }
}
