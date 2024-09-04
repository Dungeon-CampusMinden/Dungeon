package de.fwatermann.dungine.physics.colliders;

import org.joml.Vector3f;

import java.util.Set;

public record Collision(Vector3f normal, float depth, Set<Vector3f> collisionPoints) {}
