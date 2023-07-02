package core;

public interface IMenuScreenObserver {
    void onSinglePlayerModeChosen();
    void onMultiPlayerHostModeChosen();
    void onMultiPlayerClientModeChosen(String hostAddress, Integer port);
}
