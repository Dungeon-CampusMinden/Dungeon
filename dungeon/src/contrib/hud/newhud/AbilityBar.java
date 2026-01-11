package contrib.hud.newhud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import contrib.utils.components.skill.selfSkill.SelfHealSkill;
import core.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The AbilityBar is a HUD element that displays the hero's abilities (skills).
 *
 * <p>This class uses multiple {@link AbilitySlot} to build a dynamic ability bar. Currently, every
 * skill requires a dedicated icon and tooltip.
 */
public class AbilityBar extends Table implements HUDElement {
  private final List<AbilitySlot> slots = new ArrayList<>();
  private int activeAbilityCount = 0;
  private final int barSize = 5;

  /**
   * Creates a new AbilityBar.
   *
   * @param skin The skin that defines the appearance of UI elements.
   */
  public AbilityBar(Skin skin) {
    super(skin);

    setBackground(skin.getDrawable("dark-gray"));

    defaults().pad(5);

    for (int i = 0; i < barSize; i++) {
      AbilitySlot slot = new AbilitySlot(skin);
      slots.add(slot);
      add(slot).size(64, 64);
    }
  }

  @Override
  public void init() {
    layoutElement();
    setDynamicIcons();
    setAbilityCost();
  }

  @Override
  public void layoutElement() {
    pack();
    setPosition((Gdx.graphics.getWidth() / 2f) - getWidth() / 2f, 10);
  }

  @Override
  public void update() {
    markEquippedAbility();
    updateAbilitys();
  }

  private void setAbilityIcon(int index, Texture texture) {
    if (index >= 0 && index < slots.size()) {
      slots.get(index).setTexture(texture);
    }
  }

  private void setDynamicIcons() {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(
            sc -> {
              List<Skill> skills = sc.getSkills();
              activeAbilityCount = skills.size();
              String png;
              String tooltip;
              for (int i = 0; i < barSize; i++) {
                if (i >= skills.size()) break;
                Skill skill = skills.get(i);
                switch (skill) {
                  case FireballSkill ignored:
                    png = "dungeon/assets/hud/fireball_skill_icon.png";
                    tooltip = "Shoot a Fireball to fight enemies.";
                    break;
                  case SelfHealSkill ignored:
                    png = "dungeon/assets/hud/healing_skill_icon.png";
                    tooltip = "Cast a healing spell to regenerate hp.";
                    break;
                  case BowSkill ignored:
                    png = "dungeon/assets/hud/bow_and_arrow_icon.png";
                    tooltip = "Use the Bow to shoot Arrows.";
                    break;
                  default:
                    png = "dungeon/assets/hud/empty_ability_icon.png";
                    tooltip = "The ability is missing an icon and a tooltip.";
                    break;
                }
                tooltip =
                    tooltip
                        + "\n use ability: Left-click \n prev ability: (,) \n next ability: (.)";

                setAbilityIcon(i, new Texture(png));
                slots.get(i).addTooltip(tooltip, getSkin());
              }
            });
  }

  private void markEquippedAbility() {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(
            sc -> {
              Skill equippedSkill = sc.activeSkill().orElse(null);

              for (int i = 0; i < slots.size(); i++) {
                AbilitySlot slot = slots.get(i);
                List<Skill> skills = sc.getSkills();
                if (i < skills.size() && skills.get(i) == equippedSkill) {
                  slot.setActive(true);
                } else {
                  slot.setActive(false);
                }
              }
            });
  }

  private void setAbilityCost() {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(
            sc -> {
              List<Skill> skills = sc.getSkills();
              for (int i = 0; i < slots.size(); i++) {
                if (i >= skills.size()) break;
                Map<Resource, Integer> skillResources = skills.get(i).resourceCost();
                if (!skillResources.isEmpty()) {
                  Resource res = skillResources.keySet().iterator().next();
                  int cost = skillResources.get(res);
                  slots.get(i).setCost(res, cost);
                }
              }
            });
  }

  private void updateAbilitys() {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(
            sc -> {
              List<Skill> skills = sc.getSkills();

              if (activeAbilityCount != skills.size()) {
                clearAbilitySlots();
                setDynamicIcons();
                setAbilityCost();
                activeAbilityCount = skills.size();
              }
            });
  }

  private void clearAbilitySlots() {
    for (int i = 0; i < barSize; i++) {
      slots.get(i).removeTexture();
      slots.get(i).removeCost();
      slots.get(i).removeTooltip();
    }
  }
}
