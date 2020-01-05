package components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class PlayerComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animIdleUp, animWalk, animWalkUp;

    private PhysicsComponent physics;
    private int jumps = 2;

    public PlayerComponent() {

        Image image = FXGL.image("player.png");

        animIdle = new AnimationChannel(image, 14, 78, 94, Duration.seconds(1), 0, 0);
        animIdleUp = new AnimationChannel(image, 14, 78, 94, Duration.seconds(1), 1, 1);

        animWalk = new AnimationChannel(image, 14, 78, 94, Duration.seconds(1), 2,7);
        animWalkUp = new AnimationChannel(image, 14, 78, 94, Duration.seconds(1), 8, 13);


        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(32, 47));
        entity.getViewComponent().addChild(texture);

        physics.onGroundProperty().addListener((observableValue, old, isOnGround) -> {
            if (isOnGround)
                jumps = 2;
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (isMoving()) {
            if (texture.getAnimationChannel() != animWalk && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > -50) {
                texture.loopAnimationChannel(animWalk);
            }
            else if (texture.getAnimationChannel() != animWalkUp && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < -50) {
                System.out.println("Test");
                texture.loopAnimationChannel(animWalkUp);
            }
        } else {
            if (texture.getAnimationChannel() != animIdle && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > -50) {
                texture.loopAnimationChannel(animIdle);
            }
            else if (FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < -50) {
                texture.loopAnimationChannel(animIdleUp);
            }
        }
    }

    private boolean isMoving() {
        return physics.isMovingX();
    }

    public void left() {

        getEntity().setScaleX(-1);
        physics.setVelocityX(-300);
    }

    public void right() {

        getEntity().setScaleX(1);
        physics.setVelocityX(300);
    }

    public void jump() {
        if (jumps == 0)
            return;
        physics.setVelocityY(-400);
        jumps--;
    }

    public void stop() {
        physics.setVelocityX(0);
    }

    public void fire(Point2D point2D, Point2D position) {
        SpawnData spawnData = new SpawnData(point2D).put("direction", point2D.normalize());
        spawnData.put("position", position);
        FXGL.spawn("playerBullet", spawnData);
    }


}
