package produsAdvanced.abstraction;

import contrib.components.HealthComponent;
import contrib.systems.EventScheduler;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import produsAdvanced.level.AdvancedSortLevel;

/**
 * Die Klasse {@code Monster} stellt eine Abstraktion für ein Monster-Entity im Spiel dar. Sie
 * kapselt eine {@link core.Entity}-Instanz und bietet Methoden zum Zugriff auf relevante
 * Komponenten wie Position und Lebenspunkte.
 *
 * <p>Diese Klasse erleichtert den Umgang mit Monstern, indem sie eine klar definierte Schnittstelle
 * für typische Operationen bietet, wie das Abfragen und Setzen der Position oder den Vergleich
 * anhand der Lebenspunkte.
 */
public class Monster {

  private final Entity monster;
  private static final int TINT_COLOR = 0xff5555ff;

  /**
   * Erstellt ein neues {@code Monster}-Objekt auf Basis einer bestehenden {@link Entity}.
   *
   * @param monster Die Entity, die dieses Monster repräsentiert.
   */
  public Monster(Entity monster) {
    this.monster = monster;
  }

  /**
   * Gibt die aktuellen Lebenspunkte des Monsters zurück.
   *
   * @return Anzahl der aktuellen Lebenspunkte.
   */
  public int getHealthPoints() {
    return monster
        .fetch(HealthComponent.class)
        .map(HealthComponent::currentHealthpoints)
        .orElseThrow();
  }

  /**
   * Gibt die aktuelle Position des Monsters im Spielraum zurück.
   *
   * @return Die Position als {@link Point}.
   */
  public Point getPosition() {
    return monster.fetch(PositionComponent.class).get().position();
  }

  /**
   * Setzt die Position des Monsters auf eine neue Position.
   *
   * @param newPosition Die neue Position als {@link Point}.
   */
  public void setPosition(Point newPosition) {
    monster.fetch(PositionComponent.class).get().position(newPosition);
  }

  /**
   * Tauscht die Position dieses Monsters mit der eines anderen Monsters.
   *
   * <p><b>Hinweis:</b> Derzeit erfolgt der Tausch ohne Animation.
   *
   * @param other Das andere Monster, mit dem die Position getauscht wird.
   */
  public void swapPosition(Monster other) {
    EventScheduler.scheduleAction(
        () -> {
          Point p = getPosition();
          this.setPosition(other.getPosition());
          other.setPosition(p);
          tintColor(TINT_COLOR);
          other.tintColor(TINT_COLOR);
          EventScheduler.scheduleAction(
              () -> {
                tintColor(-1);
                other.tintColor(-1);
              },
              AdvancedSortLevel.DELAY_UNTINT);
        },
        AdvancedSortLevel.DELAY * AdvancedSortLevel.delay_multiplication++);
  }

  private void tintColor(int tintColor) {
    monster.fetch(DrawComponent.class).get().tintColor(tintColor);
  }

  /**
   * Vergleicht dieses Monster mit einem anderen Objekt. Zwei Monster gelten als gleich, wenn sie
   * die gleiche Anzahl an Lebenspunkten haben.
   *
   * @param obj Das zu vergleichende Objekt.
   * @return {@code true}, wenn das Objekt ein Monster mit gleichen Lebenspunkten ist, sonst {@code
   *     false}.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Monster other = (Monster) obj;
    return this.getHealthPoints() == other.getHealthPoints();
  }
}
