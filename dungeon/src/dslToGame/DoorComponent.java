package dslToGame;

import core.Component;
import core.level.elements.tile.DoorTile;

/**
 * Speichert ein {@link DoorTile} um dieses mit einer Entität zu verbinden.
 *
 * <p>Sollte in Verbindung mit dem {@link task.TaskComponent} verwendet werden.
 *
 * <p>In Kombination kann dann der {@link task.TaskComponent#DOOR_OPENER} Consumer verwendet werden,
 * um beim aktivieren einer Task die entsprechende Tür im level zu öffnen
 *
 * <p>Dafür beim verbinden der Levelgraphen der einzelenen Task die so enstandende Tür in diesen
 * Component speichern und der Manager-Entität umhängen, dann den Callback in
 */
public final class DoorComponent implements Component {

    private final DoorTile door;

    /**
     * Create a new DoorOpenerComponent.
     *
     * @param door DoorTile to store in this component.
     */
    public DoorComponent(final DoorTile door) {
        this.door = door;
    }

    /**
     * @return the doorTile stored in this component.
     */
    public DoorTile door() {
        return door;
    }
}
