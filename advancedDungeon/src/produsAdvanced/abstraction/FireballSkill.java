package produsAdvanced.abstraction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.components.ItemComponent;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.damageSkill.projectile.DamageProjectileSkill;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import produsAdvanced.AdvancedDungeon;

/**
 * Diese Klasse stellt eine Feuerzauber-Fähigkeit im Spiel dar.
 *
 * <p>Mit FireballSkill kannst du einen Feuerball im Spiel erzeugen und dessen Eigenschaften wie
 * Schaden, Geschwindigkeit oder Aussehen anpassen. Der Feuerball kann auf Ziele geschossen werden
 * und verschiedene Effekte auslösen.
 *
 * <p>Beispiel zur Verwendung:
 *
 * <pre>
 * fireballSkill.setDamage(10);
 * fireballSkill.setSpeed(7.5f);
 * </pre>
 *
 * @see Hero#shootFireball(Point)
 */
public class FireballSkill extends DamageProjectileSkill {
  private static final Logger LOGGER = Logger.getLogger(FireballSkill.class.getName());

  /** Debug-Textur für den Feuerball. */
  public static final String DEBUG_TEXTURE = "character/monster/pumpkin_dude";

  /** Standard-Textur für den Feuerball. */
  public static final String FIRE_TEXTURE = "skills/fireball";

  private static final String SKILL_NAME = "fireball";
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final int DEFAULT_DAMAGE = 5;
  private static final int DEFAULT_RANGE = 5;
  private static final float DEFAULT_SPEED = 5f;
  private static final int DEFAULT_COOL_DOWN = 500; // ms
  private static final Consumer<Entity> ON_SPAWN_PLAY_SOUND = entity -> playSound();
  private Point target = new Point(0, 0);
  private IPath texture = new SimpleIPath("skills/fireball");
  private int range;
  private float speed;
  private int damage;
  private int coolDown; // ms
  private Consumer<Berry> onBerryHit = (berry) -> {};
  private Skill skill = AdvancedDungeon.fireballSkill;

  /**
   * Erstellt einen neuen Feuerball mit angepassten Eigenschaften.
   *
   * <p>Diese Konstruktor-Methode ist für fortgeschrittene Anwendungen gedacht.
   *
   * @param targetSelection Funktion zur Auswahl des Zielpunkts
   * @param texture Pfad zur Textur des Feuerballs
   * @param range Reichweite des Feuerballs in Spielfeldern
   * @param speed Geschwindigkeit des Feuerballs
   * @param damageAmount Schadenswert des Feuerballs
   * @param coolDown Abklingzeit des Feuerballs in Millisekunden
   * @param onHit Aktion, die beim Treffen eines Ziels ausgeführt wird
   */
  public FireballSkill(
      Supplier<Point> targetSelection,
      IPath texture,
      int range,
      float speed,
      int damageAmount,
      int coolDown,
      Consumer<Entity> onHit) {
    super(
        SKILL_NAME,
        coolDown,
        targetSelection,
        damageAmount,
        DAMAGE_TYPE,
        texture,
        speed,
        range,
        HIT_BOX_SIZE,
        DamageProjectileSkill.DEFAULT_ON_WALL_HIT,
        ON_SPAWN_PLAY_SOUND,
        (projectile, target) -> onHit.accept(target));
    this.texture = texture;
    this.range = range;
    this.speed = speed;
    this.damage = damageAmount;
    this.coolDown = Math.max(0, coolDown);
  }

  /**
   * Erstellt einen neuen Feuerball mit Standardwerten.
   *
   * <p>Der Standardfeuerball hat:
   *
   * <ul>
   *   <li>Schaden: 5
   *   <li>Reichweite: 5 Felder
   *   <li>Geschwindigkeit: 5
   *   <li>Abklingzeit: 500 Millisekunden
   * </ul>
   */
  public FireballSkill() {
    this(
        () -> new Point(0, 0),
        new SimpleIPath(FIRE_TEXTURE),
        DEFAULT_RANGE,
        DEFAULT_SPEED,
        DEFAULT_DAMAGE,
        DEFAULT_COOL_DOWN,
        (target) -> {});
  }

  /** Spielt den Soundeffekt für den Feuerball ab. */
  private static final void playSound() {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(PROJECTILE_SOUND.pathString()));

    // Generate a random pitch between 1.5f and 2.0f
    float minPitch = 2f;
    float maxPitch = 3f;
    float randomPitch = MathUtils.random(minPitch, maxPitch);

    // Play the sound with the adjusted pitch
    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, randomPitch);

    // Set the volume
    soundEffect.setVolume(soundId, 0.05f);
  }

  /**
   * Setzt die Reichweite des Feuerballs.
   *
   * <p>Die Reichweite bestimmt, wie weit der Feuerball fliegen kann, bevor er auf ein Ziel trifft
   * oder die maximale Reichweite erreicht.
   *
   * @param range Die Reichweite des Feuerballs in Spielfeldern.
   */
  public final void setRange(int range) {
    this.range = range;
  }

  /**
   * Setzt die Geschwindigkeit des Feuerballs.
   *
   * <p>Die Geschwindigkeit bestimmt, wie schnell der Feuerball fliegt.
   *
   * @param speed Die Geschwindigkeit des Feuerballs.
   */
  public final void setSpeed(float speed) {
    this.speed = speed;
  }

  /**
   * Setzt den Schaden des Feuerballs.
   *
   * <p>Der Schaden bestimmt, wie viel Schaden der Feuerball an einem Ziel verursacht, wenn er
   * trifft.
   *
   * @param damage Der Schaden des Feuerballs.
   */
  public final void setDamage(int damage) {
    this.damage = damage;
  }

  /**
   * Setzt die Textur des Feuerballs.
   *
   * <p>Die Textur bestimmt, wie der Feuerball im Spiel aussieht.
   *
   * @param texture Der Pfad zur Textur des Feuerballs.
   */
  public final void setTexture(String texture) {
    this.texture = new SimpleIPath(texture);
  }

  /**
   * Setzt die Abklingzeit des Feuerballs.
   *
   * <p>Die Abklingzeit bestimmt, wie lange der Spieler warten muss, bevor er den Feuerball erneut
   * verwenden kann.
   *
   * <p>Die Abklingzeit darf nicht negativ sein.
   *
   * @param coolDown Die Abklingzeit des Feuerballs in Millisekunden.
   */
  public final void setCoolDown(int coolDown) {
    this.coolDown = Math.max(0, coolDown);
  }

  /**
   * Gibt die Reichweite des Feuerballs zurück.
   *
   * @return Die Reichweite des Feuerballs in Spielfeldern.
   */
  public final int getRange() {
    return range;
  }

  /**
   * Gibt die Geschwindigkeit des Feuerballs zurück.
   *
   * @return Die Geschwindigkeit des Feuerballs.
   */
  public final float getSpeed() {
    return speed;
  }

  /**
   * Gibt den Schaden des Feuerballs zurück.
   *
   * @return Der Schaden des Feuerballs.
   */
  public final int getDamage() {
    return damage;
  }

  /**
   * Gibt den Pfad zur Textur des Feuerballs zurück.
   *
   * @return Der Pfad zur Textur des Feuerballs.
   */
  public final IPath getTexture() {
    return texture;
  }

  /**
   * Gibt die Abklingzeit des Feuerballs zurück.
   *
   * @return Die Abklingzeit des Feuerballs in Millisekunden.
   */
  public final int getCoolDown() {
    return coolDown;
  }

  /**
   * Diese Methode wird aufgerufen, wenn der Feuerball ein Ziel trifft.
   *
   * <p>Falls eine {@link Berry} getroffen wird, wird die Methode {@link #onBerryHit} aufgerufen.
   *
   * @param target Das Ziel, das getroffen wurde.
   */
  private void onHit(Entity target) {
    // remove id
    String name = target.name();
    int lastUnderscore = name.lastIndexOf("_");
    if (lastUnderscore != -1) {
      name = name.substring(0, lastUnderscore);
    }
    if (name.equals("Berry")) {
      target
          .fetch(ItemComponent.class)
          .ifPresent(
              itemComp -> {
                try {
                  onBerryHit.accept((Berry) itemComp.item());
                } catch (UnsupportedOperationException e) {
                  LOGGER.info(e.getMessage());
                }
              });
    }
  }

  /**
   * Führt den Feuerball aus und schießt ihn in die angegebene Richtung.
   *
   * @param user Der Benutzer, der den Feuerball abfeuert.
   * @param target Die Zielposition, auf die der Feuerball geschossen wird.
   */
  final void shoot(Entity user, Point target) {
    if (!skill.canBeUsedAgain()) return;

    this.target = new Point(target);

    skill =
        new FireballSkill(() -> this.target, texture, range, speed, damage, coolDown, this::onHit);
    skill.execute(user);
  }

  /**
   * Setzt den Controller für die Feuerball-Fähigkeit.
   *
   * <p>Diese Methode wird verwendet, um den Controller zu setzen, der die Logik für den Feuerball
   * steuert.
   *
   * <p>Diese Methode sollte nur einmal pro Änderung aufgerufen werden.
   *
   * @param controller Der Controller, der die Logik für den Feuerball steuert.
   * @see AdvancedDungeon
   */
  public void setController(Fireball controller) {
    this.onBerryHit = controller::onBerryHit;
  }
}
