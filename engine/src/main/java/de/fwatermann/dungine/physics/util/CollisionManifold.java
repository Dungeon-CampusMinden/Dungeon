package de.fwatermann.dungine.physics.util;

import de.fwatermann.dungine.physics.colliders.BoxCollider;
import de.fwatermann.dungine.physics.colliders.Collider;
import de.fwatermann.dungine.utils.functions.IFunction4P;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/**
 * The `CollisionManifold` class provides functionality to register and calculate collision contact points
 * between different types of colliders in the physics engine.
 */
public class CollisionManifold {

  private static final Logger LOGGER = LogManager.getLogger(CollisionManifold.class);

  private static final Map<Class<? extends Collider>, Map<Class<? extends Collider>, IFunction4P<Set<Vector3f>, Collider, Collider, Vector3f, Float>>>
      functions = new HashMap<>();

  static {
    registerCalculationFunction(BoxCollider.class, BoxCollider.class, CollisionManifoldPolyhedron::calculateContactPoints);
  }

  private CollisionManifold() {}

  /**
   * Registers a collision calculation function for the specified collider classes.
   *
   * @param c1 the first collider class
   * @param c2 the second collider class
   * @param function the function to calculate collision contact points
   */
  public static void registerCalculationFunction(Class<? extends Collider> c1, Class<? extends Collider> c2, IFunction4P<Set<Vector3f>, Collider, Collider, Vector3f, Float> function) {
    functions.computeIfAbsent(c1, k -> new HashMap<>()).put(c2, function);
  }

  /**
   * Calculates the contact points between two colliders using the registered calculation function.
   *
   * @param cA the first collider
   * @param cB the second collider
   * @param normal the collision normal
   * @param depth the collision depth
   * @return a set of contact points
   */
  public static Set<Vector3f> calculateContactPoints(Collider cA, Collider cB, Vector3f normal, float depth) {
    IFunction4P<Set<Vector3f>, Collider, Collider, Vector3f, Float> function;
    if ((function = functions.get(cA.getClass()).get(cB.getClass())) != null) {
      return function.run(cA, cB, normal, depth);
    } else if((function = functions.get(cB.getClass()).get(cA.getClass())) != null) {
      return function.run(cB, cA, normal, depth);
    }
    LOGGER.warn("No collision calculation function found for colliders {} and {}", cA.getClass(), cB.getClass());
    return Set.of();
  }

}
