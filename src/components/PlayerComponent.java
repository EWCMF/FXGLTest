package components;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class PlayerComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk;

    private PhysicsComponent physics;
    private int jumps = 2;

    public PlayerComponent() {
        animIdle = new AnimationChannel(FXGL.image("playerIdle.png"), 1, 64, 80, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(FXGL.image("playerWalk2.png"), 6, 78, 80, Duration.seconds(1), 0,5);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(32, 40));
        entity.getViewComponent().addChild(texture);

        physics.onGroundProperty().addListener((observableValue, old, isOnGround) -> {
            if (isOnGround)
                jumps = 2;
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (isMoving()) {
            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
        } else {
            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    private boolean isMoving() {
        return physics.isMovingX();
    }

    public void left() {

        getEntity().setScaleX(-1);
        physics.setVelocityX(-200);
    }

    public void right() {

        getEntity().setScaleX(1);
        physics.setVelocityX(200);
    }

    public void jump() {
        if (jumps == 0)
            return;
        physics.setVelocityY(-300);
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
