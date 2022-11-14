package character.skills;

import controller.AbstractController;

public class EffectController extends AbstractController<BaseSkillEffect> {
    @Override
    public void process(BaseSkillEffect baseSkillEffect) {
        baseSkillEffect.update();
    }
}
