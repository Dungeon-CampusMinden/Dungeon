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

public class AbilityBar extends Table implements HUDElement {
  private final List<AbilitySlot> slots = new ArrayList<>();
  private int activeAbilityCount = 0;

  public AbilityBar(Skin skin) {
    super(skin);

    setBackground(skin.getDrawable("dark-gray"));
    pad(1);

    // Abstand zwischen Slots
    defaults().pad(5);

    // 5 Slots erzeugen
    for (int i = 0; i < 5; i++) {
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

  public void setAbilityIcon(int index, Texture texture) {
    if (index >= 0 && index < slots.size()) {
      slots.get(index).setTexture(texture);
    }
  }

  public void setDynamicIcons() {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(
            sc -> {
              List<Skill> skills = sc.getSkills();
              activeAbilityCount = skills.size();
              String png;
              String tooltip;
              for (int i = 0; i < 5; i++) {
                if (i >= skills.size()) break;
                Skill skill = skills.get(i);
                switch (skill) {
                  case FireballSkill ignored:
                    png = "dungeon/assets/items/book/red_book.png";
                    tooltip = "Shoot a Fireball to fight enemies.";
                    break;
                  case SelfHealSkill ignored:
                    png = "dungeon/assets/items/book/magic_scroll.png";
                    tooltip = "Cast a healing spell to regenerate hp.";
                    break;
                  case BowSkill ignored:
                    png = "dungeon/assets/items/weapon/wooden_bow.png";
                    tooltip = "Use the Bow to shoot Arrows.";
                    break;
                  default:
                    png = null;
                    tooltip = "";
                    break;
                }
                tooltip =
                    tooltip
                        + "\n use ability: Left-click \n prev ability: (,) \n next ability: (.)";
                if (png != null) {
                  setAbilityIcon(i, new Texture(png));
                  slots.get(i).addTooltip(tooltip, getSkin());
                }
              }
            });
  }

  public void markEquippedAbility() {
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

  public void setAbilityCost() {
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

  public void updateAbilitys() {
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

  public void clearAbilitySlots() {
    for (int i = 0; i < 5; i++) {
      slots.get(i).removeTexture();
      slots.get(i).removeCost();
      slots.get(i).removeTooltip();
    }
  }
}
