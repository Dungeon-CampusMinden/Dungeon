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
  private Step pending;
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
    if (pending != null) return Optional.of(pending);
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
    Cell to = cell;
    Direction heading = facing;
    switch (action) {
      case LEFT -> heading = facing.left();
      case RIGHT -> heading = facing.right();
      case ATTACK -> {}
      case MOVE, JUMP -> {
        Cell next = cell.next(facing);
        if (action == Action.JUMP) {
          if (!next.equals(LoopMaze.pit())) return fail("Sprung ohne Grube voraus.");
          next = next.next(facing);
        }
        // Walking into a wall stops at the physical collision, not before the move begins.
        if (next.equals(LoopMaze.pit())) return fail("Grube voraus. Schritt gestoppt.");
        if (monsterAlive && next.equals(LoopMaze.monster()))
          return fail("Ein Eindringling versperrt den Weg.");
        to = next;
      }
    }
    pending = new Step(action, cell, to, heading);
    return Optional.of(pending);
  }

  /** Commits the current instruction only after the physical runtime has completed it. */
  public void complete() {
    if (pending == null) throw new IllegalStateException("No physical action to complete");
    Step step = pending;
    pending = null;
    facing = step.facing();
    if (step.action() == Action.ATTACK && cell.next(facing).equals(LoopMaze.monster()))
      monsterAlive = false;
    if (!step.from().equals(step.to())) {
      cell = step.to();
      history.add(cell);
      if (!LoopMaze.cells().contains(cell)) fail("Weg verlassen. Bewegung gestoppt.");
    }
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
   * @return the last physically reached cell
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
   * @return reached movement anchors, including the starting cell
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
