package server;

import jakarta.websocket.server.ServerEndpointConfig;
import java.io.File;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;

public class WebsocketServer {

  static Server server;

  public WebsocketServer() {
    configServer();
  }

  private void configServer() {
    // Opening a server at port 8080
    server = new Server(8080);

    ServletContextHandler handler = new ServletContextHandler("/");

    // Set the base resource directory for static files
    String absolutePath = new File("VisuPrg/frontend/dist").getAbsolutePath();
    handler.setBaseResourceAsString(absolutePath);

    handler.addServlet(DefaultServlet.class, "/*");
    server.setHandler(handler);

    // Configure WebSocket support
    JakartaWebSocketServletContainerInitializer.configure(
        handler,
        (servletContext, container) -> {
          // Set maximum buffer size for text messages
          container.setDefaultMaxTextMessageBufferSize(128 * 1024);

          // Add WebSocket endpoint
          container.addEndpoint(
              ServerEndpointConfig.Builder.create(StandardEndpoint.class, "/ws").build());
        });
  }

  public void start() throws Exception {
    server.start();
  }
}
