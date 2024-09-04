package de.fwatermann.dungine.physics.ecs;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.simple.Lines;
import de.fwatermann.dungine.graphics.simple.Points;
import de.fwatermann.dungine.graphics.simple.WireframeBox;
import de.fwatermann.dungine.physics.colliders.PolyhedronCollider;
import de.fwatermann.dungine.utils.pair.IntPair;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A system that is used to debug physics. This system is used to display debug information about
 * physics components.
 */
public class PhysicsDebugSystem extends System<PhysicsDebugSystem> {

  private static final Logger LOGGER = LogManager.getLogger(PhysicsDebugSystem.class);

  private Camera<?> camera;
  private WireframeBox boundingBox;
  private Lines colliderLines;
  private Lines collisionLines;
  private Lines velocityLines;
  private Lines forceLines;
  private Points entityPositionPoints;
  private Points colliderVertices;
  private Points contactPoints;

  public static Points contactPointsDebug;
  public static Lines manifoldLines;

  public PhysicsDebugSystem(Camera<?> camera) {
    super(0, true, PhysicsDebugComponent.class);
    this.camera = camera;
  }

  @Override
  public void update(ECS ecs) {

    if (this.collisionLines == null) this.collisionLines = new Lines(0xFF0000FF).lineWidth(4.0f);
    if (this.velocityLines == null) this.velocityLines = new Lines(0x00FF00FF);
    if (this.forceLines == null) this.forceLines = new Lines(0xFF8000FF);
    if(this.colliderLines == null) this.colliderLines = new Lines(0x00FF00FF);
    if (this.entityPositionPoints == null)
      this.entityPositionPoints = new Points(0x8000FFFF).pointSize(8.0f);
    if (this.colliderVertices == null)
      this.colliderVertices = new Points(0x00FFFFFF).pointSize(8.0f);
    if (this.contactPoints == null) this.contactPoints = new Points(0xFF00FFFF).pointSize(8.0f);
    if(contactPointsDebug == null) contactPointsDebug = new Points(0xFFFF00FF).pointSize(8.0f);
    if(manifoldLines == null) manifoldLines = new Lines(0xFFFF00FF);
    this.collisionLines.clear();
    this.velocityLines.clear();
    this.forceLines.clear();
    this.entityPositionPoints.clear();
    this.colliderVertices.clear();
    this.contactPoints.clear();
    this.colliderLines.clear();

    ecs.entities(
        s -> {
          s.forEach(
              entity -> {
                Optional<PhysicsDebugComponent> optPDC =
                    entity.component(PhysicsDebugComponent.class);
                if (optPDC.isEmpty()) return;
                PhysicsDebugComponent pdc = optPDC.get();

                Optional<RigidBodyComponent> optRBC = entity.component(RigidBodyComponent.class);

                if (pdc.displayBoundingBox()) {
                  this.initBoundingBox(entity);
                  this.boundingBox.render(this.camera);
                }

                if (pdc.displayEntityPosition()) {
                  this.entityPositionPoints.addPoint(entity.position());
                }

                if (pdc.displayVelocity()) {
                  optRBC.ifPresent(
                      rbc -> {
                        Vector3f center =
                            entity.size().mul(0.5f, new Vector3f()).add(entity.position());
                        this.velocityLines.addLine(
                            center, center.add(rbc.velocity(), new Vector3f()));
                      });
                }

                if (pdc.displayForce()) {
                  optRBC.ifPresent(
                      rbc -> {
                        Vector3f center =
                            entity.size().mul(0.5f, new Vector3f()).add(entity.position());
                        this.forceLines.addLine(center, center.add(rbc.force(), new Vector3f()));
                      });
                }

                if (pdc.displayCollisionPairs()) {
                  pdc.collisions(
                      (stream) ->
                          stream.forEach(
                              c -> {
                                c.collisionPoints()
                                    .forEach(
                                        cp -> {
                                          this.contactPoints.addPoint(cp);
                                        });
                              }));
                }

                if (pdc.displayColliders()) {
                  optRBC.ifPresent(
                      rbc -> {
                        rbc.colliders().stream()
                            .filter(c -> c instanceof PolyhedronCollider<?>)
                            .forEach(
                                c -> {
                                  PolyhedronCollider<?> pc = (PolyhedronCollider<?>) c;
                                  Vector3f[] vertices = pc.vertices();
                                  IntPair[] edges = pc.edges();
                                  for (IntPair edge : edges) {
                                    /*this.colliderVertices.addPoint(vertices[edge.a()]);
                                    this.colliderVertices.addPoint(vertices[edge.b()]);*/
                                    this.colliderLines.addLine(
                                        vertices[edge.a()], vertices[edge.b()]);
                                  }
                                  this.colliderLines.render(this.camera);
                                  Vector3f offset =
                                      entity.rotation().transform(pc.offset(), new Vector3f());
                                  this.forceLines.addLine(
                                      new Vector3f(entity.position()),
                                      offset.add(entity.position()));
                                });
                      });
                }
              });
        },
        PhysicsDebugComponent.class);

    this.collisionLines.render(this.camera);
    this.velocityLines.render(this.camera);
    this.forceLines.render(this.camera);
    this.entityPositionPoints.render(this.camera);
    this.colliderVertices.render(this.camera);
    this.contactPoints.render(this.camera);
    contactPointsDebug.render(this.camera);
    manifoldLines.render(this.camera);
  }

  private void initBoundingBox(Entity entity) {
    if (this.boundingBox == null) {
      this.boundingBox = new WireframeBox().color(0x0000FFFF);
    }
    Optional<RigidBodyComponent> rbcOpt = entity.component(RigidBodyComponent.class);
    if (rbcOpt.isEmpty()) {
      this.boundingBox.position(entity.position()).rotation(entity.rotation()).scale(entity.size());
    } else {
      RigidBodyComponent rbc = rbcOpt.get();
      Vector3f min = new Vector3f(Float.MAX_VALUE);
      Vector3f max = new Vector3f(-Float.MAX_VALUE);
      rbc.colliders()
          .forEach(
              collider -> {
                min.min(collider.min());
                max.max(collider.max());
              });
      this.boundingBox.position(min).scaling(max.sub(min)).rotation(new Quaternionf());
    }
  }

  public Camera<?> camera() {
    return this.camera;
  }

  public PhysicsDebugSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }



}
