package contrib.utils.multiplayer.manager;

import core.level.elements.ILevel;
import core.utils.Point;

public interface IMultiplayerClientManagerObserver {
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
     * @param level Level that has been set as level for multiplayer session. May be null.
     */
    void onMapLoad(ILevel level);

    /** . */
    void onChangeMapRequest();
}
