package contrib.modules.puzzle;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Splits a rectangular image into {@code pieceCount} convex polygonal pieces.
 *
 * <p>Algorithm:
 *
 * <ol>
 *   <li>Start with a single polygon = the full image rectangle.
 *   <li>Repeat {@code pieceCount - 1} times:
 *       <ul>
 *         <li>Pick the polygon with the largest area.
 *         <li>Pick a random point P1 on its perimeter (parameter {@code t1} in {@code [0, 1)}).
 *         <li>Pick a roughly opposite point P2 at perimeter parameter {@code t1 + 0.5 + jitter}.
 *         <li>Split the polygon along the chord {@code P1-P2} into two convex sub-polygons.
 *       </ul>
 * </ol>
 *
 * <p>Polygon vertices are stored in image-pixel coordinates ({@code x in [0, width]}, {@code y in
 * [0, height]} with Y increasing downward, matching image rows).
 */
final class PuzzleSlicer {

  /** Maximum jitter added to the "opposite" perimeter parameter (in units of full perimeter). */
  private static final float OPPOSITE_JITTER = 0.30f;

  /** Maximum attempts at picking a non-degenerate split before giving up on this iteration. */
  private static final int MAX_SPLIT_ATTEMPTS = 32;

  /** Minimum spacing (as fraction of perimeter) between the two split points. */
  private static final float MIN_SPLIT_GAP = 0.15f;

  /**
   * Maximum allowed area imbalance between the two halves of a split, expressed as {@code (larger -
   * smaller) / larger}. Splits that exceed this ratio are rejected and retried with new random
   * points (within the same {@code Random}, so determinism is preserved).
   */
  private static final float MAX_AREA_RATIO_DIFF = 0.30f;

  private PuzzleSlicer() {}

  /**
   * Slices the rectangle {@code [0, width] x [0, height]} into {@code pieceCount} convex polygons.
   *
   * @param width image width in pixels (must be {@code > 0})
   * @param height image height in pixels (must be {@code > 0})
   * @param pieceCount total number of pieces (must be {@code >= 2})
   * @param seed RNG seed for deterministic slicing
   * @return list of polygons; each polygon is a {@code float[]} of vertices in {@code
   *     {x0,y0,x1,y1,...}} order
   */
  static List<float[]> slice(int width, int height, int pieceCount, long seed) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    if (pieceCount < 2) {
      throw new IllegalArgumentException("pieceCount must be >= 2");
    }

    Random rng = new Random(seed);

    List<float[]> polygons = new ArrayList<>();
    polygons.add(
        new float[] {
          0f, 0f,
          width, 0f,
          width, height,
          0f, height
        });

    while (polygons.size() < pieceCount) {
      int idx = indexOfLargest(polygons);
      float[] poly = polygons.get(idx);

      float[][] split = trySplit(poly, rng);
      if (split == null) {
        // Could not find a non-degenerate split for the largest polygon; try the next-largest.
        // To avoid infinite loops, fall back to a guaranteed axis-aligned bisection.
        split = forcedBisect(poly);
      }

      polygons.remove(idx);
      polygons.add(split[0]);
      polygons.add(split[1]);
    }

    return polygons;
  }

  // ---------------------------------------------------------------------------
  // Splitting
  // ---------------------------------------------------------------------------

  private static float[][] trySplit(float[] poly, Random rng) {
    float perimeter = perimeter(poly);
    if (perimeter <= 0f) return null;

    for (int attempt = 0; attempt < MAX_SPLIT_ATTEMPTS; attempt++) {
      float t1 = rng.nextFloat();
      float jitter = (rng.nextFloat() - 0.5f) * 2f * OPPOSITE_JITTER;
      float t2 = (t1 + 0.5f + jitter) % 1f;
      if (t2 < 0f) t2 += 1f;

      float gap = Math.abs(t1 - t2);
      gap = Math.min(gap, 1f - gap);
      if (gap < MIN_SPLIT_GAP) continue;

      Hit h1 = pointOnPerimeter(poly, t1 * perimeter);
      Hit h2 = pointOnPerimeter(poly, t2 * perimeter);
      if (h1.edgeIndex == h2.edgeIndex) continue; // chord would lie on a single edge

      float[][] result = splitAt(poly, h1, h2);
      if (result == null) continue;
      if (result[0].length < 6 || result[1].length < 6) continue; // need at least a triangle

      // Reject overly unbalanced splits. Determinism is preserved because we keep consuming
      // values from the same `rng` until an acceptable split is found.
      float a0 = area(result[0]);
      float a1 = area(result[1]);
      float larger = Math.max(a0, a1);
      float smaller = Math.min(a0, a1);
      if (larger > 0f && (larger - smaller) / larger > MAX_AREA_RATIO_DIFF) continue;

      return result;
    }
    return null;
  }

  /** Splits the polygon along its longer axis through the centroid. */
  private static float[][] forcedBisect(float[] poly) {
    float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
    float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < poly.length; i += 2) {
      minX = Math.min(minX, poly[i]);
      maxX = Math.max(maxX, poly[i]);
      minY = Math.min(minY, poly[i + 1]);
      maxY = Math.max(maxY, poly[i + 1]);
    }
    float perimeter = perimeter(poly);
    boolean horizontal = (maxX - minX) >= (maxY - minY);
    // Find any two perimeter points lying on a line through the bounding-box center,
    // perpendicular to the longer axis.
    // Simpler: pick t = 0.0 (somewhere on edge 0) and t = 0.5 across the polygon.
    Hit h1 = pointOnPerimeter(poly, perimeter * 0.25f);
    Hit h2 = pointOnPerimeter(poly, perimeter * 0.75f);
    float[][] r = splitAt(poly, h1, h2);
    if (r != null) return r;
    // Last resort: split the rectangle by midline.
    if (horizontal) {
      float mx = (minX + maxX) * 0.5f;
      return new float[][] {
        new float[] {minX, minY, mx, minY, mx, maxY, minX, maxY},
        new float[] {mx, minY, maxX, minY, maxX, maxY, mx, maxY}
      };
    } else {
      float my = (minY + maxY) * 0.5f;
      return new float[][] {
        new float[] {minX, minY, maxX, minY, maxX, my, minX, my},
        new float[] {minX, my, maxX, my, maxX, maxY, minX, maxY}
      };
    }
  }

  /**
   * Splits {@code poly} along the chord between two boundary hits. Both hits must lie on different
   * edges.
   */
  private static float[][] splitAt(float[] poly, Hit a, Hit b) {
    int n = poly.length / 2;
    Hit h1 = a, h2 = b;
    if (h1.edgeIndex > h2.edgeIndex) {
      Hit tmp = h1;
      h1 = h2;
      h2 = tmp;
    }

    // Polygon A: h1.point, vertices (h1.edgeIndex+1) .. h2.edgeIndex (inclusive), h2.point
    List<Vector2> a0 = new ArrayList<>();
    a0.add(new Vector2(h1.x, h1.y));
    for (int i = h1.edgeIndex + 1; i <= h2.edgeIndex; i++) {
      a0.add(new Vector2(poly[2 * i], poly[2 * i + 1]));
    }
    a0.add(new Vector2(h2.x, h2.y));

    // Polygon B: h2.point, vertices (h2.edgeIndex+1) .. (h1.edgeIndex) [wrapping], h1.point
    List<Vector2> b0 = new ArrayList<>();
    b0.add(new Vector2(h2.x, h2.y));
    int i = (h2.edgeIndex + 1) % n;
    int stopExclusive = (h1.edgeIndex + 1) % n;
    while (i != stopExclusive) {
      b0.add(new Vector2(poly[2 * i], poly[2 * i + 1]));
      i = (i + 1) % n;
    }
    b0.add(new Vector2(h1.x, h1.y));

    return new float[][] {toArray(a0), toArray(b0)};
  }

  private static float[] toArray(List<Vector2> pts) {
    float[] out = new float[pts.size() * 2];
    for (int i = 0; i < pts.size(); i++) {
      out[2 * i] = pts.get(i).x;
      out[2 * i + 1] = pts.get(i).y;
    }
    return out;
  }

  // ---------------------------------------------------------------------------
  // Geometry helpers
  // ---------------------------------------------------------------------------

  /** Result of locating a point on the polygon perimeter. */
  private static final class Hit {
    final float x;
    final float y;
    final int edgeIndex;

    Hit(float x, float y, int edgeIndex) {
      this.x = x;
      this.y = y;
      this.edgeIndex = edgeIndex;
    }
  }

  private static Hit pointOnPerimeter(float[] poly, float distance) {
    int n = poly.length / 2;
    float remaining = distance;
    for (int i = 0; i < n; i++) {
      float ax = poly[2 * i], ay = poly[2 * i + 1];
      float bx = poly[2 * ((i + 1) % n)], by = poly[2 * ((i + 1) % n) + 1];
      float dx = bx - ax, dy = by - ay;
      float len = (float) Math.sqrt(dx * dx + dy * dy);
      if (remaining <= len || i == n - 1) {
        float t = len > 0f ? Math.min(1f, Math.max(0f, remaining / len)) : 0f;
        // Avoid landing exactly on a vertex (which would make the chord lie on an edge).
        t = Math.min(0.999f, Math.max(0.001f, t));
        return new Hit(ax + dx * t, ay + dy * t, i);
      }
      remaining -= len;
    }
    return new Hit(poly[0], poly[1], 0);
  }

  private static float perimeter(float[] poly) {
    int n = poly.length / 2;
    float p = 0f;
    for (int i = 0; i < n; i++) {
      float ax = poly[2 * i], ay = poly[2 * i + 1];
      float bx = poly[2 * ((i + 1) % n)], by = poly[2 * ((i + 1) % n) + 1];
      float dx = bx - ax, dy = by - ay;
      p += (float) Math.sqrt(dx * dx + dy * dy);
    }
    return p;
  }

  static float area(float[] poly) {
    int n = poly.length / 2;
    float s = 0f;
    for (int i = 0; i < n; i++) {
      float ax = poly[2 * i], ay = poly[2 * i + 1];
      float bx = poly[2 * ((i + 1) % n)], by = poly[2 * ((i + 1) % n) + 1];
      s += ax * by - bx * ay;
    }
    return Math.abs(s) * 0.5f;
  }

  private static int indexOfLargest(List<float[]> polygons) {
    int best = 0;
    float bestArea = -1f;
    for (int i = 0; i < polygons.size(); i++) {
      float a = area(polygons.get(i));
      if (a > bestArea) {
        bestArea = a;
        best = i;
      }
    }
    return best;
  }

  /**
   * Standard ray-casting point-in-polygon test. Works for any simple polygon (the convex pieces
   * produced by {@link #slice} are a subset).
   *
   * @param poly polygon vertices as {@code {x0,y0,x1,y1,...}}
   * @param px point x
   * @param py point y
   * @return whether {@code (px, py)} lies inside the polygon
   */
  static boolean contains(float[] poly, float px, float py) {
    int n = poly.length / 2;
    boolean inside = false;
    for (int i = 0, j = n - 1; i < n; j = i++) {
      float xi = poly[2 * i], yi = poly[2 * i + 1];
      float xj = poly[2 * j], yj = poly[2 * j + 1];
      boolean intersect =
          ((yi > py) != (yj > py))
              && (px < (xj - xi) * (py - yi) / ((yj - yi) == 0f ? 1e-9f : (yj - yi)) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }

  /**
   * @return integer bounding box {@code {minX, minY, maxX, maxY}} rounded outward
   */
  static int[] boundingBox(float[] poly, int clampW, int clampH) {
    float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
    float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < poly.length; i += 2) {
      minX = Math.min(minX, poly[i]);
      maxX = Math.max(maxX, poly[i]);
      minY = Math.min(minY, poly[i + 1]);
      maxY = Math.max(maxY, poly[i + 1]);
    }
    int x0 = Math.max(0, (int) Math.floor(minX));
    int y0 = Math.max(0, (int) Math.floor(minY));
    int x1 = Math.min(clampW, (int) Math.ceil(maxX));
    int y1 = Math.min(clampH, (int) Math.ceil(maxY));
    return new int[] {x0, y0, x1, y1};
  }
}



