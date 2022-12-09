package character.skills;

import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import graphic.Animation;
import java.util.List;
import tools.Point;

public class BaseMeleeEffect extends BaseSkillEffect {
    Point offset;
    Animation animation;

    Hitbox[] hitboxes;
    int curhitbox = 0;
    int frame = 0;
    int frames;

    public BaseMeleeEffect(
            int alive,
            Collidable caster,
            Point offset,
            List<String> animationTextures,
            Hitbox[] hitboxes,
            int frames) {
        super(alive, caster);
        this.offset = offset;
        this.animation = new Animation(animationTextures, frames);
        this.hitboxes = hitboxes;
        this.frames = frames;
        for (Hitbox hitbox : hitboxes) {
            hitbox.setCollidable(this);
        }
    }

    public BaseMeleeEffect(
            Collidable caster,
            Point offset,
            List<String> animationTextures,
            Hitbox[] hitboxes,
            int frames) {
        this(hitboxes.length * frames, caster, offset, animationTextures, hitboxes, frames);
    }

    @Override
    public void update() {
        super.update();
        frame++;
        if (frame >= frames) {
            curhitbox = (curhitbox + 1) % hitboxes.length;
        }
    }

    @Override
    public Hitbox getHitbox() {
        return hitboxes[curhitbox];
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {
        alive = 0;
    }

    @Override
    public Animation getActiveAnimation() {
        return animation;
    }

    @Override
    public Point getPosition() {
        return new Point(caster.getPosition().x + offset.x, caster.getPosition().y + offset.y);
    }
}
