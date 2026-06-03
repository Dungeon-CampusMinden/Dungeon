package contrib.modules.puzzle;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Splits a rectangular image into {@code pieceCount} polygonal pieces with jagged cut edges.
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
 *         <li>Build a jagged chord between P1 and P2: place 3 intermediate points evenly along the
 *             straight chord and offset each one orthogonally to the chord. The first intermediate
 *             point is offset to a random side, and the remaining two alternate sides. Both
 *             resulting sub-polygons share the exact same jagged edge (in reverse order) so the
 *             pieces still tile perfectly.
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

  /**
   * Base orthogonal offset of the 3 jagged intermediate points, expressed as a fraction of the
   * chord length. Kept small so the jagged edge stays well inside the parent polygon and does not
   * self-intersect with other edges.
   */
  private static final float JAGGED_OFFSET_FRACTION = 0.06f;

  /** Maximum extra jitter (as fraction of chord length) added to / subtracted from each offset. */
  private static final float JAGGED_OFFSET_JITTER = 0.03f;

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
    polygons.add(new float[] {0f, 0f, width, 0f, width, height, 0f, height});

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

      float[] jagged = makeJaggedPoints(h1, h2, rng);
      float[][] result = splitAt(poly, h1, h2, jagged);
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

  /**
   * Splits the polygon along its longer axis through the centroid.
   *
   * @param poly the polygon to split
   * @return the two resulting polygons
   */
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
    float[][] r = splitAt(poly, h1, h2, null);
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
   * edges. If {@code jagged} is non-null, it must contain 3 intermediate points along the cut (6
   * floats: {@code x25,y25,x50,y50,x75,y75}). These points are inserted into both resulting
   * polygons in opposite traversal order so that the two pieces share the exact same jagged seam.
   *
   * @param poly the polygon to split
   * @param a the first boundary hit
   * @param b the second boundary hit
   * @param jagged optional intermediate cut points (6 floats), or {@code null} for a straight cut
   * @return the two resulting polygons
   */
  private static float[][] splitAt(float[] poly, Hit a, Hit b, float[] jagged) {
    int n = poly.length / 2;
    Hit h1 = a, h2 = b;
    boolean swapped = false;
    if (h1.edgeIndex > h2.edgeIndex) {
      Hit tmp = h1;
      h1 = h2;
      h2 = tmp;
      swapped = true;
    }

    // Polygon A: h1.point, vertices (h1.edgeIndex+1) .. h2.edgeIndex (inclusive), h2.point,
    // then jagged points from h2 back to h1 (i.e. reversed cut direction).
    List<Vector2> a0 = new ArrayList<>();
    a0.add(new Vector2(h1.x, h1.y));
    for (int i = h1.edgeIndex + 1; i <= h2.edgeIndex; i++) {
      a0.add(new Vector2(poly[2 * i], poly[2 * i + 1]));
    }
    a0.add(new Vector2(h2.x, h2.y));
    if (jagged != null) {
      // The "cut direction" used to compute jagged points goes from `a` to `b`; we possibly
      // swapped a and b above. Determine the order of jagged points along h1 -> h2 in the
      // ORIGINAL (a -> b) direction and account for the swap.
      // jagged is laid out a -> b: (J25, J50, J75). After swap it represents h2 -> h1.
      // For polygon A (closing edge h2 -> h1) we therefore want jagged in its stored order
      // when swapped, and in reverse order when not swapped.
      if (swapped) {
        a0.add(new Vector2(jagged[0], jagged[1]));
        a0.add(new Vector2(jagged[2], jagged[3]));
        a0.add(new Vector2(jagged[4], jagged[5]));
      } else {
        a0.add(new Vector2(jagged[4], jagged[5]));
        a0.add(new Vector2(jagged[2], jagged[3]));
        a0.add(new Vector2(jagged[0], jagged[1]));
      }
    }

    // Polygon B: h2.point, vertices (h2.edgeIndex+1) .. (h1.edgeIndex) [wrapping], h1.point,
    // then jagged points from h1 back to h2.
    List<Vector2> b0 = new ArrayList<>();
    b0.add(new Vector2(h2.x, h2.y));
    int i = (h2.edgeIndex + 1) % n;
    int stopExclusive = (h1.edgeIndex + 1) % n;
    while (i != stopExclusive) {
      b0.add(new Vector2(poly[2 * i], poly[2 * i + 1]));
      i = (i + 1) % n;
    }
    b0.add(new Vector2(h1.x, h1.y));
    if (jagged != null) {
      // Mirror logic of polygon A: closing edge here is h1 -> h2.
      if (swapped) {
        b0.add(new Vector2(jagged[4], jagged[5]));
        b0.add(new Vector2(jagged[2], jagged[3]));
        b0.add(new Vector2(jagged[0], jagged[1]));
      } else {
        b0.add(new Vector2(jagged[0], jagged[1]));
        b0.add(new Vector2(jagged[2], jagged[3]));
        b0.add(new Vector2(jagged[4], jagged[5]));
      }
    }

    return new float[][] {toArray(a0), toArray(b0)};
  }

  /**
   * Computes 3 jagged intermediate points along the straight chord {@code a -> b}. They are placed
   * at parameter values {@code 0.25}, {@code 0.50}, {@code 0.75} on the chord and offset
   * orthogonally to the chord. The first offset is randomly to one of the two sides; the remaining
   * two alternate sides. Each offset magnitude has a small random jitter so the resulting tear
   * looks natural rather than mechanical.
   *
   * @param a the start boundary hit of the chord
   * @param b the end boundary hit of the chord
   * @param rng the random source used to jitter the offsets
   * @return flat array {@code {x25, y25, x50, y50, x75, y75}}
   */
  private static float[] makeJaggedPoints(Hit a, Hit b, Random rng) {
    float dx = b.x - a.x;
    float dy = b.y - a.y;
    float len = (float) Math.sqrt(dx * dx + dy * dy);
    if (len <= 0f) {
      return new float[] {a.x, a.y, a.x, a.y, a.x, a.y};
    }
    // Unit chord direction and orthogonal (90 deg CCW in image-space).
    float ux = dx / len;
    float uy = dy / len;
    float ox = -uy;
    float oy = ux;

    // Random side for the first jagged point; subsequent ones alternate.
    float sign = rng.nextBoolean() ? 1f : -1f;

    float[] out = new float[6];
    float[] params = {0.25f, 0.50f, 0.75f};
    for (int k = 0; k < 3; k++) {
      float t = params[k];
      float bx = a.x + dx * t;
      float by = a.y + dy * t;
      float jitter = (rng.nextFloat() - 0.5f) * 2f * JAGGED_OFFSET_JITTER;
      float magnitude = (JAGGED_OFFSET_FRACTION + jitter) * len;
      if (magnitude < 0f) magnitude = 0f;
      float off = sign * magnitude;
      out[2 * k] = bx + ox * off;
      out[2 * k + 1] = by + oy * off;
      sign = -sign;
    }
    return out;
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
   * Computes the integer bounding box of a polygon.
   *
   * @param poly the polygon
   * @param clampW the maximum width to clamp the bounding box to
   * @param clampH the maximum height to clamp the bounding box to
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
