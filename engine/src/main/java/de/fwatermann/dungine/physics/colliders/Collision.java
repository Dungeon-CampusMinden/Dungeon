package de.fwatermann.dungine.physics.colliders;

import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

public record Collision(Vector3f normal, float depth, Set<Vector3f> collisionPoints) {

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Collision other)) return false;
    return this.normal.equals(other.normal) && this.depth == other.depth && this.collisionPoints.equals(other.collisionPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.normal, this.depth, this.collisionPoints);
  }
}
