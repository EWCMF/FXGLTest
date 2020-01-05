
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import components.PlayerComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.Map;


public class BasicGameApp extends GameApplication {
    private double aimUpVectorY = -150;
    private double aimDownVectorY = 150;

    public enum EntityType {
        PLAYER, TARGET, WALL, BULLET, START, EXIT
    }

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

        input.addAction(new UserAction("Test vectors") {
            @Override
            protected void onActionBegin() {
                System.out.println(player.getPosition());
                System.out.println(FXGL.getInput().getMousePositionWorld());
                System.out.println(FXGL.getInput().getVectorToMouse(player.getPosition()));
                System.out.println(FXGL.getInput().getVectorToMouse(player.getPosition()).normalize());
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
        FXGL.setLevelFromMap("test.tmx");

        start = FXGL.getGameWorld().getSingleton(EntityType.START).getPosition();

        player = FXGL.getGameWorld().spawn("player", start);

        Viewport viewport = FXGL.getGameScene().getViewport();

        viewport.setBounds(21, 0, 21 * 100, 21 * 35);
        viewport.bindToEntity(player, FXGL.getAppWidth() / 3, FXGL.getAppHeight() / 2);
    }

    @Override
    protected void initUI() {
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 760);
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.TARGET) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bullet, Entity target) {
                bullet.removeFromWorld();
                target.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity wall) {
                bullet.removeFromWorld();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
