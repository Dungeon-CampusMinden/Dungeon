package de.fwatermann.dungine.physics.colliders;

import org.joml.Vector3f;

public record Collision(Vector3f normal, float depth) {}
