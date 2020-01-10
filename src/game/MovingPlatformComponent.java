package game;

import com.almasb.fxgl.dsl.FXGL;
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


    @Override
    public void onAdded() {
    }

    @Override
    public void onUpdate(double tpf) {
        // Needed for rebound
        playerPlatformDiffLeftSideX = entity.getBoundingBoxComponent().getMinXWorld() - FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getBoundingBoxComponent().getMaxXWorld();
        playerPlatformDiffRightSideX = entity.getBoundingBoxComponent().getMaxXWorld() - FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getBoundingBoxComponent().getMinXWorld();
        playerPlatformDiffY = entity.getBoundingBoxComponent().getMinYWorld() - FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getBoundingBoxComponent().getMaxYWorld();

        movingLeft = physics.getVelocityX() < 0;

        if (playerPlatformDiffLeftSideX <= playerReboundRange && playerPlatformDiffY < 1 && playerPlatformDiffY > -100)
            FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getComponent(PhysicsComponent.class).setVelocityX(-100);
        if (playerPlatformDiffRightSideX <= playerReboundRange && playerPlatformDiffY < 1 && playerPlatformDiffY > -100)
            FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER).getComponent(PhysicsComponent.class).setVelocityX(100);
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
