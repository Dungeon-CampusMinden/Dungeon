package server;

import com.badlogic.gdx.Gdx;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import contrib.utils.components.Debugger;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Server {
    private static Entity hero;
    private static boolean if_flag = false;
    private static boolean else_flag = false;

    public Server(Entity hero) {
        Server.hero = hero;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        HttpContext startContext = server.createContext("/start");
        startContext.setHandler(Server::handleStartRequest);
        HttpContext resetContext = server.createContext("/reset");
        resetContext.setHandler(Server::handleResetRequest);
        server.start();
    }

    private static void handleStartRequest(HttpExchange exchange) throws IOException {
        InputStream inStream = exchange.getRequestBody();
        String text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);

        String[] actions = text.split("\n");

        for (String action : actions) {
            ifEvaluation(action);

            if (if_flag || else_flag) {
                performAction(action.trim());
            } else {
                performAction(action);
            }
        }

        PositionComponent pc = getHeroPosition();
        String response = pc.position().x + "," + pc.position().y;

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleResetRequest(HttpExchange exchange) throws IOException {
        Debugger.TELEPORT_TO_START();

        PositionComponent pc = getHeroPosition();
        String response = pc.position().x + "," + pc.position().y;

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void ifEvaluation(String action) {
        if (action.contains("falls") && action.contains("wahr")) {
            if_flag = true;
        }
        if (action.contains("falls") && action.contains("naheWand()")) {
            if_flag = isNearWall();
        }
        if (action.contains("falls") && action.contains("WandOben()")) {
            if_flag = isNearWallUp();
        }
        if (action.contains("falls") && action.contains("WandUnten()")) {
            if_flag = isNearWallDown();
        }
        if (action.contains("falls") && action.contains("WandLinks()")) {
            if_flag = isNearWallLeft();
        }
        if (action.contains("falls") && action.contains("WandRechts()")) {
            if_flag = isNearWallRight();
        }
        if (action.contains("sonst")) {
            else_flag = !if_flag;
            if_flag = false;
        }
        if (action.equals("}")) {
            if_flag = false;
            else_flag = false;
        }
    }

    private static void performAction(String action) {
        switch (action) {
            case "oben();" -> up();
            case "unten();" -> down();
            case "links();" -> left();
            case "rechts();" -> right();
                // case "interagieren();" -> interact();
            case "feuerballOben();" -> fireballUp();
            case "feuerballUnten();" -> fireballDown();
            case "feuerballLinks();" -> fireballLeft();
            case "feuerballRechts();" -> fireballRight();
        }
    }

    private static void up() {
        VelocityComponent vc =
                hero.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                hero, VelocityComponent.class));
        vc.currentYVelocity(1 * vc.yVelocity());

        waitDelta();
    }

    private static void down() {
        VelocityComponent vc =
                hero.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                hero, VelocityComponent.class));
        vc.currentYVelocity(-1 * vc.yVelocity());

        waitDelta();
    }

    private static void left() {
        VelocityComponent vc =
                hero.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                hero, VelocityComponent.class));
        vc.currentXVelocity(-1 * vc.xVelocity());

        waitDelta();
    }

    private static void right() {
        VelocityComponent vc =
                hero.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                hero, VelocityComponent.class));
        vc.currentXVelocity(1 * vc.xVelocity());

        waitDelta();
    }

    private static void waitDelta() {
        long timeout = (long) (Gdx.graphics.getDeltaTime() * 1000);
        try {
            TimeUnit.MILLISECONDS.sleep(timeout - 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNearWall() {
        boolean isNearWall;

        isNearWall = isNearWallUp() || isNearWallDown() || isNearWallLeft() || isNearWallRight();
        return isNearWall;
    }

    public static boolean isNearWallUp() {
        boolean isNearWallUp;

        PositionComponent pc = getHeroPosition();
        Point newPositionUp = new Point(pc.position().x, pc.position().y + 0.3f);

        isNearWallUp = !Game.tileAT(newPositionUp).isAccessible();
        return isNearWallUp;
    }

    public static boolean isNearWallDown() {
        boolean isNearWallDown;

        PositionComponent pc = getHeroPosition();
        Point newPositionDown = new Point(pc.position().x, pc.position().y - 0.3f);

        isNearWallDown = !Game.tileAT(newPositionDown).isAccessible();
        return isNearWallDown;
    }

    public static boolean isNearWallLeft() {
        boolean isNearWallLeft;

        PositionComponent pc = getHeroPosition();
        Point newPositionLeft = new Point(pc.position().x - 0.3f, pc.position().y);

        isNearWallLeft = !Game.tileAT(newPositionLeft).isAccessible();
        return isNearWallLeft;
    }

    public static boolean isNearWallRight() {
        boolean isNearWallRight;

        PositionComponent pc = getHeroPosition();
        Point newPositionRight = new Point(pc.position().x + 0.3f, pc.position().y);

        isNearWallRight = !Game.tileAT(newPositionRight).isAccessible();
        return isNearWallRight;
    }

    public static PositionComponent getHeroPosition() {
        return hero.fetch(PositionComponent.class)
                .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    }

    public static void fireballUp() {
        Skill fireball =
                new Skill(
                        new FireballSkill(
                                new Supplier<Point>() {
                                    @Override
                                    public Point get() {
                                        Point heroPoint =
                                                new Point(
                                                        getHeroPosition().position().x,
                                                        getHeroPosition().position().y);
                                        heroPoint.y += 1;
                                        return heroPoint;
                                    }
                                }),
                        1);
        fireball.execute(hero);
        waitDelta();
    }

    public static void fireballDown() {
        Skill fireball =
                new Skill(
                        new FireballSkill(
                                new Supplier<Point>() {
                                    @Override
                                    public Point get() {
                                        Point heroPoint =
                                                new Point(
                                                        getHeroPosition().position().x,
                                                        getHeroPosition().position().y);
                                        heroPoint.y -= 1;
                                        return heroPoint;
                                    }
                                }),
                        1);
        fireball.execute(hero);
        waitDelta();
    }

    public static void fireballLeft() {
        Skill fireball =
                new Skill(
                        new FireballSkill(
                                new Supplier<Point>() {
                                    @Override
                                    public Point get() {
                                        Point heroPoint =
                                                new Point(
                                                        getHeroPosition().position().x,
                                                        getHeroPosition().position().y);
                                        heroPoint.x -= 1;
                                        return heroPoint;
                                    }
                                }),
                        1);
        fireball.execute(hero);
        waitDelta();
    }

    public static void fireballRight() {
        Skill fireball =
                new Skill(
                        new FireballSkill(
                                new Supplier<Point>() {
                                    @Override
                                    public Point get() {
                                        Point heroPoint =
                                                new Point(
                                                        getHeroPosition().position().x,
                                                        getHeroPosition().position().y);
                                        heroPoint.x += 1;
                                        return heroPoint;
                                    }
                                }),
                        1);
        fireball.execute(hero);
        waitDelta();
    }

    /*public static void interact() {
        InteractionTool.interactWithClosestInteractable(hero);
        waitDelta();
    }*/
}
