package character.skills;

import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import java.util.List;
import java.util.Map;
import tools.Point;

public class BaseMeleeSkill extends BaseSkill {

    Collidable caster;
    Map<CharacterDirection, Point> offsets;
    Map<CharacterDirection, List<String>> textures;
    Map<CharacterDirection, Hitbox[]> hitboxes;

    public BaseMeleeSkill(
            Collidable caster,
            Map<CharacterDirection, Point> offsets,
            Map<CharacterDirection, List<String>> textures,
            Map<CharacterDirection, Hitbox[]> hitboxes) {
        super(100);
        this.caster = caster;
        this.offsets = offsets;
        this.textures = textures;
        this.hitboxes = hitboxes;
    }

    @Override
    protected BaseSkillEffect spawn(CharacterDirection direction) {
        return new BaseMeleeEffect(
                caster,
                offsets.get(direction),
                textures.get(direction),
                hitboxes.get(direction),
                5);
    }
}
