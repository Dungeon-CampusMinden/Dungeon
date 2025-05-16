package produsAdvanced.abstraction;

import com.badlogic.gdx.Input;
import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.hud.DialogUtils;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import java.util.Comparator;
import produsAdvanced.AdvancedDungeon;

/**
 * Die Klasse {@code Hero} kapselt eine Spielfigur (Entity) und stellt Methoden zur Steuerung und
 * Interaktion bereit.
 *
 * <p>Diese Klasse bietet eine Abstraktion für Spielfiguren, die vom Spieler gesteuert werden
 * können. Sie unterstützt insbesondere Bewegung, das Öffnen von Inventaren, Interaktionen mit
 * Objekten sowie das Ausführen einer Feuerball-Fähigkeit mit Abklingzeit.
 */
public class Hero {

  /** Erzeugt eine Pause zwischen Interaktionen. */
  private static final IAction INTERACTION_COOLDOWN = () -> {};

  private static EventScheduler.ScheduledAction cooldownEvent;

  /** Die Entity, die diesen Helden repräsentiert. */
  private Entity hero;

  /**
   * Die Feuerball-Fähigkeit, die für diesen Helden verwendet wird.
   *
   * @see Fireball
   */
  private final FireballSkill fireballSkill;

  /**
   * Konstruktor.
   *
   * @param heroInstance Die Entity, die diesen Helden darstellt. Muss ein {@link PlayerComponent}
   *     enthalten.
   * @param fireballSkill Die Feuerball-Fähigkeit, die für diesen Helden verwendet wird.
   */
  public Hero(Entity heroInstance, FireballSkill fireballSkill) {
    this.hero = heroInstance;

    if (!AdvancedDungeon.DEBUG_MODE) {
      PlayerComponent pc = heroInstance.fetch(PlayerComponent.class).get();
      pc.removeCallbacks(); // Entfernt alle bisherigen Tastenzuweisungen
    }
    this.fireballSkill = fireballSkill;
  }

  /**
   * Setzt den Controller für die Spielfigur.
   *
   * <p>Registriert Tastendrücke (von Taste 0 bis Z) und übergibt sie an den übergebenen {@link
   * PlayerController}, der die Verarbeitung übernimmt.
   *
   * @param controller Ein {@link PlayerController}-Objekt, das die Eingaben verarbeitet. Wenn
   *     {@code null}, wird keine Aktion durchgeführt.
   */
  public void setController(PlayerController controller) {
    if (controller == null) return;
    PlayerComponent pc = hero.fetch(PlayerComponent.class).get();
    String[] mousebuttons = {"LMB", "RMB", "MMB"};

    for (int key = 0; key <= Input.Keys.Z; key++) {
      int finalKey = key;
      pc.registerCallback(
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

    // Callback zum Schließen von UI-Dialogen
    pc.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (e) -> {
          var firstUI =
              Game.entityStream()
                  .filter(x -> x.isPresent(UIComponent.class))
                  .map(
                      x ->
                          new Tuple<>(
                              x,
                              x.fetch(UIComponent.class)
                                  .orElseThrow(
                                      () -> MissingComponentException.build(x, UIComponent.class))))
                  .filter(x -> x.b().closeOnUICloseKey())
                  .max(Comparator.comparingInt(x -> x.b().dialog().getZIndex()))
                  .orElse(null);
          if (firstUI != null) {
            InventoryGUI.inHeroInventory = false;
            firstUI.a().remove(UIComponent.class);
            if (firstUI.a().componentStream().findAny().isEmpty()) {
              Game.remove(firstUI.a());
            }
          }
        },
        false,
        true);
  }

  /**
   * Setzt die horizontale Bewegungsgeschwindigkeit des Helden.
   *
   * @param speed Geschwindigkeit in X-Richtung.
   */
  public void setXSpeed(float speed) {
    hero.fetch(VelocityComponent.class).get().currentXVelocity(speed);
  }

  /**
   * Setzt die vertikale Bewegungsgeschwindigkeit des Helden.
   *
   * @param speed Geschwindigkeit in Y-Richtung.
   */
  public void setYSpeed(float speed) {
    hero.fetch(VelocityComponent.class).get().currentYVelocity(speed);
  }

  /**
   * Gibt die aktuelle Mausposition im Spielfeld als {@link Point} zurück.
   *
   * @return Die aktuelle Mauszeigerposition.
   */
  public Point getMousePosition() {
    return SkillTools.cursorPositionAsPoint();
  }

  /**
   * Führt die Feuerball-Fähigkeit in die angegebene Richtung aus, sofern die Abklingzeit abgelaufen
   * ist.
   *
   * @param direction Zielposition des Feuerballs.
   */
  public void shootFireball(Point direction) {
    fireballSkill.shoot(hero, direction);
  }

  /**
   * Führt eine Interaktion mit einem Objekt in der Nähe des Helden aus.
   *
   * <p>Falls eine UI geöffnet ist, wird sie geschlossen. Andernfalls wird mit dem nächsten
   * interagierbaren Objekt interagiert.
   */
  public void interact() {
    if (cooldownEvent != null && EventScheduler.isScheduled(cooldownEvent)) return;
    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null
        && uiComponent.dialog() instanceof GUICombination
        && !InventoryGUI.inHeroInventory) {
      hero.remove(UIComponent.class);
    } else {
      InteractionTool.interactWithClosestInteractable(hero);
      cooldownEvent = EventScheduler.scheduleAction(INTERACTION_COOLDOWN, 250);
    }
  }

  /**
   * Gibt ein {@link Berry}-Item zurück, das sich an der angegebenen Position befindet.
   *
   * @param point Die Position auf der Karte.
   * @return Eine Instanz von {@link Berry}, falls vorhanden, sonst {@code null}.
   */
  public Berry getBerryAt(Point point) {
    if (point == null) return null;
    Tile t = Game.tileAT(point);
    if (t == null) return null;
    return Game.entityAtTile(t)
        .findFirst()
        .flatMap(e -> e.fetch(ItemComponent.class))
        .map(ItemComponent::item)
        .filter(item -> item instanceof Berry)
        .map(item -> (Berry) item)
        .orElse(null);
  }

  /**
   * Öffnet oder schließt das Inventar des Helden.
   *
   * <p>Wenn bereits eine Inventar-UI geöffnet ist, wird sie geschlossen. Andernfalls wird sie
   * geöffnet.
   */
  public void openInventory() {
    if (hero.fetch(PlayerComponent.class).get().openDialogs()) {
      return;
    }

    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null) {
      if (uiComponent.dialog() instanceof GUICombination) {
        InventoryGUI.inHeroInventory = false;
        hero.remove(UIComponent.class);
      }
    } else {
      InventoryGUI.inHeroInventory = true;
      hero.add(
          new UIComponent(
              new GUICombination(new InventoryGUI(hero.fetch(InventoryComponent.class).get())),
              true));
    }
  }

  /**
   * Zerstört (löscht) ein Item an der angegebenen Position, sofern vorhanden.
   *
   * @param point Die Zielposition.
   */
  public void destroyItemAt(Point point) {
    if (point == null) return;
    Entity e = Game.entityAtTile(Game.tileAT(point)).findFirst().orElse(null);
    if (e != null && e.isPresent(ItemComponent.class)) {
      Game.remove(e);
    }
  }
}
