package core.utils.components.draw;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.*;

/**
 * A pool manager for FrameBuffer objects (FBOs) to optimize VRAM usage by reusing FBOs of exact
 * sizes. This pool maintains a soft and hard limit on the number of FBOs, culling unused ones over
 * time.
 */
public class FrameBufferPool implements Disposable {

  // --- Singleton Instance ---
  private static FrameBufferPool instance;

  // --- Configuration ---
  private static final int SOFT_LIMIT = 50;
  private static final int HARD_LIMIT = 100;
  private static final long CULL_TIMEOUT_MS = 5000;

  // --- Internal State ---
  private int currentFboCount = 0;

  // Map: Key = Exact size (W x H), Value = List of available FBOs (with last used time)
  private final Map<SizeKey, LinkedList<PooledFbo>> availablePool;

  // Set to track FBOs currently in use by entities (to prevent double-freeing)
  private final Set<FrameBuffer> inUseFbos;

  // Timer to control how often culling runs (for performance)
  private long lastCullTime = 0;

  // Wrapper for FBOs in the pool to track metadata
  private static class PooledFbo {
    final FrameBuffer fbo;
    long lastUsedTime;

    PooledFbo(FrameBuffer fbo) {
      this.fbo = fbo;
      this.lastUsedTime = TimeUtils.millis();
    }
  }

  // Key class for FBO sizes.
  private record SizeKey(int width, int height) {}

  // --- Creation ---

  /**
   * Retrieves the singleton instance of the FrameBufferPool.
   *
   * @return The single instance of FrameBufferPool.
   */
  public static FrameBufferPool getInstance() {
    if (instance == null) {
      instance = new FrameBufferPool();
    }
    return instance;
  }

  /** Constructs a new FrameBufferPool, initializing internal maps. */
  public FrameBufferPool() {
    availablePool = new HashMap<>();
    inUseFbos = new HashSet<>();
  }

  // --- Public Methods ---

  /**
   * Retrieves an FBO of the exact specified size from the pool or creates a new one.
   *
   * @param width The required width of the FrameBuffer.
   * @param height The required height of the FrameBuffer.
   * @return An available or newly created FrameBuffer.
   * @throws IllegalStateException if the HARD_LIMIT has been reached and a new FBO is needed.
   */
  public FrameBuffer obtain(int width, int height) {
    SizeKey key = new SizeKey(width, height);
    LinkedList<PooledFbo> fbos = availablePool.get(key);

    // 1. Check for available FBOs of the EXACT size
    if (fbos != null && !fbos.isEmpty()) {
      PooledFbo pooledFbo = fbos.removeFirst();
      inUseFbos.add(pooledFbo.fbo);
      return pooledFbo.fbo;
    }

    // 2. Check HARD_LIMIT before creating new FBO
    if (currentFboCount >= HARD_LIMIT) {
      throw new IllegalStateException(
          "FBO Pool HARD_LIMIT (" + HARD_LIMIT + ") reached. Cannot allocate new FBOs.");
    }

    // 3. Create a new FBO
    FrameBuffer newFbo = new FrameBuffer(Format.RGBA8888, width, height, false);
    currentFboCount++;
    inUseFbos.add(newFbo);
    // System.out.println("New FBO created: " + width + "x" + height + ". Total: " +
    // currentFboCount);
    return newFbo;
  }

  /**
   * Returns an FBO to the available pool, updating its last used time.
   *
   * @param fbo The FrameBuffer to return to the pool.
   */
  public void free(FrameBuffer fbo) {
    if (fbo == null) return;

    if (!inUseFbos.remove(fbo)) {
      // Log error: Trying to free an FBO not currently tracked as 'in use'.
      // This might indicate a double-free or a managed resource being freed incorrectly.
      System.err.println("Warning: Attempted to free an FBO not marked as 'in use'.");
      return;
    }

    SizeKey key = new SizeKey(fbo.getWidth(), fbo.getHeight());

    // Create a new PooledFbo wrapper for tracking time
    PooledFbo pooledFbo = new PooledFbo(fbo);
    pooledFbo.lastUsedTime = TimeUtils.millis();

    availablePool.computeIfAbsent(key, k -> new LinkedList<>()).add(pooledFbo);
  }

  /** Runs once per frame (or periodically) to cull excess FBOs down to the SOFT_LIMIT. */
  public void update() {
    // Culling logic only runs periodically (e.g., every 1 second)
    if (TimeUtils.timeSinceMillis(lastCullTime) < 1000) {
      return;
    }
    lastCullTime = TimeUtils.millis();

    if (currentFboCount <= SOFT_LIMIT) {
      return;
    }

    // Iterate over all size buckets in the pool
    for (LinkedList<PooledFbo> fbos : availablePool.values()) {
      // Iterate backwards so we can safely remove elements
      for (Iterator<PooledFbo> fboIt = fbos.iterator(); fboIt.hasNext(); ) {
        PooledFbo pooledFbo = fboIt.next();

        // Check two conditions: Time limit passed AND we are over the soft limit
        if (TimeUtils.timeSinceMillis(pooledFbo.lastUsedTime) > CULL_TIMEOUT_MS
            && currentFboCount > SOFT_LIMIT) {

          // Dispose and clean up the VRAM resource
          pooledFbo.fbo.dispose();
          fboIt.remove();
          currentFboCount--;
        }

        if (currentFboCount <= SOFT_LIMIT) {
          return;
        }
      }
    }
  }

  /** Disposes all FBOs (in use and available) when the pool is destroyed. */
  @Override
  public void dispose() {
    for (LinkedList<PooledFbo> list : availablePool.values()) {
      for (PooledFbo pooledFbo : list) {
        pooledFbo.fbo.dispose();
      }
    }
    for (FrameBuffer fbo : inUseFbos) {
      fbo.dispose();
    }
    availablePool.clear();
    inUseFbos.clear();
    currentFboCount = 0;
  }
}
