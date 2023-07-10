package contrib.utils.multiplayer.client;


public interface IMultiplayerClient {

    /**
     * Used to implement sending objects over TCP.
     *
     * @param object To be sent object.
     */
    void sendTCP(Object object);

    /**
     * Used to implement sending objects over TCP.
     *
     * @param object To be sent object.
     */
    void sendUDP(Object object);

    /**
     * Used to implement connecting to an endpoint.
     *
     * @param address IP address of device to be connected to.
     * @param port Port to be connected to. Will be used as TCP port. UDP port will be TCP port + 1;
     * @return Should return true, if connected successfully. False, otherwise.
     */
    boolean connectToHost(String address, int port);

    /**
     * Used to implement disconnecting from endpoint.
     */
    void disconnect();

    /**
     * Used to implement checking whether client is connected or not.
     *
     * @return Should return true, if connected to an endpoint. False, otherwise.
     */
    boolean isConnected();

    /**
     * Add observer to implement customized actions.
     *
     * @param observer Observer reference to be added.
     */
    void addObserver(IMultiplayerClientObserver observer);

    /**
     * Remove observer.
     *
     * @param observer Observer reference to be removed.
     */
    void removeObserver(IMultiplayerClientObserver observer);
}
