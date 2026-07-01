package contrib.entities;

import core.Entity;
import core.utils.Point;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UseMainSkillTest {
  @Mock
  private Entity mockHero;

  @Mock
  private SkillComponent mockSkillComponent;

  @Mock
  private CursorSkill mockCursorSkill;

  @Mock
  private ProjectileSkill mockProjectileSkill;

  @Mock
  private Skill mockGenericSkill;

  @Captor
  private ArgumentCaptor<Supplier<Point>> supplierCaptor;

  private Point validTarget;

  private AutoCloseable mocks;


  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    validTarget = new Point(10, 20);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void useMainSkill_WithCursorSkillAndValidTarget_SetsCursorPositionAndExecutes() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.of(mockCursorSkill));

    // Act
    HeroController.useMainSkill(mockHero, validTarget);

    // Assert
    verify(mockCursorSkill).cursorPositionSupplier(supplierCaptor.capture());
    assertEquals(validTarget, supplierCaptor.getValue().get(),
      "Cursor skill should set the target position to the provided valid target point");
  }

  @Test
  void useMainSkill_WithProjectileSkillAndValidTarget_SetsEndpointAndExecutes() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.of(mockProjectileSkill));

    // Act
    HeroController.useMainSkill(mockHero, validTarget);

    // Assert
    verify(mockProjectileSkill).endPointSupplier(supplierCaptor.capture());
    assertEquals(validTarget, supplierCaptor.getValue().get(),
      "Projectile skill should set the endpoint to the provided valid target point");
  }

  @Test
  void useMainSkill_WithGenericSkillType_ExecutesWithoutTargetSetting() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.of(mockGenericSkill));

    // Act
    HeroController.useMainSkill(mockHero, validTarget);

    // Assert
    verify(mockGenericSkill, times(1)).execute(mockHero);
  }

  @Test
  void useMainSkill_WithNoActiveSkill_ExecutesNothing() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.empty());

    // Act
    HeroController.useMainSkill(mockHero, validTarget);

    // Assert
    verify(mockGenericSkill, never()).execute(any());
  }

  @Test
  void useMainSkill_WithoutSkillComponent_ExecutesNothing() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.empty());

    // Act
    HeroController.useMainSkill(mockHero, validTarget);

    // Assert
    verify(mockGenericSkill, never()).execute(any());
  }


  @Test
  void useMainSkill_WithNullHero_ThrowsNullPointerException() {
    // Arrange
    Entity nullHero = null;

    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
      HeroController.useMainSkill(nullHero, validTarget);
    }, "NullPointerException should be thrown when hero parameter is null");
  }

  @Test
  void useMainSkill_WithCursorSkillAndNullTarget_SetsNullSupplier() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.of(mockCursorSkill));

    // Act
    HeroController.useMainSkill(mockHero, null);

    // Assert
    verify(mockCursorSkill).cursorPositionSupplier(supplierCaptor.capture());
    assertNull(supplierCaptor.getValue().get(),
      "Cursor skill supplier should return null when null target is provided");
    verify(mockCursorSkill, times(1)).execute(mockHero);
  }

  @Test
  void useMainSkill_WithProjectileSkillAndNullTarget_SetsNullSupplier() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class))
      .thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeMainSkill())
      .thenReturn(Optional.of(mockProjectileSkill));

    // Act
    HeroController.useMainSkill(mockHero, null);

    // Assert
    verify(mockProjectileSkill).endPointSupplier(supplierCaptor.capture());
    assertNull(supplierCaptor.getValue().get(),
      "Projectile skill supplier should return null when null target is provided");
    verify(mockProjectileSkill, times(1)).execute(mockHero);
  }
}
