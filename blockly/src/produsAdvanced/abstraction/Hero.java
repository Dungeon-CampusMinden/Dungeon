package produsAdvanced.abstraction;

import com.badlogic.gdx.Input;
import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
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
import java.util.function.Supplier;

/**
 * Die Klasse {@code Hero} kapselt eine Spielfigur (Entity) und stellt Methoden zur Steuerung und
 * Interaktion bereit.
 *
 * <p>Diese Klasse bietet eine Abstraktion für Spielfiguren, die vom Spieler gesteuert werden
 * können. Sie unterstützt insbesondere Bewegung sowie das Ausführen einer Feuerball-Fähigkeit mit
 * Abklingzeit.
 */
public class Hero {
  /** Die Entity, die diesen Helden repräsentiert. */
  private Entity hero;

  /** Abklingzeit (in Millisekunden) für das erneute Ausführen der Feuerball-Fähigkeit. */
  private int FIREBALL_COOL_DOWN = 500;

  /** Zielposition für den Feuerball, standardmäßig (0, 0). */
  private Supplier<Point> fireballTarget = () -> new Point(0, 0);

  /** Instanz der Feuerball-Fähigkeit, die erneut erstellt wird, wenn sie ausgeführt wird. */
  private Skill fireball = new Skill(new FireballSkill(fireballTarget), FIREBALL_COOL_DOWN);

  /**
   * Konstruktor.
   *
   * @param heroInstance Die Entity, die diesen Helden darstellt. Muss ein {@link PlayerComponent}
   *     enthalten.
   */
  public Hero(Entity heroInstance) {
    this.hero = heroInstance;
    PlayerComponent pc = heroInstance.fetch(PlayerComponent.class).get();
    pc.removeCallbacks(); // Entfernt alle bisherigen Tastenzuweisungen
  }

  /**
   * Setzt den Controller für die Spielfigur.
   *
   * <p>Registriert Tastendrücke (Keys von 0 bis Z) und leitet sie an den übergebenen {@link
   * PlayerController} weiter, der die Verarbeitung übernimmt.
   *
   * @param controller Ein {@link PlayerController}-Objekt, das die Eingaben verarbeitet. Wenn
   *     {@code null}, wird keine Aktion durchgeführt.
   */
  public void setController(PlayerController controller) {
    if (controller == null) return;
    PlayerComponent pc = hero.fetch(PlayerComponent.class).get();
    for (int key = 0; key <= Input.Keys.Z; key++) {
      int finalKey = key;
      pc.registerCallback(key, entity -> controller.processKey(Input.Keys.toString(finalKey)));
    }

    pc.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (e) -> {
          var firstUI =
              Game.entityStream() // would be nice to directly access HudSystems
                  // stream (no access to the System object)
                  .filter(x -> x.isPresent(UIComponent.class)) // find all Entities
                  // which have a
                  // UIComponent
                  .map(
                      x ->
                          new Tuple<>(
                              x,
                              x.fetch(UIComponent.class)
                                  .orElseThrow(
                                      () ->
                                          MissingComponentException.build(
                                              x, UIComponent.class)))) // create a tuple to
                  // still have access to
                  // the UI Entity
                  .filter(x -> x.b().closeOnUICloseKey())
                  .max(Comparator.comparingInt(x -> x.b().dialog().getZIndex())) // find dialog
                  // with highest
                  // z-Index
                  .orElse(null);
          if (firstUI != null) {
            InventoryGUI.inHeroInventory = false;
            firstUI.a().remove(UIComponent.class);
            if (firstUI.a().componentStream().findAny().isEmpty()) {
              Game.remove(firstUI.a()); // delete unused Entity
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
   * @return Die aktuelle Mauszeigerposition als {@link Point}.
   */
  public Point getMousePosition() {
    return SkillTools.cursorPositionAsPoint();
  }

  /**
   * Führt die Feuerball-Fähigkeit in die angegebene Richtung aus, sofern die Abklingzeit abgelaufen
   * ist.
   *
   * <p>Die Fähigkeit wird mit einem neuen Zielpunkt ausgeführt und anschließend als "verwendet"
   * markiert, um die Abklingzeit zu starten.
   *
   * @param direction Zielposition des Feuerballs.
   */
  public void shootFireball(Point direction) {
    Supplier<Point> newTarget = () -> direction;
    Skill newFireball = new Skill(new FireballSkill(fireballTarget), FIREBALL_COOL_DOWN);
    if (fireball.canBeUsedAgain()) {
      newFireball.execute(hero);
      newFireball.setLastUsedToNow();
    }
    fireball = newFireball;
    fireballTarget = newTarget;
  }

  /** Führt eine Interaktion mit einem Objekt in der Nähe des Helden aus. */
  public void interact() {
    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null
        && uiComponent.dialog() instanceof GUICombination
        && !InventoryGUI.inHeroInventory) {
      // if chest or cauldron
      hero.remove(UIComponent.class);
    } else {
      InteractionTool.interactWithClosestInteractable(hero);
    }
  }

  public Berry getBerryAt(Point point) {
    if (point == null) return null;
    Tile t = Game.tileAT(point);
    if (t == null) return null;
    return Game.entityAtTile(Game.tileAT(point))
        .findFirst()
        .flatMap(e -> e.fetch(ItemComponent.class))
        .map(ItemComponent::item)
        .filter(item -> item instanceof Berry)
        .map(item -> (Berry) item)
        .orElse(null);
  }

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

  public void destroyItemAt(Point point) {
    if (point == null) return;
    Entity e = Game.entityAtTile(Game.tileAT(point)).findFirst().orElse(null);
    if (e != null && e.isPresent(ItemComponent.class)) Game.remove(e);
  }
}
