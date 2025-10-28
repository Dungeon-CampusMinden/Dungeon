package core.network.handler;

import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import java.util.Random;
import java.util.concurrent.*;

/**
 * A network handler that simulates slow network conditions by introducing artificial latency and
 * packet loss. Extends the {@link NettyNetworkHandler} to override the send method.
 */
public class SlowNettyNetworkHandler extends NettyNetworkHandler {
  public static final float CLIENT_PACKAGE_DROP_RATE = 0.0f; // drop rate (0.0 - 1.0)
  public static final float SERVER_PACKAGE_DROP_RATE = 0.0f; // drop rate (0.0 - 1.0)

  public static final long NETWORK_LATENCY_MS = 100; // ms of latency added to each packet

  private static final Random RANDOM = new Random();

  private static final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
          });

  @Override
  public CompletableFuture<Boolean> send(short clientId, NetworkMessage message, boolean reliable) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    scheduler.schedule(
        () -> {
          float dropRate = isServer() ? SERVER_PACKAGE_DROP_RATE : CLIENT_PACKAGE_DROP_RATE;
          if (RANDOM.nextFloat() < dropRate) {
            // Simulate packet loss
            if (reliable) {
              // Retry to stack latency
              this.send(clientId, message, true).thenAccept(future::complete);
            } else {
              future.complete(false);
            }
          } else {
            super.send(clientId, message, reliable).thenAccept(future::complete);
          }
        },
        NETWORK_LATENCY_MS,
        TimeUnit.MILLISECONDS);
    return future;
  }

  @Override
  public void sendInput(InputMessage input) {
    scheduler.schedule(
        () -> {
          if (RANDOM.nextFloat() < CLIENT_PACKAGE_DROP_RATE) {
            // Simulate packet loss; assume TCP for inputs -> retry to stack latency
            this.sendInput(input);
            return;
          }
          super.sendInput(input);
        },
        NETWORK_LATENCY_MS,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void broadcast(NetworkMessage message, boolean reliable) {
    scheduler.schedule(
        () -> {
          if (RANDOM.nextFloat() < SERVER_PACKAGE_DROP_RATE) {
            // Simulate packet loss
            if (reliable) {
              // Retry to stack latency
              this.broadcast(message, true);
            }
            return;
          }
          super.broadcast(message, reliable);
        },
        NETWORK_LATENCY_MS,
        TimeUnit.MILLISECONDS);
  }
}
