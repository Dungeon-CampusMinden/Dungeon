package de.fwatermann.dungine.window;

import static de.fwatermann.dungine.utils.ThreadUtils.checkMainThread;
import static org.lwjgl.glfw.GLFW.*;

import de.fwatermann.dungine.Dungine;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.event.window.WindowCloseEvent;
import de.fwatermann.dungine.event.window.WindowFocusChangedEvent;
import de.fwatermann.dungine.event.window.WindowMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.exception.GLFWException;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.IVoidFunction;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLUtil;

public abstract class GameWindow implements Disposable {

  @Nullable public static Thread MAIN_THREAD = null;
  @Nullable public static Thread UPDATE_THREAD = null;

  private String title;
  private Vector2i size;
  private Vector2i position;
  private boolean debug = false;
  private boolean visible = false;
  private boolean resizable = true;
  private boolean rawMouseInput = false;
  private boolean vsync = false;
  private boolean hasFocus = true;
  private boolean fullscreen = false;
  private long frameRate = -1;
  private long tickRate = 50;
  private boolean shouldClose = false;

  private Vector2d mousePosition = new Vector2d(0, 0);
  private long glfwWindow;

  private final ConcurrentLinkedQueue<IVoidFunction> mainThreadQueue =
      new ConcurrentLinkedQueue<>();

  /**
   * Constructs a new GameWindow.
   *
   * @param title the title of the game window
   * @param size the size of the game window as a Vector2i object
   * @param visible the visibility state of the game window
   * @param debug the debug state of the game window
   */
  public GameWindow(String title, Vector2i size, boolean visible, boolean debug) {
    this.title = title;
    this.size = size;
    this.visible = visible;
    this.debug = debug;

    Dungine.WINDOWS.add(this);
  }

  /**
   * Render the game. This method is called every frame. It should <b>ONLY</b> be used to render the
   * game. Attention: This method is called asynchronously to the update method!
   *
   * @param deltaTime The time since the last render in seconds.
   */
  public abstract void render(float deltaTime);

  /**
   * Update the game logic. Attention: This method is called asynchronously to the render method!
   *
   * @param deltaTime The time since the last update in seconds.
   */
  public abstract void update(float deltaTime);

  /** Initialize the game. This method is called once before the render loop starts. */
  public abstract void init();

  /** Clean up the game. This method is called once after the game loop ends. */
  public abstract void cleanup();

  private void _init() {
    this._initGLFW();
    this._initOpenGL();

    if (this.visible) {
      glfwShowWindow(this.glfwWindow);
    }
    MAIN_THREAD = Thread.currentThread();
    this._renderLoop();
  }

  private void _initGLFW() {
    GLFWErrorCallback.createPrint(System.err).set();
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, this.visible ? GLFW_TRUE : GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, this.resizable ? GLFW_TRUE : GLFW_FALSE);
    if (this.debug) {
      glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
    }
    this.glfwWindow = glfwCreateWindow(this.size.x, this.size.y, this.title, 0L, 0L);
    if (this.glfwWindow == 0L) {
      throw new GLFWException("Unable to create GLFW window");
    }

    glfwMakeContextCurrent(this.glfwWindow);
    glfwSwapInterval(this.vsync ? 1 : 0);

    // Register Callbacks
    glfwSetWindowSizeCallback(
        this.glfwWindow,
        (window, width, height) -> {
          WindowResizeEvent event =
              new WindowResizeEvent(this.size, new Vector2i(width, height), this);
          event.fire();
          if (event.isCanceled()) {
            glfwSetWindowSize(window, event.from.x, event.from.y);
          }
          this.size = new Vector2i(width, height);
        });

    glfwSetFramebufferSizeCallback(
        this.glfwWindow,
        (window, width, height) -> {
          GL33.glViewport(0, 0, width, height);
        });

    glfwSetWindowPosCallback(
        this.glfwWindow,
        (window, x, y) -> {
          WindowMoveEvent event = new WindowMoveEvent(this.position, new Vector2i(x, y));
          event.fire();
          if (event.isCanceled()) {
            glfwSetWindowPos(window, event.from.x, event.from.y);
          }
          this.position = event.to;
        });

    glfwSetWindowCloseCallback(
        this.glfwWindow,
        window -> {
          WindowCloseEvent event = new WindowCloseEvent(this);
          event.fire();
          if (event.isCanceled()) {
            glfwSetWindowShouldClose(window, false);
            return;
          }
          glfwSetWindowShouldClose(window, true);
          this.shouldClose = true;
          this.title(this.title + " - Closing...");
        });

    glfwSetWindowFocusCallback(
        this.glfwWindow,
        (window, focused) -> {
          WindowFocusChangedEvent event = new WindowFocusChangedEvent(focused, this);
          event.fire();
          this.hasFocus = focused;
        });

    glfwSetMouseButtonCallback(
        this.glfwWindow,
        (window, button, action, mods) -> {
          MouseButtonEvent.MouseButtonAction mouseButtonAction =
              switch (action) {
                case GLFW_PRESS -> MouseButtonEvent.MouseButtonAction.PRESS;
                case GLFW_RELEASE -> MouseButtonEvent.MouseButtonAction.RELEASE;
                case GLFW_REPEAT -> MouseButtonEvent.MouseButtonAction.REPEAT;
                default -> throw new IllegalStateException("Unexpected value: " + action);
              };
          MouseButtonEvent event = new MouseButtonEvent(button, mouseButtonAction);
          event.fire();
        });

    glfwSetScrollCallback(
        this.glfwWindow,
        (window, xoffset, yoffset) -> {
          MouseScrollEvent event = new MouseScrollEvent((int) xoffset, (int) yoffset);
          event.fire();
        });

    glfwSetCursorPosCallback(
        this.glfwWindow,
        (window, xpos, ypos) -> {
          Vector2i from = new Vector2i((int) this.mousePosition.x, (int) this.mousePosition.y);
          Vector2i to = new Vector2i((int) xpos, (int) ypos);
          MouseMoveEvent event = new MouseMoveEvent(from, to);
          event.fire();
          if (event.isCanceled()) {
            glfwSetCursorPos(window, from.x, from.y);
          } else {
            this.mousePosition = new Vector2d(xpos, ypos);
          }
        });

    glfwSetKeyCallback(
        this.glfwWindow,
        (window, key, scancode, action, mods) -> {
          KeyboardEvent.KeyAction keyAction =
              switch (action) {
                case GLFW_PRESS -> KeyboardEvent.KeyAction.PRESS;
                case GLFW_RELEASE -> KeyboardEvent.KeyAction.RELEASE;
                case GLFW_REPEAT -> KeyboardEvent.KeyAction.REPEAT;
                default -> throw new IllegalStateException("Unexpected value: " + action);
              };
          KeyboardEvent event = new KeyboardEvent(key, keyAction);
          event.fire();
        });
  }

  private void _initOpenGL() {
    GL.createCapabilities();
    GL30.glClearColor(0.51f, 0.78f, 0.89f, 1f);
    GL30.glDisable(GL30.GL_DEPTH_TEST);
    GL30.glDisable(GL30.GL_CULL_FACE);
    // GL33.glEnable(GL33.GL_CULL_FACE);
    // GL33.glCullFace(GL33.GL_FRONT);

    if (GLUtils.checkVersion(4, 3)) {
      GLUtil.setupDebugMessageCallback(System.out);
    }
  }

  private void _updateLoop() {
    long lastTime = 0;
    while (!this.shouldClose) {
      long currentTime = System.nanoTime();
      float deltaTime = (currentTime - lastTime) / 1_000_000_000f;
      lastTime = currentTime;

      long start = System.nanoTime();
      this.update(deltaTime);
      long end = System.nanoTime();
      long execution = end - start;

      try {
        long sleepTime = ((1_000_000_000L / this.tickRate) - (execution)) / 1_000_000L;
        if (sleepTime > 0) {
          TimeUnit.MILLISECONDS.sleep(sleepTime);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void _renderLoop() {
    try {
      this.init();

      //Start Update Thread
      UPDATE_THREAD = new Thread(this::_updateLoop, "Update Thread");
      UPDATE_THREAD.start();

      long lastTime = System.nanoTime();
      while (!this.shouldClose) {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000f;
        lastTime = currentTime;

        long start = System.nanoTime();
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        this.render(deltaTime);
        glfwSwapBuffers(this.glfwWindow);
        glfwPollEvents();

        this.mainThreadQueue.forEach(IVoidFunction::run);
        this.mainThreadQueue.clear();

        long end = System.nanoTime();
        long execution = end - start;

        try {
          long sleepTime =
              ((1_000_000_000L / Math.max(1, this.frameRate)) - (execution)) / 1_000_000L;
          if (this.hasFocus) {
            if (sleepTime > 0 && this.frameRate > 0) {
              TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
          } else { // Reduce Frame Rate when window is not focused to max 10 FPS.
            sleepTime = ((1_000_000_000L / 10) - (execution)) / 1_000_000L;
            if (sleepTime > 0) {
              TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } finally {
      this.close();
    }
  }

  /**
   * Runs the given function on the main thread.
   *
   * <p>Attention: This method is not blocking and the function will be executed in the next frame
   * and will impact the frame performance.
   *
   * @param func the function to run on the main thread
   */
  public void runOnMainThread(IVoidFunction func) {
    this.mainThreadQueue.add(func);
  }

  /**
   * Returns the title of the game window.
   *
   * @return the title of the game window
   */
  public String title() {
    return this.title;
  }

  /**
   * Sets the title of the game window.
   *
   * @param title the new title of the game window
   * @return the game window
   */
  public GameWindow title(String title) {
    checkMainThread();
    glfwSetWindowTitle(this.glfwWindow, title);
    this.title = title;
    return this;
  }

  /**
   * Returns the size of the game window.
   *
   * @return the size of the game window
   */
  public Vector2i size() {
    return this.size;
  }

  /**
   * Sets the size of the game window.
   *
   * @param size the new size of the game window
   * @return the game window
   */
  public GameWindow size(Vector2i size) {
    checkMainThread();
    glfwSetWindowSize(this.glfwWindow, size.x, size.y);
    this.size = size;
    return this;
  }

  /**
   * Returns the position of the game window.
   *
   * @return the position of the game window
   */
  public Vector2i position() {
    return this.position;
  }

  /**
   * Sets the position of the game window.
   *
   * @param position the new position of the game window
   * @return the game window
   */
  public GameWindow position(Vector2i position) {
    checkMainThread();
    glfwSetWindowPos(this.glfwWindow, position.x, position.y);
    this.position = position;
    return this;
  }

  /**
   * Returns the debug state of the game window.
   *
   * @return the debug state of the game window
   */
  public boolean debug() {
    return this.debug;
  }

  /**
   * Sets the debug state of the game window.
   *
   * @param debug the new debug state of the game window
   * @return the game window
   */
  public GameWindow debug(boolean debug) {
    checkMainThread();
    if (!this.debug && debug) {
      glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
    } else if (this.debug && !debug) {
      glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_FALSE);
    }
    this.debug = debug;
    return this;
  }

  /**
   * Returns the visibility state of the game window.
   *
   * @return the visibility state of the game window
   */
  public boolean visible() {
    return this.visible;
  }

  /**
   * Sets the visibility state of the game window.
   *
   * @param visible the new visibility state of the game window
   * @return the game window
   */
  public GameWindow visible(boolean visible) {
    checkMainThread();
    if (this.visible && !visible) {
      glfwHideWindow(this.glfwWindow);
    } else if (!this.visible && visible) {
      glfwShowWindow(this.glfwWindow);
    }
    this.visible = visible;
    return this;
  }

  /**
   * Returns the VSync state of the game window.
   *
   * @return the VSync state of the game window
   */
  public boolean vsync() {
    return this.vsync;
  }

  /**
   * Sets the VSync state of the game window.
   *
   * @param vsync the new VSync state of the game window
   * @return the game window
   */
  public GameWindow vsync(boolean vsync) {
    checkMainThread();
    if (this.vsync && !vsync) {
      glfwSwapInterval(0);
    } else if (!this.vsync && vsync) {
      glfwSwapInterval(1);
    }
    this.vsync = vsync;
    return this;
  }

  /**
   * Returns the frame rate of the game window.
   *
   * @return the frame rate of the game window
   */
  public long frameRate() {
    return this.frameRate;
  }

  /**
   * Sets the frame rate of the game window.
   *
   * @param frameRate the new frame rate of the game window
   * @return the game window
   */
  public GameWindow frameRate(long frameRate) {
    this.frameRate = frameRate;
    return this;
  }

  /**
   * Returns the tick rate of the game window.
   *
   * @return the tick rate of the game window
   */
  public long tickRate() {
    return this.tickRate;
  }

  /**
   * Sets the tick rate of the game window.
   *
   * @param tickRate the new tick rate of the game window
   * @return the game window
   */
  public GameWindow tickRate(long tickRate) {
    this.tickRate = tickRate;
    return this;
  }

  /** Starts the game window. */
  public void start() {
    this._init();
  }

  /**
   * Returns the raw mouse input state of the game window.
   *
   * @return the raw mouse input state of the game window
   */
  public boolean rawMouseInput() {
    return this.rawMouseInput;
  }

  /**
   * Sets the raw mouse input state of the game window.
   *
   * @param rawMouseInput the new raw mouse input state of the game window
   * @return the game window
   */
  public GameWindow rawMouseInput(boolean rawMouseInput) {
    checkMainThread();
    if (this.rawMouseInput && !rawMouseInput) {
      glfwSetInputMode(this.glfwWindow, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
    } else if (!this.rawMouseInput && rawMouseInput) {
      if (glfwRawMouseMotionSupported()) {
        glfwSetInputMode(this.glfwWindow, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
      } else {
        throw new GLFWException("Raw mouse input is not supported on this system");
      }
    }
    this.rawMouseInput = rawMouseInput;
    return this;
  }

  /**
   * Returns the resizable state of the game window.
   *
   * @return the resizable state of the game window
   */
  public boolean resizable() {
    return this.resizable;
  }

  /**
   * Sets the resizable state of the game window.
   *
   * @param resizable the new resizable state of the game window
   * @return the game window
   */
  public GameWindow resizable(boolean resizable) {
    checkMainThread();
    if (this.resizable && !resizable) {
      glfwSetWindowAttrib(this.glfwWindow, GLFW_RESIZABLE, GLFW_FALSE);
    } else if (!this.resizable && resizable) {
      glfwSetWindowAttrib(this.glfwWindow, GLFW_RESIZABLE, GLFW_TRUE);
    }
    this.resizable = resizable;
    return this;
  }

  /**
   * Sets the focus to the game window. This method makes the game window the active window on the
   * user's screen.
   */
  public void focus() {
    glfwFocusWindow(this.glfwWindow);
  }

  public boolean fullscreen() {
    return this.fullscreen;
  }

  public void close() {
    glfwSetWindowShouldClose(this.glfwWindow, true);
    this.shouldClose = true;
    if (UPDATE_THREAD != null) {
      try {
        UPDATE_THREAD.join(5000);
        UPDATE_THREAD.interrupt();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public GameWindow fullscreen(boolean fullscreen) {
    checkMainThread();
    if (this.fullscreen && !fullscreen) {
      glfwSetWindowMonitor(
          this.glfwWindow, 0, this.position.x, this.position.y, this.size.x, this.size.y, 0);
    } else if (!this.fullscreen && fullscreen) {
      long monitor = glfwGetPrimaryMonitor();
      GLFWVidMode mode = glfwGetVideoMode(monitor);
      if (mode == null) return this;
      glfwSetWindowMonitor(
          this.glfwWindow, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
    }
    this.fullscreen = fullscreen;
    return this;
  }

  @Override
  public void dispose() {
    Dungine.WINDOWS.remove(this);
    this.close();
  }
}