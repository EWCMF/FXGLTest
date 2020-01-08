package game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.Map;

import static game.BasicGameTypes.*;
import static com.almasb.fxgl.dsl.FXGL.*;


public class BasicGameApp extends GameApplication {
    public static double aimUpVectorY = -150;
    public static double aimDownVectorY = 150;

    public static int hpInX = 15;
    public static int hpInY = 35;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Basic Game App");
        settings.setVersion("0.1");
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).jump();
            }
        }, KeyCode.SPACE);

        input.addAction(new UserAction("Test something") {
            @Override
            protected void onActionBegin() {
                double width = player.getBoundingBoxComponent().getWidth();
                double height = player.getBoundingBoxComponent().getHeight();

                SpawnData data = new SpawnData(player.getBoundingBoxComponent().getMinXWorld(), player.getBoundingBoxComponent().getMinYWorld());
                data.put("width", width);
                data.put("height", height);

                Entity flicker = FXGL.getGameWorld().spawn("hitboxFlicker", data);
                System.out.println(flicker.getViewComponent().getChildren());
                System.out.println(player.getViewComponent().getChildren());

            }
        }, KeyCode.F);

        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onActionBegin() {
                Point2D vector = FXGL.getInput().getVectorToMouse(player.getPosition());
                if (FXGL.getInput().getVectorToMouse(player.getPosition()).getX() < 0 && player.getTransformComponent().getScaleX() != -1.0)
                    return;
                if (FXGL.getInput().getVectorToMouse(player.getPosition()).getX() > 0 && player.getTransformComponent().getScaleX() != 1.0)
                    return;

                double gunPosition = FXGL.getInput().getVectorToMouse(player.getPosition()).getY();

                if (player.getTransformComponent().getScaleX() == -1.0) {
                    // Middle position, Up position, down position.
                    if (gunPosition > aimUpVectorY && gunPosition < aimDownVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(0, 35));
                    else if (gunPosition < aimUpVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(6, 13));
                    else if (gunPosition > aimDownVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(7, 70));
                }
                else {
                    // Middle position, Up position, down position.
                    if (gunPosition > aimUpVectorY && gunPosition < aimDownVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(52, 35));
                    else if (gunPosition < aimUpVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(46, 13));
                    else if (gunPosition > aimDownVectorY)
                        player.getComponent(PlayerComponent.class).fire(vector, player.getPosition().add(45, 70));
                }
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
    }

    public Entity player;
    public Point2D start;

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new BasicGameFactory());
        FXGL.setLevelFromMap("test2.tmx");

        start = FXGL.getGameWorld().getSingleton(START).getPosition();

        player = FXGL.getGameWorld().spawn("player", start);

        initHP();

        Viewport viewport = FXGL.getGameScene().getViewport();

        viewport.setBounds(32, 0, 32 * 100, 32 * 35);
        viewport.bindToEntity(player, FXGL.getAppWidth() / 4, FXGL.getAppHeight() / 2);
    }

    @Override
    protected void initUI() {
        var hp = new HPIndicator(player.getComponent(HPComponent.class));

        addUINode(hp, hpInX, hpInY);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 760);
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, TARGET) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bullet, Entity target) {
                bullet.removeFromWorld();
                target.getComponent(EnemyComponent.class).onHit(bullet.getInt("damage"));
                target.getComponent(FlickerComponent.class).flicker();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(BULLET, WALL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity wall) {
                bullet.removeFromWorld();
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(ENEMYBULLET, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity enemyBullet, Entity player) {
                enemyBullet.removeFromWorld();
                player.getComponent(PlayerComponent.class).onHit(enemyBullet.getInt("damage"));
                player.getComponent(FlickerComponent.class).flicker();
            }
        });
    }

    protected void initHP() {
        if (player != null) {
            player.getComponent(PlayerComponent.class).restoreHP();
        }
        for (int i = 0; i < getGameWorld().getEntitiesByType(TARGET).size(); i++) {
            getGameWorld().getEntitiesByType(TARGET).get(i).getComponent(EnemyComponent.class).initHP();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
