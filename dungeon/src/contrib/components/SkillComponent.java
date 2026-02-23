package contrib.components;

import contrib.utils.components.skill.Skill;
import core.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A component that manages a list of {@link Skill} objects for an entity.
 *
 * <p>It supports adding, removing, and retrieving skills, as well as managing which skill is
 * currently active. The active skill can be cycled through the list or set to the last one.
 */
public class SkillComponent implements Component {

  /** All skills currently assigned to this component. */
  private final List<Skill> skills;

  /**
   * The index of the currently active skill in {@link #skills}. If no skill is active, the value is
   * {@code -1}.
   */
  private int activeMainSkill = -1;

  private int activeSecondSkill = -1;

  /**
   * Creates a new {@code SkillComponent} with the given skills.
   *
   * <p>If at least one skill is provided, the first skill will be marked as active.
   *
   * @param skills one or more skills to initialize the component with
   */
  public SkillComponent(Skill... skills) {
    this.skills = new ArrayList<>(Arrays.asList(skills));

    if (!this.skills.isEmpty()) {
      activeMainSkill = 0;
    }

    if (this.skills.size() > 1) {
      activeSecondSkill = 1;
    }
  }

  /**
   * Adds a skill to this component.
   *
   * <p>If this is the first skill added, it will automatically become the active skill.
   *
   * @param skill the skill to add (ignored if {@code null})
   */
  public void addSkill(Skill skill) {
    if (skill != null) {
      this.skills.add(skill);

      if (activeMainSkill == -1) {
        activeMainSkill = 0;
      } else if (activeSecondSkill == -1 && this.skills.size() > 1) {
        activeSecondSkill = this.skills.size() - 1;
      }
    }
  }

  /**
   * Removes a given skill instance from this component.
   *
   * <p>If the active skill is removed, the active index will be adjusted. If no skills remain, the
   * active index is set to {@code -1}.
   *
   * @param skill the skill to remove
   */
  public void removeSkill(Skill skill) {
    if (this.skills.remove(skill)) {
      if (this.skills.size() > 1) {
        activeMainSkill = this.skills.size() - 2;
        activeSecondSkill = this.skills.size() - 1;
      } else if (this.skills.size() == 1) {
        activeMainSkill = 0;
        activeSecondSkill = -1;
      } else {
        activeMainSkill = -1;
        activeSecondSkill = -1;
      }
    }
  }

  /** Remove all skills. */
  public void removeAll() {
    this.skills.clear();
    activeMainSkill = -1;
    activeSecondSkill = -1;
  }

  /**
   * Removes all skills of the given class (or subclasses).
   *
   * <p>If the active skill is removed, the active index will be adjusted. If no skills remain, the
   * active index is set to {@code -1}.
   *
   * @param skillClass the class of the skills to remove
   */
  public void removeSkill(Class<? extends Skill> skillClass) {
    this.skills.removeIf(s -> skillClass.isAssignableFrom(s.getClass()));
    if (this.skills.size() > 1) {
      activeMainSkill = this.skills.size() - 2;
      activeSecondSkill = this.skills.size() - 1;
    } else if (this.skills.size() == 1) {
      activeMainSkill = 0;
      activeSecondSkill = -1;
    } else {
      activeMainSkill = -1;
      activeSecondSkill = -1;
    }
  }

  /**
   * Finds the first skill that matches the given class (or subclass).
   *
   * @param skillClass the class of the skill to find
   * @return an {@link Optional} containing the matching skill, or {@link Optional#empty()} if none
   *     exists
   */
  public Optional<Skill> getSkill(Class<? extends Skill> skillClass) {
    return this.skills.stream().filter(s -> skillClass.isAssignableFrom(s.getClass())).findFirst();
  }

  /**
   * Returns the currently active main skill.
   *
   * @return an {@link Optional} containing the active main skill, or {@link Optional#empty()} if no
   *     main skill is active
   */
  public Optional<Skill> activeMainSkill() {
    if (activeMainSkill == -1 || activeMainSkill >= this.skills.size()) {
      return Optional.empty();
    }
    return Optional.of(this.skills.get(activeMainSkill));
  }

  /**
   * Returns the currently active second skill.
   *
   * @return an {@link Optional} containing the active second skill, or {@link Optional#empty()} if
   *     no second skill is active
   */
  public Optional<Skill> activeSecondSkill() {
    if (activeSecondSkill == -1 || activeSecondSkill >= this.skills.size()) {
      return Optional.empty();
    }
    return Optional.of(this.skills.get(activeSecondSkill));
  }

  /**
   * Moves the active main skill to the next one in the list.
   *
   * <p>If the end of the list is reached, it wraps around to the first skill. If no skills are
   * present, nothing happens.
   */
  public void nextMainSkill() {
    if (this.skills.size() >= 2) {
      int startIndex = activeMainSkill;

      do {
        activeMainSkill = (activeMainSkill + 1) % skills.size();
      } while (activeMainSkill == activeSecondSkill && activeMainSkill != startIndex);
    }
  }

  /**
   * Moves the active main skill to the previous one in the list.
   *
   * <p>If the beginning of the list is reached, it wraps around to the last skill. If no skills are
   * present, nothing happens.
   */
  public void prevMainSkill() {
    if (this.skills.size() >= 2) {
      int startIndex = activeMainSkill;

      do {
        activeMainSkill = (activeMainSkill - 1 + skills.size()) % skills.size();
      } while (activeMainSkill == activeSecondSkill && activeMainSkill != startIndex);
    }
  }

  /**
   * Moves the active second skill to the next one in the list.
   *
   * <p>If the end of the list is reached, it wraps around to the first skill. If no skills are
   * present, nothing happens.
   */
  public void nextSecondSkill() {
    if (this.skills.size() >= 2) {
      int startIndex = activeSecondSkill;

      do {
        activeSecondSkill = (activeSecondSkill + 1) % skills.size();
      } while (activeSecondSkill == activeMainSkill && activeSecondSkill != startIndex);
    }
  }

  /**
   * Moves the active second skill to the previous one in the list.
   *
   * <p>If the beginning of the list is reached, it wraps around to the last skill. If no skills are
   * present, nothing happens.
   */
  public void prevSecondSkill() {
    if (this.skills.size() >= 2) {
      int startIndex = activeSecondSkill;

      do {
        activeSecondSkill = (activeSecondSkill - 1 + skills.size()) % skills.size();
      } while (activeSecondSkill == activeMainSkill && activeSecondSkill != startIndex);
    }
  }

  /**
   * Returns an unmodifiable list of all skills currently assigned to this component.
   *
   * @return a copy of the skill list
   */
  public List<Skill> getSkills() {
    return List.copyOf(this.skills);
  }
}
