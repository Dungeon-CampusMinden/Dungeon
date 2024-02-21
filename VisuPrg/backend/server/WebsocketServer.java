package server;

import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;

import java.io.File;

public class WebsocketServer {

    static Server server;

//    public static void main(String[] args) throws Exception {
//        server = new Server(8080);
//
//        ServletContextHandler handler = new ServletContextHandler("/");
//        String absolutePath = new File("VisuPrg/frontend").getAbsolutePath();
//        handler.setBaseResourceAsString(absolutePath);
//
//        handler.addServlet(DefaultServlet.class, "/*");
//        server.setHandler(handler);
//
//        JakartaWebSocketServletContainerInitializer.configure(handler, (servletContext, container) ->
//        {
//            container.setDefaultMaxTextMessageBufferSize(128 * 1024);
//
//            container.addEndpoint(
//                ServerEndpointConfig.Builder.create(MyHelloServer.class, "/ws")
//                    .build()
//            );
//        });
//
//
//        server.start();
//        server.join();
//    }

    public WebsocketServer(){
        configServer();
    }

    private void configServer() {
        server = new Server(8080);

        ServletContextHandler handler = new ServletContextHandler("/");
        String absolutePath = new File("VisuPrg/frontend").getAbsolutePath();
        handler.setBaseResourceAsString(absolutePath);

        handler.addServlet(DefaultServlet.class, "/*");
        server.setHandler(handler);

        JakartaWebSocketServletContainerInitializer.configure(handler, (servletContext, container) ->
        {
            container.setDefaultMaxTextMessageBufferSize(128 * 1024);

            container.addEndpoint(
                ServerEndpointConfig.Builder.create(StandardEndpoint.class, "/ws")
                    .build()
            );
        });
    }

    public void start() throws Exception {
        server.start();
    }
}
