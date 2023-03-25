package graphic.hud.menus.startmenu;

public interface IStartMenuObserver {
    void onSinglePlayerModeChosen();
    void onMultiPlayerHostModeChosen();
    void onMultiPlayerClientModeChosen(String hostAddress, Integer port);
}
