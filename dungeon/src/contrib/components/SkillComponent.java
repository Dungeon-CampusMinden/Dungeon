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
  private int activeSkillOne = -1;

  private int activeSkillTwo = -1;

  /**
   * Creates a new {@code SkillComponent} with the given skills.
   *
   * <p>If at least one skill is provided, the first skill will be marked as active skill one.
   *
   * <p>If at least two skills are provided, the second skill will be marked as active skill two
   *
   * @param skills one or more skills to initialize the component with
   */
  public SkillComponent(Skill... skills) {
    this.skills = new ArrayList<>(Arrays.asList(skills));
    activeSkillOne = skills.length > 0 ? 0 : activeSkillOne;
    activeSkillTwo = skills.length > 1 ? 1 : activeSkillTwo;
  }

  /**
   * Adds a skill to this component.
   *
   * <p>If this is the first or second skill added, it will automatically become an active skill.
   *
   * @param skill the skill to add (ignored if {@code null})
   */
  public void addSkill(Skill skill) {
    if (skill == null) return;

    skills.add(skill);
    if (activeSkillOne == -1) activeSkillOne = 0;
    else if (activeSkillTwo == -1) activeSkillTwo = 0;
  }

  /**
   * Removes a given skill instance from this component. * *
   *
   * <p>If the active skill is removed, the active index will be adjusted. If no skills remain, the
   * active index is set to {@code -1}.
   *
   * @param skill the skill to remove
   */
  public void removeSkill(Skill skill) {
    if (!skills.remove(skill)) {
      return;
    }
    adjustActiveSkillIndices();
  }

  /** Remove all skills. */
  public void removeAll() {
    skills.clear();
    activeSkillOne = -1;
    activeSkillTwo = -1;
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
    if (!skills.removeIf(s -> skillClass.isAssignableFrom(s.getClass()))) {
      return;
    }

    adjustActiveSkillIndices();
  }

  /**
   * Finds the first skill that matches the given class (or subclass).
   *
   * @param skillClass the class of the skill to find
   * @return an {@link Optional} containing the matching skill, or {@link Optional#empty()} if none
   *     exists
   */
  public Optional<Skill> getSkill(Class<? extends Skill> skillClass) {
    return skills.stream().filter(s -> skillClass.isAssignableFrom(s.getClass())).findFirst();
  }

  /**
   * Returns the currently active skill on slot one.
   *
   * @return an {@link Optional} containing the active skill, or {@link Optional#empty()} if no
   *     skill is active
   */
  public Optional<Skill> activeSkillOne() {
    if (activeSkillOne == -1 || activeSkillOne >= skills.size()) {
      return Optional.empty();
    }
    return Optional.of(skills.get(activeSkillOne));
  }

  /**
   * Returns the currently active skill on slot two.
   *
   * @return an {@link Optional} containing the active skill, or {@link Optional#empty()} if no
   *     skill is active
   */
  public Optional<Skill> activeSkillTwo() {
    if (activeSkillTwo == -1 || activeSkillTwo >= skills.size()) {
      return Optional.empty();
    }
    return Optional.of(skills.get(activeSkillTwo));
  }

  /**
   * Moves the first active skill to the next skill in the list.
   *
   * <p>If the end of the list is reached, it wraps around to the first skill. If no skills are
   * present, nothing happens.
   *
   * <p>If the next skill is currently the second active skill, the skill after that will be
   * selected.
   */
  public void nextFirstSkill() {
    if (!skills.isEmpty()) {
      activeSkillOne = (activeSkillOne + 1) % skills.size();
      if (activeSkillOne == activeSkillTwo) {
        if (skills.size() > 1) nextFirstSkill();
        else activeSkillTwo = -1;
      }
    }
  }

  /**
   * Moves the second active skill to the next skill in the list.
   *
   * <p>If the end of the list is reached, it wraps around to the first skill. If no skills are
   * present, nothing happens.
   *
   * <p>If the next skill is currently the first active skill, the skill after that will be
   * selected.
   */
  public void nextSecondSkill() {
    if (!skills.isEmpty()) {
      activeSkillTwo = (activeSkillTwo + 1) % skills.size();

      if (activeSkillOne == activeSkillTwo) {
        if (skills.size() > 1) nextSecondSkill();
        else activeSkillOne = -1;
      }
    }
  }

  /**
   * Moves the first active skill to the previous skill in the list.
   *
   * <p>If the beginning of the list is reached, it wraps around to the last skill. If no skills are
   * present, nothing happens.
   *
   * <p>If the previous skill is currently the second active skill, the skill before that will be
   * selected.
   */
  public void prevFirstSkill() {
    if (!skills.isEmpty()) {
      activeSkillOne = (activeSkillOne - 1 + skills.size()) % skills.size();

      if (activeSkillOne == activeSkillTwo) {
        if (skills.size() > 1) prevFirstSkill();
        else activeSkillTwo = -1;
      }
    }
  }

  /**
   * Moves the second active skill to the previous skill in the list.
   *
   * <p>If the beginning of the list is reached, it wraps around to the last skill. If no skills are
   * present, nothing happens.
   *
   * <p>If the previous skill is currently the first active skill, the skill before that will be
   * selected.
   */
  public void prevSecondSkill() {
    System.out.println("Prev second skill");
    if (!skills.isEmpty()) {
      activeSkillTwo = (activeSkillTwo - 1 + skills.size()) % skills.size();

      if (activeSkillOne == activeSkillTwo) {
        if (skills.size() > 1) prevSecondSkill();
        else activeSkillOne = -1;
      }
    }
  }

  /**
   * Returns an unmodifiable list of all skills currently assigned to this component.
   *
   * @return a copy of the skill list
   */
  public List<Skill> getSkills() {
    return List.copyOf(skills);
  }

  private void adjustActiveSkillIndices() {
    int size = skills.size();

    if (size == 0) {
      activeSkillOne = -1;
      activeSkillTwo = -1;
      return;
    }

    activeSkillOne = clampIndex(activeSkillOne, size);
    activeSkillTwo = clampIndex(activeSkillTwo, size);

    if (activeSkillOne == activeSkillTwo) {
      if (size > 1) nextSecondSkill();
      else activeSkillTwo = -1;
    }
  }

  private int clampIndex(int index, int size) {
    return (index >= size) ? size - 1 : index;
  }
}
