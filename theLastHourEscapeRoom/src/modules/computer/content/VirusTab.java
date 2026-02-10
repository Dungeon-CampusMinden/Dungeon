package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;

public class VirusTab extends ComputerTab {

  // Maze dimensions (odd numbers work best for maze generation)
  private static final int MAZE_WIDTH = 11;
  private static final int MAZE_HEIGHT = 11;
  private static final int TILE_SIZE = 28;
  private static final float MOVE_COOLDOWN = 0.05f;
  private static final float VIRUS_CHANCE = 0.08f; // 8% chance for floor tiles to become virus tiles

  // Tile types
  private static final int WALL = 0;
  private static final int FLOOR = 1;
  private static final int START = 2;
  private static final int GOAL = 3;
  private static final int VIRUS = 4;

  // Colors for tiles
  private static final Color COLOR_WALL = new Color(0.15f, 0.15f, 0.2f, 1f);
  private static final Color COLOR_FLOOR = new Color(0.4f, 0.4f, 0.45f, 1f);
  private static final Color COLOR_START = new Color(0.2f, 0.6f, 0.2f, 1f);
  private static final Color COLOR_GOAL = new Color(0.2f, 0.5f, 0.9f, 1f);
  private static final Color COLOR_VIRUS = new Color(0.8f, 0.2f, 0.2f, 1f);
  private static final Color COLOR_PLAYER = new Color(1f, 0.9f, 0.2f, 1f);

  private int[][] maze;
  private int playerX, playerY;
  private int startX, startY;
  private float moveCooldownTimer = 0f;
  private boolean gameWon = false;

  private Image playerImage;
  private Stack[][] tileStacks;
  private Label statusLabel;
  private Random random;

  public VirusTab(ComputerStateComponent sharedState) {
    super(sharedState, "virus", "*+*+* VIRUS *+*+*", false);
  }

  protected void createActors() {
    random = new Random();
    this.clearChildren();

    Label virusLabel = Scene2dElementFactory.createLabel("COMPUTER IS INFECTED", 48, Color.RED);
    this.add(virusLabel).expandX().center().row();

    Label explainLabel = Scene2dElementFactory.createLabel("Navigate to the blue exit! (WASD to move)", 24, Color.RED);
    this.add(explainLabel).expandX().center().padTop(5).row();

    statusLabel = Scene2dElementFactory.createLabel("", 20, Color.YELLOW);
    this.add(statusLabel).growX().center().padTop(5).row();

    // Generate and display the maze
    generateMaze();
    Table mazeTable = createMazeTable();
    this.add(mazeTable).center().padTop(15).row();

    // Add keyboard input listener
    setupInputListener();

    // Add continuous action for cooldown timer
    this.addAction(Actions.forever(Actions.run(() -> {
      if (moveCooldownTimer > 0) {
        moveCooldownTimer -= 0.016f; // Approximate delta time
      }
    })));
  }

  private void generateMaze() {
    maze = new int[MAZE_HEIGHT][MAZE_WIDTH];
    // Fill with walls initially
    for (int y = 0; y < MAZE_HEIGHT; y++) {
      for (int x = 0; x < MAZE_WIDTH; x++) {
        maze[y][x] = WALL;
      }
    }

    // Generate maze using recursive backtracking
    // Start from cell (1,1)
    generateMazePath(1, 1);

    // Set start position (top-left inner corner)
    startX = 1;
    startY = 1;
    maze[startY][startX] = START;
    playerX = startX;
    playerY = startY;

    // Find a position on the edge for the goal
    placeGoalOnEdge();

    // Add some virus tiles on random floor tiles
    addVirusTiles();

    gameWon = false;
  }

  private void generateMazePath(int x, int y) {
    maze[y][x] = FLOOR;

    // Directions: right, down, left, up (moving 2 cells at a time)
    int[][] directions = {{2, 0}, {0, 2}, {-2, 0}, {0, -2}};
    List<int[]> dirList = new ArrayList<>();
    Collections.addAll(dirList, directions);
    Collections.shuffle(dirList, random);

    for (int[] dir : dirList) {
      int newX = x + dir[0];
      int newY = y + dir[1];

      // Check bounds (leave outer edge as walls)
      if (newX > 0 && newX < MAZE_WIDTH - 1 && newY > 0 && newY < MAZE_HEIGHT - 1) {
        if (maze[newY][newX] == WALL) {
          // Carve path between current cell and new cell
          maze[y + dir[1] / 2][x + dir[0] / 2] = FLOOR;
          generateMazePath(newX, newY);
        }
      }
    }
  }

  private void placeGoalOnEdge() {
    // Find all floor tiles adjacent to the edge and place goal there
    List<int[]> edgeCandidates = new ArrayList<>();

    // Check bottom edge (y = MAZE_HEIGHT - 2, looking for path to edge)
    for (int x = 1; x < MAZE_WIDTH - 1; x++) {
      if (maze[MAZE_HEIGHT - 2][x] == FLOOR) {
        edgeCandidates.add(new int[]{x, MAZE_HEIGHT - 1});
      }
    }
    // Check right edge
    for (int y = 1; y < MAZE_HEIGHT - 1; y++) {
      if (maze[y][MAZE_WIDTH - 2] == FLOOR) {
        edgeCandidates.add(new int[]{MAZE_WIDTH - 1, y});
      }
    }

    int goalX, goalY;
    if (!edgeCandidates.isEmpty()) {
      int[] goal = edgeCandidates.get(random.nextInt(edgeCandidates.size()));
      goalX = goal[0];
      goalY = goal[1];
    } else {
      // Fallback: place at bottom-right corner area
      goalX = MAZE_WIDTH - 1;
      goalY = MAZE_HEIGHT - 2;
      // Ensure there's a path to it
      maze[goalY][goalX - 1] = FLOOR;
    }
    maze[goalY][goalX] = GOAL;
  }

  private void addVirusTiles() {
    for (int y = 0; y < MAZE_HEIGHT; y++) {
      for (int x = 0; x < MAZE_WIDTH; x++) {
        if (maze[y][x] == FLOOR && random.nextFloat() < VIRUS_CHANCE) {
          // Don't place virus too close to start
          if (Math.abs(x - startX) + Math.abs(y - startY) > 3) {
            maze[y][x] = VIRUS;
          }
        }
      }
    }
  }

  private Table createMazeTable() {
    Table table = new Table();
    table.setBackground(createColorDrawable(Color.BLACK));
    table.pad(2);

    tileStacks = new Stack[MAZE_HEIGHT][MAZE_WIDTH];

    for (int y = 0; y < MAZE_HEIGHT; y++) {
      for (int x = 0; x < MAZE_WIDTH; x++) {
        Stack stack = new Stack();
        tileStacks[y][x] = stack;

        // Add tile background
        Image tileImage = new Image(createColorDrawable(getTileColor(maze[y][x])));
        stack.add(tileImage);

        // Add player on start position
        if (x == playerX && y == playerY) {
          playerImage = new Image(createColorDrawable(COLOR_PLAYER));
          stack.add(playerImage);
        }

        table.add(stack).size(TILE_SIZE, TILE_SIZE).pad(1);
      }
      table.row();
    }

    return table;
  }

  private Color getTileColor(int tileType) {
    return switch (tileType) {
      case WALL -> COLOR_WALL;
      case FLOOR -> COLOR_FLOOR;
      case START -> COLOR_START;
      case GOAL -> COLOR_GOAL;
      case VIRUS -> COLOR_VIRUS;
      default -> COLOR_WALL;
    };
  }

  private TextureRegionDrawable createColorDrawable(Color color) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(texture);
  }

  private void setupInputListener() {
    this.setTouchable(Touchable.enabled);
    this.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (gameWon || moveCooldownTimer > 0) return false;

        int dx = 0, dy = 0;
        switch (keycode) {
          case Input.Keys.W, Input.Keys.UP -> dy = -1;
          case Input.Keys.S, Input.Keys.DOWN -> dy = 1;
          case Input.Keys.A, Input.Keys.LEFT -> dx = -1;
          case Input.Keys.D, Input.Keys.RIGHT -> dx = 1;
          default -> { return false; }
        }

        movePlayer(dx, dy);
        return true;
      }
    });
  }

  private void movePlayer(int dx, int dy) {
    int newX = playerX + dx;
    int newY = playerY + dy;

    // Check bounds
    if (newX < 0 || newX >= MAZE_WIDTH || newY < 0 || newY >= MAZE_HEIGHT) {
      return;
    }

    int targetTile = maze[newY][newX];

    // Can't move into walls
    if (targetTile == WALL) {
      return;
    }

    // Remove player from current position
    if (tileStacks[playerY][playerX].getChildren().size > 1) {
      tileStacks[playerY][playerX].removeActor(playerImage);
    }

    // Update position
    playerX = newX;
    playerY = newY;

    // Add player to new position
    playerImage = new Image(createColorDrawable(COLOR_PLAYER));
    tileStacks[playerY][playerX].add(playerImage);

    moveCooldownTimer = MOVE_COOLDOWN;

    // Check for special tiles
    if (targetTile == VIRUS) {
      statusLabel.setText("Virus hit! Resetting...");
      // Reset to start with a small delay
      this.addAction(Actions.sequence(
          Actions.delay(0.3f),
          Actions.run(this::resetToStart)
      ));
      Sounds.play(LastHourSounds.COMPUTER_VIRUS_CAUGHT);
    } else if (targetTile == GOAL) {
      onGameWon();
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      statusLabel.setText("");
      Sounds.play(CoreSounds.INTERFACE_ITEM_HOVERED);
    }
  }

  private void resetToStart() {
    // Remove player from current position
    if (tileStacks[playerY][playerX].getChildren().size > 1) {
      tileStacks[playerY][playerX].removeActor(playerImage);
    }

    // Reset position
    playerX = startX;
    playerY = startY;

    // Add player to start
    playerImage = new Image(createColorDrawable(COLOR_PLAYER));
    tileStacks[playerY][playerX].add(playerImage);

    statusLabel.setText("");
  }

  private void onGameWon() {
    gameWon = true;
    statusLabel.setText("VIRUS DEFEATED!");
    statusLabel.setColor(Color.GREEN);

    // Close this tab after a short delay
    this.addAction(Actions.sequence(
        Actions.delay(1.5f),
        Actions.run(() -> {
          // Clear infection state
          ComputerStateComponent.setInfection(false);
          // Close the virus tab via the ComputerDialog
          ComputerDialog.getInstance().ifPresent(dialog -> dialog.closeTab("virus"));
        })
    ));
  }

  @Override
  public Actor hit(float x, float y, boolean touchable) {
    // Make sure this tab can receive keyboard focus
    if (getStage() != null) {
      getStage().setKeyboardFocus(this);
    }
    return super.hit(x, y, touchable);
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}
}
