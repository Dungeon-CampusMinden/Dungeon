package core.gui.events;

public interface Cancelable {

    boolean isCanceled();

    void setCanceled(boolean canceled);
}
