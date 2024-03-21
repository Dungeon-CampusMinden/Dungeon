package level;

import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.Game;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import level.room.DojoRoom;
import level.room.task.Task;
import starter.DojoStarter;

public abstract class TaskRoomGenerator {
  private final RoomGenerator gen;
  private final DojoRoom room;
  private final DojoRoom nextNeighbour;
  private final List<Task> roomTasks = new ArrayList<>();

  public TaskRoomGenerator(RoomGenerator gen, DojoRoom room, DojoRoom nextNeighbour) {
    this.gen = gen;
    this.room = room;
    this.nextNeighbour = nextNeighbour;
  }

  public void addTask(Task task) {
    roomTasks.add(task);
  }

  public Optional<Task> getNextUncompletedTask() {
    return roomTasks.stream().filter(t -> !t.isCompleted()).findFirst();
  }

  public Optional<Task> getNextUncompletedTaskIfName(String taskName) {
    return getNextUncompletedTask().stream()
        .filter(t -> t.getTaskName().equals(taskName))
        .findFirst();
  }

  public boolean areAllTasksCompleted() {
    return roomTasks.stream().allMatch(Task::isCompleted);
  }

  /**
   * Add the entities as payload to the LevelNode.
   *
   * <p>This will add the entities (in the node payload) to the game, at the moment the level get
   * loaded for the first time.
   *
   * @param roomEntities
   */
  public void addRoomEntities(Set<Entity> roomEntities) {
    room.addEntities(roomEntities);

    room.level().onFirstLoad(() -> room.entities().forEach(Game::add));
  }

  public void openDoors() {
    DojoStarter.openDoors(getRoom(), getNextNeighbour());
  }

  public RoomGenerator getGen() {
    return gen;
  }

  public DojoRoom getRoom() {
    return room;
  }

  public DojoRoom getNextNeighbour() {
    return nextNeighbour;
  }

  public abstract void generateRoom() throws IOException;
}
