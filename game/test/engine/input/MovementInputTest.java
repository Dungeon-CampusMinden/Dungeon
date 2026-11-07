package engine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import engine.Entity;
import engine.components.VelocityComponent;
import engine.utils.Vector2;
import feature.entities.HeroController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the movement vector from keys and stick, and how the hero turns it into force. */
class MovementInputTest {
  private static final Binding STICK = Binding.pad(Binding.PadAxisId.LSTICK);
  private static final Vector2 SPEED = Vector2.of(5, 5);

  private InputResolver resolver;

  @BeforeEach
  void setUp() {
    GameInput.reset();
    resolver = GameInput.resolver();
    resolver.activate(new InputContext(CoreActions.GAMEPLAY));
  }

  @AfterEach
  void tearDown() {
    GameInput.reset();
  }

  @Test
  void keysGiveNormalizedVector() {
    resolver.keyDown(Input.Keys.W);
    resolver.keyDown(Input.Keys.D);
    resolver.advance(0);

    assertEquals(1.0, CoreActions.move().length(), 1e-4);
    assertEquals(CoreActions.move().x(), CoreActions.move().y(), 1e-4);
  }

  @Test
  void oppositeKeysCancelOut() {
    resolver.keyDown(Input.Keys.A);
    resolver.keyDown(Input.Keys.D);
    resolver.advance(0);

    assertTrue(CoreActions.move().isZero());
  }

  @Test
  void stickKeepsItsAnalogValue() {
    resolver.padAxis(STICK, 0.3f, 0.4f);

    assertEquals(0.5, CoreActions.move().length(), 1e-4);
  }

  @Test
  void keysAndStickTogetherStayWithinLengthOne() {
    resolver.keyDown(Input.Keys.D);
    resolver.advance(0);
    resolver.padAxis(STICK, 0.8f, 0f);

    assertEquals(1.0, CoreActions.move().length(), 1e-4);
  }

  @Test
  void deadZoneIsRadialAndStretchesTheRest() {
    assertTrue(PadInput.radialDeadzone(0.1f, 0.1f).isZero());
    assertEquals(0.5, PadInput.radialDeadzone(0.6f, 0f).length(), 1e-4);
    assertEquals(1.0, PadInput.radialDeadzone(0.9f, 0.9f).length(), 1e-4);
  }

  private static double forceLength(Vector2 direction) {
    Entity hero = new Entity("hero");
    VelocityComponent velocity = new VelocityComponent(5f);
    hero.add(velocity);
    HeroController.moveHero(hero, direction, SPEED);
    return velocity.force(HeroController.MOVEMENT_ID).map(Vector2::length).orElse(0.0);
  }

  @Test
  void straightAndDiagonalMoveEquallyFast() {
    assertEquals(5.0, forceLength(Vector2.of(0, 1)), 1e-4);
    assertEquals(5.0, forceLength(Vector2.of(0.70710677, 0.70710677)), 1e-3);
  }

  @Test
  void halfTiltedStickAsksForHalfTheForce() {
    assertEquals(2.5, forceLength(Vector2.of(0.5, 0)), 1e-4);
  }

  @Test
  void serverLimitsDirectionToLengthOneAndIgnoresNonFiniteValues() {
    assertEquals(5.0, forceLength(Vector2.of(30, 40)), 1e-4);
    assertEquals(5.0, forceLength(Vector2.of(Float.NaN, 1)), 1e-4);
  }
}
