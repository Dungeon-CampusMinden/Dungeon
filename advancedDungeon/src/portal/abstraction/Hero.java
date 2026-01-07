package portal.abstraction;

import com.badlogic.gdx.Input;
import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.entities.HeroController;
import contrib.hud.DialogUtils;
import contrib.hud.UIUtils;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Objects;
import starter.PortalStarter;

/**
 * Die Klasse {@code Hero} kapselt eine Spielfigur (Entity) und stellt Methoden zur Steuerung und
 * Interaktion bereit.
 *
 * <p>Diese Klasse bietet eine Abstraktion für Spielfiguren, die vom Spieler gesteuert werden
 * können. Sie unterstützt insbesondere Bewegung, das Öffnen von Inventaren, Interaktionen mit
 * Objekten sowie das Ausführen einer Feuerball-Fähigkeit mit Abklingzeit.
 *
 * @param hero Die Entity, die diesen Helden repräsentiert.
 */
public record Hero(Entity hero) {

  /** Erzeugt eine Pause zwischen Interaktionen. */
  private static final IAction INTERACTION_COOLDOWN = () -> {};

  private static EventScheduler.ScheduledAction cooldownEvent;

  /**
   * Konstruktor.
   *
   * @param hero Die Entity, die diesen Helden darstellt. Muss ein {@link PlayerComponent}
   *     enthalten.
   */
  public Hero(Entity hero) {
    this.hero = hero;
    if (!PortalStarter.DEBUG_MODE) {
      // Entfernt alle bisherigen Tastenzuweisungen (außer der zum Schließen der UI)
      hero.fetch(InputComponent.class)
          .ifPresent(
              inputComponent ->
                  inputComponent.removeCallbacksIf(
                      (entry -> !Objects.equals(entry.getKey(), KeyboardConfig.CLOSE_UI.value()))));
    }
  }

  /**
   * Setzt den Controller für die Spielfigur.
   *
   * <p>Registriert Tasgittendrücke und übergibt sie an den übergebenen {@link PlayerController},
   * der die Verarbeitung übernimmt.
   *
   * @param controller Ein {@link PlayerController}-Objekt, das die Eingaben verarbeitet. Wenn
   *     {@code null}, wird keine Aktion durchgeführt.
   */
  public void setController(PlayerController controller) {
    if (controller == null) return;
    String[] mousebuttons = {"LMB", "RMB", "MMB"};
    hero.fetch(InputComponent.class)
        .ifPresent(
            ic -> {
              for (int key = 0; key <= Input.Keys.MAX_KEYCODE; key++) {
                int finalKey = key;
                ic.registerCallback(
                    key,
                    entity -> {
                      try {
                        if (finalKey <= 2) {
                          controller.processKey(mousebuttons[finalKey]);
                        } else controller.processKey(Input.Keys.toString(finalKey).toUpperCase());
                      } catch (Exception e) {
                        DialogUtils.showTextPopup(
                            "Ups, da ist ein Fehler im Code: " + e.getMessage(), "Error");
                      }
                    });
              }
            });
  }

  /**
   * Setzt die Bewegungsgeschwindigkeit des Helden.
   *
   * @param speed Geschwindigkeit in x und y Richtung.
   */
  public void setSpeed(Vector2 speed) {
    hero.fetch(VelocityComponent.class).ifPresent(vc -> vc.applyForce("MOVEMENT", speed));
  }

  /**
   * Setzt die horizontale Bewegungsgeschwindigkeit des Helden.
   *
   * @param speed Geschwindigkeit in X-Richtung.
   */
  public void setXSpeed(float speed) {
    hero.fetch(VelocityComponent.class)
        .ifPresent(vc -> vc.applyForce("MOVEMENT_X", Vector2.of(speed, 0)));
  }

  /**
   * Setzt die vertikale Bewegungsgeschwindigkeit des Helden.
   *
   * @param speed Geschwindigkeit in Y-Richtung.
   */
  public void setYSpeed(float speed) {

    hero.fetch(VelocityComponent.class)
        .ifPresent(vc -> vc.applyForce("MOVEMENT_Y", Vector2.of(0, speed)));
  }

  /**
   * Gibt die aktuelle Mausposition im Spielfeld als {@link Point} zurück.
   *
   * @return Die aktuelle Mauszeigerposition.
   */
  public Point getMousePosition() {
    return SkillTools.cursorPositionAsPoint();
  }

  /** Feuert den Skill ab. */
  public void shootSkill() {
    hero.fetch(SkillComponent.class)
        .flatMap(SkillComponent::activeSkill)
        .ifPresent(fs -> fs.execute(hero));
  }

  /** Schalte auf den nächsten Skill. */
  public void nextSkill() {
    hero.fetch(SkillComponent.class).ifPresent(sc -> sc.nextSkill());
  }

  /**
   * Führt eine Interaktion mit dem Objekt am angegebene Punkt in der Nähe des Helden aus.
   *
   * <p>Falls eine UI geöffnet ist, wird sie geschlossen. Andernfalls wird mit dem nächsten
   * interagierbaren Objekt interagiert.
   *
   * @param point Position an der eine Interaktion ausgeführt werden soll (z.B Mausposition).
   */
  public void interact(Point point) {
    if (cooldownEvent != null && EventScheduler.isScheduled(cooldownEvent)) return;
    hero.fetch(UIComponent.class)
        .ifPresentOrElse(
            uiComponent -> {
              if (uiComponent.dialog() instanceof GUICombination
                  && !InventoryGUI.inPlayerInventory(hero)) {
                UIUtils.closeDialog(uiComponent);
              }
            },
            () -> {
              HeroController.interact(hero, point);
              cooldownEvent = EventScheduler.scheduleAction(INTERACTION_COOLDOWN, 250);
            });
  }

  /**
   * Öffnet oder schließt das Inventar des Helden.
   *
   * <p>Wenn bereits eine Inventar-UI geöffnet ist, wird sie geschlossen. Andernfalls wird sie
   * geöffnet.
   */
  public void openInventory() {
    HeroController.toggleInventory(hero);
  }

  /**
   * Zerstört (löscht) ein Item an der angegebenen Position, sofern vorhanden.
   *
   * @param point Die Zielposition.
   */
  public void destroyItemAt(Point point) {
    if (point == null) return;
    Tile tile = Game.tileAt(point).orElse(null);
    if (tile == null) return;
    Game.entityAtTile(tile)
        .filter(e -> e.isPresent(ItemComponent.class))
        .findFirst()
        .ifPresent(Game::remove);
  }

  /**
   * Bringt dem Helden einen neuen Skill bei.
   *
   * @param skill Der neue Skill.
   */
  public void addSkill(Skill skill) {
    hero.fetch(SkillComponent.class).ifPresent(sc -> sc.addSkill(skill));
  }
}
