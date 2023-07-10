package contrib.utils.multiplayer.server;

import com.badlogic.gdx.utils.Null;

import java.io.IOException;

public interface IMultiplayerServer {

    /**
     * Used to implement inherited instances to listen on connections.
     *
     * @param port A preconfigured TCP port.
     * @throws IOException Should throw IOException if port can not be used.
     */
    void startListening(@Null Integer port) throws IOException;

    /**
     * Used to implement closing session and ports.
     */
    void stopListening();
}
