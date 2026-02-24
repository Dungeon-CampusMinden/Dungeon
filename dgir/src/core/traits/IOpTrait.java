package core.traits;

import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface that all op-traits must extend.
 *
 * <p>Every trait is a Java interface that extends {@code IOpTrait}. A trait can provide:
 *
 * <ul>
 *   <li>a {@code default boolean verify(@NotNull SelfTrait ignored)} method that is called during
 *       verification (see {@link core.detail.OperationDetails#verifyTraits});
 *   <li>convenience accessor default methods backed by {@link #getOperation()}.
 * </ul>
 *
 * <p>Op classes pick up traits by simply implementing the desired trait interfaces. The
 * {@link core.detail.OperationDetails.Registered} introspects declared interfaces at registration
 * time and stores both the trait set and the verifier methods.
 */
public interface IOpTrait {

  /**
   * Returns the backing {@link Operation} of the op that implements this trait.
   *
   * @return the operation, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull Operation getOperation();
}
