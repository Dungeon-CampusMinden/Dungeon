package server;

import core.game.ECSManagment;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import systems.VisualProgrammingSystem;
import tasks.CreateVariable;
import tasks.IVisuTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ServerEndpoint("/ws")
public class StandardEndpoint {

    private Session session;
    private ScheduledExecutorService scheduler;
    VisualProgrammingSystem visualProgrammingSystem;

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        startPingScheduler();
    }

    @OnClose
    public void onClose() {
        stopPingScheduler();
    }

    private void startPingScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()){
                try{
                    session.getBasicRemote().sendPing(ByteBuffer.wrap("Ping".getBytes()));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        },0,25, TimeUnit.SECONDS);
    }

    private void stopPingScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @OnMessage
    public void handleMessage(String message) {
        System.out.println("Message recieved: " + message);
        if (visualProgrammingSystem == null ) visualProgrammingSystem = (VisualProgrammingSystem) ECSManagment.systems().get(VisualProgrammingSystem.class);

        // Todo - react accordingly to message/create a new Task
        ArrayList<IVisuTask> tasks = parseMessage(message);
        visualProgrammingSystem.addTask(tasks);
    }

    private ArrayList<IVisuTask> parseMessage(String message) {
        ArrayList<IVisuTask> tasks = new ArrayList<>();

        // declaration of variables
        if(message.startsWith("var")){

            Pattern pattern = Pattern.compile("(?<=var\\s|,\\s)(\\w+)");
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()){
                tasks.add(new CreateVariable(message, visualProgrammingSystem, matcher.group(1).trim()));
            }

        }else if (message.startsWith("")){

        }else{
            // todo - error
        }

        return tasks;
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage message){
        System.out.println("Pong recieved!");
    }

}
