package game.enemy;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import game.BasicGameTypes;
import game.characters.HPComponent;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class BaronOfHellComponent extends Component {
    private Entity player;
    private PhysicsComponent physics;
    private HPComponent hp;
    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk, animAtk;

    public BaronOfHellComponent() {
        Image image = FXGL.image("testBoH.png");

        animIdle = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 2, 5);
        animAtk = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 6, 8);

        texture = new AnimatedTexture(animIdle);
        texture.loop();

    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(173, 176));
        entity.getViewComponent().addChild(texture);
    }

    public void walkLeft() {
        physics.setVelocityX(-100);
        texture.loopAnimationChannel(animWalk);
        entity.setScaleX(1);
        FXGL.runOnce(() -> {
            physics.setVelocityX(0);
            texture.loopAnimationChannel(animIdle);
        }, Duration.seconds(5));
    }

    public void walkRight() {
        entity.setScaleX(-1);
        physics.setVelocityX(100);
        texture.loopAnimationChannel(animWalk);
        FXGL.runOnce(() -> {
            physics.setVelocityX(0);
            texture.loopAnimationChannel(animIdle);
        }, Duration.seconds(5));
    }

    public void attack() {
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        texture.playAnimationChannel(animAtk);
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
            Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(-50, -30).subtract(entity.getBoundingBoxComponent().getCenterWorld());
            FXGL.getGameWorld().spawn("normalBOH", new SpawnData(enemyPosition).put("direction", enemyTarget));
        }, Duration.seconds(0.5));

//        FXGL.runOnce(() -> {
//            texture.loopAnimationChannel(animIdle);
//        }, Duration.seconds(5));
    }

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            FXGL.spawn("enemyDeathEffect", entity.getPosition());
            entity.removeFromWorld();
        }
    }
}
