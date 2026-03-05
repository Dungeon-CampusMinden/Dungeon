package blockly.dgir.compiler.java;

import com.github.javaparser.ast.Node;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface EmitResult<T> {
  @Contract("_ -> new")
  static <T> @NonNull EmitResult<T> success(T result) {
    return new Success<>(result);
  }

  @Contract(" -> new")
  static <T> @NonNull EmitResult<T> failure() {
    return new Failure<>();
  }

  @Contract("_, _, _, _ -> new")
  static <T> @NonNull EmitResult<T> failure(
      @NonNull EmitContext context, Node node, String message, Object... args) {
    context.emitError(node, message, args);
    return new Failure<>();
  }

  @Contract("_ -> new")
  static <T> @NonNull EmitResult<T> of(@NotNull T result) {
    return success(result);
  }

  static <T> @NotNull EmitResult<T> ofNullable(@Nullable T result) {
    return result == null ? failure() : success(result);
  }

  static <T> @NotNull EmitResult<T> ofNullable(
      @Nullable T result, EmitContext context, Node node, String message, Object... args) {
    return result == null ? failure(context, node, message, args) : success(result);
  }

  static <T> @NonNull EmitResult<T> ofOptional(@NotNull Optional<T> result) {
    return result.map(EmitResult::success).orElseGet(EmitResult::failure);
  }

  static <T> @NonNull EmitResult<T> ofOptional(
      @NotNull Optional<T> result, EmitContext context, Node node, String message, Object... args) {
    return result.map(EmitResult::success).orElseGet(() -> failure(context, node, message, args));
  }

  boolean isSuccess();

  boolean isFailure();

  @NotNull
  T get();

  default <U> @NotNull EmitResult<U> map(@NotNull Function<? super T, ? extends U> mapper) {
    if (isFailure()) {
      return failure();
    } else {
      return EmitResult.ofNullable(mapper.apply(get()));
    }
  }

  default <U> @NotNull EmitResult<U> flatMap(
      Function<? super T, ? extends EmitResult<? extends U>> mapper) {
    Objects.requireNonNull(mapper);
    if (isFailure()) {
      return failure();
    } else {
      @SuppressWarnings("unchecked")
      EmitResult<U> r = (EmitResult<U>) mapper.apply(get());
      return Objects.requireNonNull(r);
    }
  }

  default @NotNull EmitResult<T> or(@NotNull Supplier<? extends EmitResult<? extends T>> supplier) {
    if (isSuccess()) {
      return this;
    } else {
      @SuppressWarnings("unchecked")
      EmitResult<T> r = (EmitResult<T>) supplier.get();
      return Objects.requireNonNull(r);
    }
  }

  default @NotNull Stream<T> stream() {
    if (isFailure()) {
      return Stream.empty();
    } else {
      return Stream.of(get());
    }
  }

  default @Nullable T orElse(@Nullable T other) {
    return isSuccess() ? get() : other;
  }

  default @Nullable T orElseGet(@NotNull Supplier<? extends T> supplier) {
    return isSuccess() ? get() : supplier.get();
  }

  default @NotNull Optional<T> toOptional() {
    return isSuccess() ? Optional.of(get()) : Optional.empty();
  }

  record Success<T>(@NotNull T result) implements EmitResult<T> {
    @Contract(pure = true)
    @Override
    public boolean isSuccess() {
      return true;
    }

    @Contract(pure = true)
    @Override
    public boolean isFailure() {
      return false;
    }

    @Contract(pure = true)
    @Override
    public @NonNull T get() {
      return result;
    }
  }

  record Failure<T>() implements EmitResult<T> {
    @Contract(pure = true)
    @Override
    public boolean isSuccess() {
      return false;
    }

    @Contract(pure = true)
    @Override
    public boolean isFailure() {
      return true;
    }

    @Contract(pure = true)
    @Override
    public @NonNull T get() {
      throw new NoSuchElementException("Cannot get result from a failure.");
    }
  }
}
