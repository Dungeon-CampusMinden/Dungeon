package dojo.rooms;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import dojo.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRoom extends Room {
  private final List<Task> roomTasks = new ArrayList<>();

  public TaskRoom(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public void addTask(Task task) {
    roomTasks.add(task);
  }

  public Optional<Task> getNextUncompletedTask() {
    return roomTasks.stream().filter(t -> !t.isCompleted()).findFirst();
  }

  public Optional<Task> getNextUncompletedTaskByName(String taskName) {
    return getNextUncompletedTask().stream()
        .filter(t -> t.getTaskName().equals(taskName))
        .findFirst();
  }

  public boolean areAllTasksCompleted() {
    return roomTasks.stream().allMatch(Task::isCompleted);
  }
}
