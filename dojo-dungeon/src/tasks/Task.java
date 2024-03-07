package tasks;

import contrib.level.generator.graphBased.levelGraph.LevelNode;
import java.util.function.Function;
import starter.DojoStarter;

public class Task {
  private final TaskRoomGenerator trGen;
  private final LevelNode currentRoom;
  private final LevelNode nextRoom;
  private final Function<Task, Boolean> onActivated;
  private final Function<Task, Boolean> onDeactivated;
  private boolean isActivated = false;
  private boolean completed = false;

  public Task(
      TaskRoomGenerator trGen,
      LevelNode currentRoom,
      LevelNode nextRoom,
      Function<Task, Boolean> onActivated,
      Function<Task, Boolean> onDeactivated) {
    this.trGen = trGen;
    this.currentRoom = currentRoom;
    this.nextRoom = nextRoom;
    this.onActivated = onActivated;
    this.onDeactivated = onDeactivated;
  }

  public void check() {
    if (isActivated()) {
      if (onActivated.apply(this)) {
        setCompleted(true);
        if (trGen.roomTasks.stream().allMatch(Task::isCompleted)) {
          DojoStarter.openDoors(currentRoom, nextRoom);
        }
      }
    } else {
      onDeactivated.apply(this);
    }
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }
}
