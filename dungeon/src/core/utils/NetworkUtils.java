package core.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small helper utilities for local network information. */
public final class NetworkUtils {

  private static final String LOOPBACK = "127.0.0.1";

  private NetworkUtils() {}

  /**
   * Collects the non-loopback IPv4 addresses of this machine.
   *
   * <p>These are the addresses other clients on the same network can use to reach a server hosted
   * on this machine. Falls back to the local host address (or {@code 127.0.0.1}) when no suitable
   * interface address can be determined.
   *
   * @return the list of local IPv4 addresses, never empty
   */
  public static List<String> localIpAddresses() {
    List<String> addresses = new ArrayList<>();

    try {
      for (NetworkInterface networkInterface :
          Collections.list(NetworkInterface.getNetworkInterfaces())) {
        if (!networkInterface.isUp()
            || networkInterface.isLoopback()
            || networkInterface.isVirtual()) {
          continue;
        }

        Collections.list(networkInterface.getInetAddresses()).stream()
            .filter(Inet4Address.class::isInstance)
            .map(InetAddress::getHostAddress)
            .forEach(addresses::add);
      }
    } catch (SocketException e) {
      // Fall back below.
    }

    if (addresses.isEmpty()) {
      try {
        addresses.add(InetAddress.getLocalHost().getHostAddress());
      } catch (UnknownHostException e) {
        addresses.add(LOOPBACK);
      }
    }

    return addresses;
  }
}
