package dojo.rooms;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import dojo.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Class for managing tasks in the rooms. */
public class TaskRoom extends Room {
  private final List<Task> roomTasks = new ArrayList<>();

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public TaskRoom(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Adds an uncompleted task.
   *
   * @param task the task to add
   */
  public void addTask(Task task) {
    roomTasks.add(task);
  }

  /**
   * Returns the next uncompleted task.
   *
   * @return the next uncompleted task
   */
  public Optional<Task> getNextUncompletedTask() {
    return roomTasks.stream().filter(t -> !t.isCompleted()).findFirst();
  }

  /**
   * Returns the next uncompleted task with the given name.
   *
   * @param taskName name the task must have
   * @return the next uncompleted task with the given name
   */
  public Optional<Task> getNextUncompletedTaskByName(String taskName) {
    return getNextUncompletedTask().stream()
        .filter(t -> t.getTaskName().equals(taskName))
        .findFirst();
  }

  /**
   * Returns if all tasks are completed.
   *
   * @return true if all tasks are completed
   */
  public boolean areAllTasksCompleted() {
    return roomTasks.stream().allMatch(Task::isCompleted);
  }
}
