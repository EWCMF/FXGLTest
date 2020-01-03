import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;

public class PlayerControl extends Component {

    private PhysicsComponent physics;
    private int jumps = 1;

    @Override
    public void onAdded() {
        physics.onGroundProperty().addListener((observableValue, old, isOnGround) -> {
            if (isOnGround)
                jumps = 1;
        });
    }

    public void left() {
        physics.setVelocityX(-200);
    }

    public void right() {
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


}
