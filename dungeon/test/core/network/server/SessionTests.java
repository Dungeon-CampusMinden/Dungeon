package core.network.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import core.network.messages.NetworkMessage;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Session}. */
public class SessionTests {

  /** Validates that unreliable messages use UDP when the session marks UDP as healthy. */
  @Test
  public void test_unreliableSendUsesUdpWhenReady() {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session =
        new Session(
            mock(ChannelHandlerContext.class),
            (target, msg) -> {
              udpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            },
            (ctx, msg) -> {
              tcpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            });

    session.udpAddress(new InetSocketAddress("127.0.0.1", 7777));
    session.udpReady(true);

    assertTrue(session.sendMessage(mock(NetworkMessage.class), false).join());
    assertTrue(udpCalls.get() == 1);
    assertTrue(tcpCalls.get() == 0);
  }

  /** Validates that unreliable messages fall back to TCP when UDP is unavailable. */
  @Test
  public void test_unreliableSendFallsBackToTcpWhenUdpUnavailable() {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session =
        new Session(
            mock(ChannelHandlerContext.class),
            (target, msg) -> {
              udpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            },
            (ctx, msg) -> {
              tcpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            });

    session.udpAddress(new InetSocketAddress("127.0.0.1", 7777));
    session.udpReady(false);

    assertTrue(session.sendMessage(mock(NetworkMessage.class), false).join());
    assertTrue(udpCalls.get() == 0);
    assertTrue(tcpCalls.get() == 1);
  }

  /** Validates that reliable messages always use TCP even when UDP is healthy. */
  @Test
  public void test_reliableSendAlwaysUsesTcp() {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session =
        new Session(
            mock(ChannelHandlerContext.class),
            (target, msg) -> {
              udpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            },
            (ctx, msg) -> {
              tcpCalls.incrementAndGet();
              return CompletableFuture.completedFuture(true);
            });

    session.udpAddress(new InetSocketAddress("127.0.0.1", 7777));
    session.udpReady(true);

    assertTrue(session.sendMessage(mock(NetworkMessage.class), true).join());
    assertTrue(udpCalls.get() == 0);
    assertTrue(tcpCalls.get() == 1);
  }
}
