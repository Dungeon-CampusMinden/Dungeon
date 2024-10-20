package de.fwatermann.dungine.state;

import de.fwatermann.dungine.audio.AudioContext;
import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.Grid3D;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.functions.IVoidFunction;
import de.fwatermann.dungine.window.GameWindow;
import org.lwjgl.opengl.GL33;

/** Represents a state of the game. It extents the ECS class. */
public abstract class GameState extends ECS implements Disposable, EventListener {

  private Grid3D grid;
  private boolean renderGrid = false;

  /** The game window. */
  protected GameWindow window;

  /** The user interface root element*/
  protected UIRoot ui;

  /** The skybox. */
  protected SkyBox skyBox;

  /** The camera. */
  protected Camera<?> camera;

  /** The audio context. */
  protected AudioContext audioContext;

  /** The last delta time of the last frame. */
  protected float lastFrameDeltaTime = 0.0f;

  /** The last delta time of the last tick. */
  protected float lastTickDeltaTime = 0.0f;

  /**
   * Create a new game state.
   *
   * @param window the game window
   */
  protected GameState(GameWindow window) {
    this.window = window;
    this.ui = new UIRoot(this.window, this.window.size().x, this.window.size().y);
    this.audioContext = new AudioContext();
    this.camera = new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0.0f, 0.0f));

    this.window.runOnMainThread(() -> {
      EventManager.getInstance().registerListener(this);
    });
  }

  /**
   * Initialize this state. This method is called async to the render method. If GL-Context is
   * needed (e.g. for textures) use {@link GameWindow#runOnMainThread(IVoidFunction)}
   */
  public abstract void init();

  /**
   * Get the progress of this state. This is used for loading screens.
   *
   * <p>Default implementation returns 0.
   *
   * @return a value between 0 and 1
   */
  public float getProgress() {
    return 0.0f;
  }

  /**
   * Check if this state is loaded.
   *
   * @return true if this state is loaded
   */
  public abstract boolean loaded();

  /**
   * Render this state.
   *
   * @param deltaTime the time since the last frame in seconds
   */
  public final void render(float deltaTime) {
    this.lastFrameDeltaTime = deltaTime;
    GL33.glEnable(GL33.GL_BLEND);
    GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

    this.camera.update();

    if(this.skyBox != null)
      this.skyBox.render(this.camera);

    this.executeSystems(this, true);
    this.renderState(deltaTime);
    if(this.renderGrid) {
      if(this.grid == null)
        this.grid = new Grid3D();
      this.grid.render(this.camera);
    }
    this.ui.render();
  }

  /**
   * Render this State. This method is called by {@link #render(float)}
   *
   * @param deltaTime The time since the last frame in seconds
   */
  public void renderState(float deltaTime) {}

  /**
   * Update this state. This method is called async to the render method.
   *
   * @param deltaTime the time since the last update in seconds
   */
  public final void update(float deltaTime) {
    this.lastTickDeltaTime = deltaTime;
    this.audioContext.update(deltaTime);
    this.executeSystems(this, false);
    this.updateState(deltaTime);
  }

  /**
   * Update this state. This method is called async to the render method by {@link #update(float)}
   *
   * @param deltaTime the time since the last update in seconds
   */
  public void updateState(float deltaTime) {}

  /**
   * Get the last delta time of the last frame.
   *
   * @return the last delta time of the last frame in seconds
   */
  public float lastFrameDeltaTime() {
    return this.lastFrameDeltaTime;
  }

  /**
   * Get the last delta time of the last tick.
   *
   * @return the last delta time of the last tick in seconds
   */
  public float lastTickDeltaTime() {
    return this.lastTickDeltaTime;
  }

  /**
   * Get the game window.
   *
   * @return the game window
   */
  public GameWindow window() {
    return this.window;
  }

  /**
   * Get whether the grid is rendered.
   * @return true if the grid is rendered
   */
  public boolean grid() {
    return this.renderGrid;
  }

  /**
   * Set whether the grid is rendered.
   * @param renderGrid true if the grid should be rendered
   */
  public void grid(boolean renderGrid) {
    this.renderGrid = renderGrid;
  }

  /**
   * Dispose this state.
   */
  public final void dispose() {
    EventManager.getInstance().unregisterListener(this);
    this.disposeState();
    this.ui.dispose();
    this.audioContext.dispose();
  }

  /**
   * Dispose this state. This method is called by {@link #dispose()}
   */
  public void disposeState() {};

  @EventHandler
  private void onWindowSize(WindowResizeEvent event) {
    if(!event.isCanceled()) {
      this.camera.updateViewport(event.to.x, event.to.y, 0, 0);
    }
  }


}
