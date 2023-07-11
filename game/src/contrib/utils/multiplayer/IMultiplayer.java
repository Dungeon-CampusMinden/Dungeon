package contrib.utils.multiplayer;

import core.level.elements.ILevel;
import core.utils.Point;

/**
 * Used to customize actions and implement custom game logic based on event occurred in {@link
 * MultiplayerManager}.
 */
public interface IMultiplayer {
    /**
     * Called after request to start a multiplayer session is processed.
     *
     * @param isSucceed True, if session successfully started. False, otherwise.
     */
    void onMultiplayerServerInitialized(boolean isSucceed);

    /**
     * Called after request to join a multiplayer session is processed.
     *
     * @param isSucceed True, if session successfully joined. False, otherwise.
     * @param heroGlobalID For session assigned unique global ID. HAS TO BE SET LOCALLY for own
     *     playable entity.
     * @param level Session level. HAS TO BE SET LOCALLY.
     * @param initialHeroPosition Assigned start position of own playable entity. HAS TO BE SET
     *     LOCALLY.
     */
    void onMultiplayerSessionJoined(
            boolean isSucceed, int heroGlobalID, ILevel level, Point initialHeroPosition);

    /** Called after the connection to multiplayer session is lost. */
    void onMultiplayerSessionConnectionLost();

    /**
     * Called after request to load new map is processed.
     *
     * @param level Level that has been set as level for multiplayer session.
     */
    void onMapLoad(ILevel level);

    /** */
    void onChangeMapRequest();
}
