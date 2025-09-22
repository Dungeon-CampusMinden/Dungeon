package produsAdvanced.riddles;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.AdvancedDungeon;
import produsAdvanced.abstraction.Berry;

/**
 * Eine Feuerball-Fähigkeit, die beim Aufprall Feuerschaden verursacht.
 *
 * <p>Diese Klasse baut auf {@link DamageProjectileSkill} auf und erstellt einen speziellen
 * Feuerball. Der Feuerball fliegt auf ein Ziel zu, macht bei einer Kollision Schaden und
 * verschwindet, wenn er eine Wand trifft oder zu weit geflogen ist.
 */
public class MyFireballSkill extends DamageProjectileSkill {

  private static final String SKILL_NAME = "FIREBALL";
  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final long COOLDOWN = 500;
  private static final boolean IS_PIERCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;

  /**
   * Erstellt einen Feuerball mit Standardwerten.
   *
   * <p>Der Feuerball fliegt schnell, macht 2 Schaden und hat eine kurze Abklingzeit.
   */
  public MyFireballSkill() {
    this(COOLDOWN, SPEED, RANGE, DAMAGE);
  }

  /**
   * Erstellt einen Feuerball, bei dem du selbst Werte festlegen kannst.
   *
   * @param cooldown Abklingzeit in Millisekunden (wie lange man warten muss, bis man den Feuerball
   *     erneut benutzen darf).
   * @param speed Geschwindigkeit, mit der der Feuerball fliegt.
   * @param range Reichweite – wie weit der Feuerball maximal fliegen kann.
   * @param damageAmount Schaden, den der Feuerball beim Treffen macht.
   * @param resourceCost Kosten, falls die Fähigkeit bestimmte Ressourcen verbraucht (z. B. Mana).
   */
  @SafeVarargs
  public MyFireballSkill(
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldown,
        TEXTURE,
        null,
        speed,
        range,
        IS_PIERCING,
        damageAmount,
        DAMAGE_TYPE,
        IGNORE_FIRST_WALL,
        resourceCost);
  }

  /**
   * Berechnet das Ziel, wohin der Feuerball fliegen soll.
   *
   * @param caster Die Figur (Entity), die den Feuerball schießt.
   * @return Das Ziel, wo der Feuerball endet.
   */
  @Override
  protected Point end(Entity caster) {
    return AdvancedDungeon.hero.getMousePosition();
  }

  /**
   * Was passiert, wenn der Feuerball eine Beere trifft.
   *
   * @param berry Die Beere, die getroffen wurde.
   */
  private void onBerryHit(Entity berry) {
    System.out.println("Beere getroffen");
  }

  @Override
  protected void additionalEffectAfterDamage(
      Entity caster, Entity projectile, Entity target, Direction direction) {
    if (target.name().equals(Berry.NAME)) {
      onBerryHit(target);
    }
  }
}
