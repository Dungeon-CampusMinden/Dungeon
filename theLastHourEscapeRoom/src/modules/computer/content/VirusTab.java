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
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import java.util.*;
import modules.computer.ComputerDialog;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;

public class VirusTab extends ComputerTab {

  public static String KEY = "virus";

  // Maze dimensions (odd numbers work best for maze generation)
  private static final int MAZE_WIDTH = 32;
  private static final int MAZE_HEIGHT = 21;
  private static final int TILE_SIZE = 22;
  private static final float MOVE_COOLDOWN = 0.04f;
  private static final float VIRUS_CHANCE = 0.10f; // 10% chance for floor tiles to become virus tiles
  private static final float EXTRA_PATH_CHANCE = 0.12f; // Chance to add extra connections for branching

  // Tile types
  private static final int WALL = 0;
  private static final int FLOOR = 1;
  private static final int START = 2;
  private static final int GOAL = 3;
  private static final int VIRUS = 4;

  // Colors for tiles
  private static final Color COLOR_WALL = new Color(0.15f, 0.15f, 0.2f, 1f);
  private static final Color COLOR_FLOOR = new Color(0.4f, 0.4f, 0.45f, 1f);
  private static final Color COLOR_FLOOR_OCCUPIED = new Color(0.8f, 0.8f, 0.85f, 1f);
  private static final Color COLOR_START = new Color(0.2f, 0.6f, 0.2f, 1f);
  private static final Color COLOR_GOAL = new Color(0.2f, 0.5f, 0.9f, 1f);
  private static final Color COLOR_VIRUS = new Color(0.8f, 0.2f, 0.2f, 1f);
  private static final Color COLOR_PLAYER = new Color(1f, 0.9f, 0.2f, 1f);

  private int[][] maze;
  private int playerX, playerY;
  private int startX, startY;
  private int goalX, goalY;
  private float moveCooldownTimer = 0f;
  private boolean gameWon = false;
  private Set<Long> criticalPath; // Tiles that must remain passable

  private Image playerImage;
  private Stack[][] tileStacks;
  private Label virusLabel;
  private Random random;

  public VirusTab(ComputerStateComponent sharedState) {
    super(sharedState, "virus", "*+*+* VIRUS *+*+*", false);
  }

  protected void createActors() {
    random = new Random();
    this.clearChildren();

    virusLabel = Scene2dElementFactory.createLabel("COMPUTER IS INFECTED", 48, Color.WHITE);
    virusLabel.setColor(Color.RED);
    virusLabel.setAlignment(Align.center);
    this.add(virusLabel).expandX().center().row();

    Label explainLabel = Scene2dElementFactory.createLabel("Navigate to the blue exit! (WASD to move)", 24, Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(5).row();

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

    // Add extra paths to create more branching and loops
    addExtraPaths();

    // Set start position (top-left inner corner)
    startX = 1;
    startY = 1;
    maze[startY][startX] = START;
    playerX = startX;
    playerY = startY;

    // Find a position on the edge for the goal
    placeGoalOnEdge();

    // Find and store the critical path using BFS
    criticalPath = findCriticalPath();

    // Add some virus tiles on random floor tiles (but not on critical path)
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

  private void addExtraPaths() {
    // Add extra connections to create loops and multiple paths
    for (int y = 2; y < MAZE_HEIGHT - 2; y++) {
      for (int x = 2; x < MAZE_WIDTH - 2; x++) {
        // Only consider wall cells that could connect two floor areas
        if (maze[y][x] == WALL && random.nextFloat() < EXTRA_PATH_CHANCE) {
          // Check if this wall separates two floor tiles horizontally
          if (maze[y][x - 1] == FLOOR && maze[y][x + 1] == FLOOR) {
            maze[y][x] = FLOOR;
          }
          // Check if this wall separates two floor tiles vertically
          else if (maze[y - 1][x] == FLOOR && maze[y + 1][x] == FLOOR) {
            maze[y][x] = FLOOR;
          }
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

  private Set<Long> findCriticalPath() {
    // Use BFS to find a path from start to goal
    Set<Long> path = new HashSet<>();
    Map<Long, Long> cameFrom = new HashMap<>();
    Queue<int[]> queue = new LinkedList<>();

    queue.add(new int[]{startX, startY});
    cameFrom.put(coordToKey(startX, startY), -1L);

    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    while (!queue.isEmpty()) {
      int[] current = queue.poll();
      int cx = current[0];
      int cy = current[1];

      if (cx == goalX && cy == goalY) {
        // Reconstruct path
        long key = coordToKey(goalX, goalY);
        while (key != -1L) {
          path.add(key);
          key = cameFrom.getOrDefault(key, -1L);
        }
        return path;
      }

      for (int[] dir : directions) {
        int nx = cx + dir[0];
        int ny = cy + dir[1];

        if (nx >= 0 && nx < MAZE_WIDTH && ny >= 0 && ny < MAZE_HEIGHT) {
          int tile = maze[ny][nx];
          if ((tile == FLOOR || tile == START || tile == GOAL) && !cameFrom.containsKey(coordToKey(nx, ny))) {
            queue.add(new int[]{nx, ny});
            cameFrom.put(coordToKey(nx, ny), coordToKey(cx, cy));
          }
        }
      }
    }

    return path; // Empty if no path found (shouldn't happen with proper maze)
  }

  private long coordToKey(int x, int y) {
    return ((long) y << 16) | (x & 0xFFFF);
  }

  private void addVirusTiles() {
    for (int y = 0; y < MAZE_HEIGHT; y++) {
      for (int x = 0; x < MAZE_WIDTH; x++) {
        if (maze[y][x] == FLOOR && random.nextFloat() < VIRUS_CHANCE) {
          // Don't place virus too close to start
          if (Math.abs(x - startX) + Math.abs(y - startY) > 3) {
            // Don't place virus on critical path
            if (!criticalPath.contains(coordToKey(x, y))) {
              maze[y][x] = VIRUS;
            }
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

        int tileType = maze[y][x];

        // Add floor tile as base for virus and goal tiles
        if (tileType == VIRUS || tileType == GOAL) {
          Image floorImage = new Image(createColorDrawable(COLOR_FLOOR_OCCUPIED));
          stack.add(floorImage);

          // Add icon on top
          Image iconImage = new Image(tileType == VIRUS ? skin.getDrawable("skull") : skin.getDrawable("flag_square"));
          iconImage.setColor(tileType == VIRUS ? COLOR_VIRUS : COLOR_GOAL);
          stack.add(iconImage);
        } else {
          Image tileImage = new Image(createColorDrawable(getTileColor(tileType)));
          stack.add(tileImage);
        }

        // Add player on start position
        if (x == playerX && y == playerY) {
          playerImage = new Image(skin.getDrawable("chess_king"));
          playerImage.setColor(COLOR_PLAYER);
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

    if (newX < 0 || newX >= MAZE_WIDTH || newY < 0 || newY >= MAZE_HEIGHT) {
      return;
    }

    int targetTile = maze[newY][newX];
    if (targetTile == WALL) {
      return;
    }

    if (tileStacks[playerY][playerX].getChildren().size > 1) {
      tileStacks[playerY][playerX].removeActor(playerImage);
    }

    playerX = newX;
    playerY = newY;

    playerImage = new Image(skin.getDrawable("chess_king"));
    playerImage.setColor(COLOR_PLAYER);
    tileStacks[playerY][playerX].add(playerImage);

    moveCooldownTimer = MOVE_COOLDOWN;

    // Check for special tiles
    if (targetTile == VIRUS) {
      this.addAction(Actions.sequence(
          Actions.delay(0.3f),
          Actions.run(this::resetToStart)
      ));
      Sounds.play(LastHourSounds.COMPUTER_VIRUS_CAUGHT);
    } else if (targetTile == GOAL) {
      onGameWon();
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      Sounds.play(CoreSounds.INTERFACE_ITEM_HOVERED);
    }
  }

  private void resetToStart() {
    if (tileStacks[playerY][playerX].getChildren().size > 1) {
      tileStacks[playerY][playerX].removeActor(playerImage);
    }

    playerX = startX;
    playerY = startY;

    // Add player to start
    playerImage = new Image(createColorDrawable(COLOR_PLAYER));
    tileStacks[playerY][playerX].add(playerImage);
  }

  private void onGameWon() {
    gameWon = true;
    virusLabel.setText("Virus defeated! System secured.");
    virusLabel.setColor(new Color(0.0f, 0.5f, 0.0f, 1f)); // Dark green for visibility on white

    this.addAction(Actions.sequence(
        Actions.delay(1.5f),
        Actions.run(() -> {
          DialogCallbackResolver.createButtonCallback(context().dialogId(), ComputerFactory.UPDATE_STATE_KEY).accept(ComputerStateComponent.getState().withInfection(false));
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
