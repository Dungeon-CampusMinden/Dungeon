package rooms.programming.modules.loops;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import rooms.programming.modules.loops.LoopMaze.Cell;
import rooms.programming.modules.loops.LoopMaze.Direction;
import rooms.programming.modules.loops.LoopProgram.Action;

/** One bounded attempt, advanced only after the previous physical action has finished. */
public final class LoopExecution {
  private static final int ITERATION_LIMIT = 24;

  /**
   * An instruction for the physical runtime.
   *
   * @param action action to animate
   * @param from starting cell
   * @param to destination cell, equal to from for stationary actions
   * @param facing resulting heading
   */
  public record Step(Action action, Cell from, Cell to, Direction facing) {}

  private final int checkpoint;
  private final LoopProgram program;
  private final ArrayDeque<Action> actions = new ArrayDeque<>();
  private final List<Cell> history = new ArrayList<>();
  private Cell cell;
  private Direction facing;
  private boolean monsterAlive;
  private int iterations;
  private boolean after;
  private boolean finished;
  private String failure = "";

  /**
   * Starts an attempt at the authoritative checkpoint.
   *
   * @param checkpoint zero-based checkpoint index
   * @param rune collected program to execute
   * @param monsterAlive whether the guardian is still present
   */
  public LoopExecution(int checkpoint, LoopRune rune, boolean monsterAlive) {
    this.checkpoint = checkpoint;
    this.program = rune.program();
    this.cell = LoopMaze.checkpoints().get(checkpoint).start();
    this.facing = LoopMaze.checkpoints().get(checkpoint).facing();
    this.monsterAlive = monsterAlive;
    history.add(cell);
  }

  /**
   * Advances the program after its previous physical action has settled.
   *
   * @return the next action, or empty when execution stops
   */
  public Optional<Step> next() {
    if (finished) return Optional.empty();
    while (actions.isEmpty()) {
      if (after) {
        finished = true;
        return Optional.empty();
      }
      boolean repeat =
          switch (program.type()) {
            case FOR -> iterations < program.count();
            case WHILE -> condition();
            case DO_WHILE -> iterations == 0 || condition();
          };
      if (!repeat) {
        after = true;
        actions.addAll(program.after());
      } else {
        if (iterations++ >= ITERATION_LIMIT) return fail("Ausführung ohne Halt abgebrochen.");
        actions.addAll(program.body());
      }
    }
    Action action = actions.removeFirst();
    Cell from = cell;
    switch (action) {
      case LEFT -> facing = facing.left();
      case RIGHT -> facing = facing.right();
      case ATTACK -> {
        if (cell.next(facing).equals(LoopMaze.monster())) monsterAlive = false;
      }
      case MOVE, JUMP -> {
        Cell next = cell.next(facing);
        if (action == Action.JUMP) {
          if (!next.equals(LoopMaze.pit())) return fail("Sprung ohne Grube voraus.");
          next = next.next(facing);
        }
        if (!LoopMaze.cells().contains(next)) return fail("Wand voraus. Bewegung gestoppt.");
        if (next.equals(LoopMaze.pit())) return fail("Grube voraus. Schritt gestoppt.");
        if (monsterAlive && next.equals(LoopMaze.monster()))
          return fail("Ein Eindringling versperrt den Weg.");
        cell = next;
        history.add(cell);
      }
    }
    return Optional.of(new Step(action, from, cell, facing));
  }

  private Optional<Step> fail(String reason) {
    failure = reason;
    finished = true;
    return Optional.empty();
  }

  private boolean condition() {
    return switch (program.condition()) {
      case FREE ->
          LoopMaze.cells().contains(cell.next(facing)) && !cell.next(facing).equals(LoopMaze.pit());
      case AT_GOAL -> cell.equals(LoopMaze.checkpoints().get(checkpoint).goal());
      case NOT_GOAL -> !cell.equals(LoopMaze.checkpoints().get(checkpoint).goal());
      case ALWAYS -> true;
    };
  }

  /**
   * @return the destination of the latest accepted action
   */
  public Cell cell() {
    return cell;
  }

  /**
   * @return whether the guardian has survived this attempt
   */
  public boolean monsterAlive() {
    return monsterAlive;
  }

  /**
   * @return accepted movement anchors, including the starting cell
   */
  public List<Cell> history() {
    return List.copyOf(history);
  }

  /**
   * @return the stopping error, or an empty string for normal completion
   */
  public String failure() {
    return failure;
  }

  /**
   * @return whether normal completion reached both the goal cell and its heading
   */
  public boolean success() {
    return finished
        && failure.isEmpty()
        && cell.equals(LoopMaze.checkpoints().get(checkpoint).goal())
        && facing == LoopMaze.goalFacing(checkpoint);
  }
}
