
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import components.PlayerComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Map;


public class BasicGameApp extends GameApplication {
    public enum EntityType {
        PLAYER, TARGET, WALL, BULLET, EXIT
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
                FXGL.getGameState().increment("pixelsMoved", +5);
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
                FXGL.getGameState().increment("pixelsMoved", +5);
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
                FXGL.getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.SPACE);

        input.addAction(new UserAction("Play Sound") {
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

                player.getComponent(PlayerComponent.class).fire(vector, player.getPosition());
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("pixelsMoved", 0);
    }

    private Entity player;

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new BasicGameFactory());

        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.DYNAMIC);
        physicsComponent.addGroundSensor(new HitBox(BoundingShape.box(64, 80)));

        physicsComponent.setFixtureDef(new FixtureDef().friction(0.0f));

        player = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(300, 300)
                //.view(new Rectangle(25, 25, Color.RED))
                .bbox(new HitBox(BoundingShape.box(64, 80)))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .with(new PlayerComponent())
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.TARGET)
                .at(500, 200)
                .viewWithBBox(new Circle(15, Color.BLACK))
                .with(new CollidableComponent(true))
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.TARGET)
                .at(100, 200)
                .viewWithBBox(new Circle(15, Color.BLACK))
                .with(new CollidableComponent(true))
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.WALL)
                .at(0, 670)
                .view(new Rectangle(600 * 5, 50, Color.DARKGREY))
                .bbox(new HitBox(BoundingShape.box(600 * 5, 50)))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.WALL)
                .at(-50, 0)
                .view(new Rectangle(50, 720, Color.DARKGREY))
                .bbox(new HitBox(BoundingShape.box(50, 600)))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.WALL)
                .at(600, 520)
                .view(new Rectangle(50, 200, Color.DARKGREY))
                .bbox(new HitBox(BoundingShape.box(50, 200)))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .buildAndAttach();

        FXGL.entityBuilder()
                .type(EntityType.EXIT)
                .at(600 * 5 - 200, 520)
                .view(new Rectangle(100, 150, Color.GREEN))
                .with(new CollidableComponent(true))
                .buildAndAttach();

        Viewport viewport = FXGL.getGameScene().getViewport();

        viewport.setBounds(0, 0, 600 * 5, 720);
        viewport.bindToEntity(player, FXGL.getAppWidth() / 3, FXGL.getAppHeight() / 2);
    }

    @Override
    protected void initUI() {
        Text textPixels = new Text();
        textPixels.setTranslateX(100); // x = 50
        textPixels.setTranslateY(100); // y = 100

        textPixels.textProperty().bind(FXGL.getGameState().intProperty("pixelsMoved").asString());

        FXGL.getGameScene().addUINode(textPixels); // add to the scene graph
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
