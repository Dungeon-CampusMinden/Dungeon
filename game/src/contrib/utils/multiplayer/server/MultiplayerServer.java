package contrib.utils.multiplayer.server;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import contrib.utils.multiplayer.packages.NetworkSetup;
import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.event.MovementEvent;
import contrib.utils.multiplayer.packages.request.ChangeMapRequest;
import contrib.utils.multiplayer.packages.request.InitializeServerRequest;
import contrib.utils.multiplayer.packages.request.JoinSessionRequest;
import contrib.utils.multiplayer.packages.request.LoadMapRequest;
import contrib.utils.multiplayer.packages.request.PingRequest;

import java.io.IOException;
import java.util.ArrayList;

/** Concrete implementation of {@link IServer} to send */
public class MultiplayerServer extends Listener implements IServer {
    public static final Version VERSION = new Version(0, 0, 0);
    // According to several tests, random generated level is the largest object to be sent
    // and can have a maximum size of about 500k bytes
    // => set max expected size to double
    private static final int MAX_OBJECT_SIZE_EXPECTED = 8000000;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = MAX_OBJECT_SIZE_EXPECTED;
    private static final int DEFAULT_OBJECT_BUFFER_SIZE = MAX_OBJECT_SIZE_EXPECTED;
    private final Server server = new Server(DEFAULT_WRITE_BUFFER_SIZE, DEFAULT_OBJECT_BUFFER_SIZE);
    private final ArrayList<IServerObserver> observers = new ArrayList<>();

    /** Creates a new Instance. */
    public MultiplayerServer() {
        server.addListener(this);
        NetworkSetup.registerCommunicationClasses(server);
    }

    @Override
    public void connected(Connection connection) {
        for (IServerObserver observer : observers) {
            observer.onClientConnected(connection.getID());
        }
    }

    @Override
    public void disconnected(Connection connection) {
        for (IServerObserver observer : observers) {
            observer.onClientDisconnected(connection.getID());
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof PingRequest pingRequest) {
            for (IServerObserver observer : observers) {
                observer.onPingRequestReceived(connection.getID(), pingRequest);
            }
        } else if (object instanceof InitializeServerRequest initServerRequest) {
            for (IServerObserver observer : observers) {
                observer.onInitializeRequestReceived(connection.getID(), initServerRequest);
            }
        } else if (object instanceof LoadMapRequest loadMapRequest) {
            for (IServerObserver observer : observers) {
                observer.onLoadMapRequestReceived(connection.getID(), loadMapRequest);
            }
        } else if (object instanceof ChangeMapRequest changeMapRequest) {
            for (IServerObserver observer : observers) {
                observer.onChangeMapRequestReceived(connection.getID(), changeMapRequest);
            }
        } else if (object instanceof JoinSessionRequest joinSessionRequest) {
            for (IServerObserver observer : observers) {
                observer.onJoinSessionRequestReceived(connection.getID(), joinSessionRequest);
            }
        } else if (object instanceof MovementEvent movementEvent) {
            for (IServerObserver observer : observers) {
                observer.onMovementEventReceived(connection.getID(), movementEvent);
            }
        }
    }

    /**
     * @param port a preconfigured TCP port. UDP port will be TCP port + 1. If null, default ports
     *     are used.
     */
    @Override
    public void startListening(@Null Integer port) throws IOException {
        server.bind(
                port != null ? port : DEFAULT_TCP_PORT, port != null ? port + 1 : DEFAULT_UDP_PORT);
        server.start();
    }

    @Override
    public void stopListening() {
        server.close();
    }

    @Override
    public void sendTCP(int clientID, Object object) {
        server.sendToTCP(clientID, object);
    }

    @Override
    public void sendUDP(int clientID, Object object) {
        server.sendToUDP(clientID, object);
    }

    @Override
    public void sendToAllTCP(Object object) {
        server.sendToAllTCP(object);
    }

    @Override
    public void sendToAllUDP(Object object) {
        server.sendToAllUDP(object);
    }

    @Override
    public void sendToAllExceptTCP(int clientID, Object object) {
        server.sendToAllExceptTCP(clientID, object);
    }

    @Override
    public void sendToAllExceptUDP(int clientID, Object object) {
        server.sendToAllExceptUDP(clientID, object);
    }

    @Override
    public void addObserver(final IServerObserver observer) {
        observers.add(requireNonNull(observer));
    }

    @Override
    public void removeObserver(final IServerObserver observer) {
        observers.remove(requireNonNull(observer));
    }
}
