package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.util.Duration;


public class MovingPlatformComponent extends Component {
    private PhysicsComponent physics;
    private boolean movingLeft;

    private int playerReboundRange = 10;
    private double playerPlatformDiffLeftSideX;
    private double playerPlatformDiffRightSideX;
    private double playerPlatformDiffY;
    private Entity player;


    @Override
    public void onAdded() {
    }

    @Override
    public void onUpdate(double tpf) {
        // Needed for rebound
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        playerPlatformDiffLeftSideX = entity.getBoundingBoxComponent().getMinXWorld() - player.getBoundingBoxComponent().getMaxXWorld();
        playerPlatformDiffRightSideX = player.getBoundingBoxComponent().getMinXWorld() - entity.getBoundingBoxComponent().getMaxXWorld();
        playerPlatformDiffY = entity.getBoundingBoxComponent().getMinYWorld() - player.getBoundingBoxComponent().getMaxYWorld();
        movingLeft = physics.getVelocityX() < 0;

        if (playerPlatformDiffLeftSideX <= playerReboundRange && playerPlatformDiffRightSideX <= -playerReboundRange && playerPlatformDiffY < 1 && playerPlatformDiffY > -110) {
            player.getComponent(PhysicsComponent.class).setVelocityX(-100);
        }
        if (playerPlatformDiffRightSideX <= playerReboundRange && playerPlatformDiffLeftSideX <= -playerReboundRange && playerPlatformDiffY < 1 && playerPlatformDiffY > -110) {
            player.getComponent(PhysicsComponent.class).setVelocityX(100);
        }
    }

    public void checkDirection() {
        if (movingLeft)
            stopFromRight();
        else
            stopFromLeft();
    }

    public void stopFromRight() {
        physics.setVelocityX(0);
        FXGL.runOnce(() -> {
            physics.setVelocityX(100);
        }, Duration.seconds(3));
    }

    public void stopFromLeft() {
        physics.setVelocityX(0);
        FXGL.runOnce(() -> {
            physics.setVelocityX(-100);
        }, Duration.seconds(3));
    }
}
