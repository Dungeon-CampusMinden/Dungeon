package core.level.utils;

import core.level.Tile;

/**
 * ILevelObserver is an interface that represents an observer in the Observer design pattern.
 *
 * <p>This interface is specifically designed to observe level events in the game. It defines a
 * single method, onLevelEvent, which is called to notify the observer of level-related changes in
 * the entity it is observing.
 */
public interface ILevelObserver {
  /**
   * Called to notify the observer of level-related changes in the entity it is observing.
   *
   * @param tile The tile that the level event is related to.
   * @param levelEvent The type of level event (HERO_TILE_CHANGED).
   */
  void onLevelEvent(Tile tile, LevelEvent levelEvent);

  /**
   * LevelEvent is an enumeration that represents the type of level event.
   *
   * <p>It has the following values: - HERO_TILE_CHANGED: Represents a hero tile change event.
   */
  enum LevelEvent {
    HERO_TILE_CHANGED,
  }
}
