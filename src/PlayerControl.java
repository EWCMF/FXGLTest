import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class PlayerControl extends Component {

    private PhysicsComponent physics;
    private int jumps = 2;

    @Override
    public void onAdded() {
        physics.onGroundProperty().addListener((observableValue, old, isOnGround) -> {
            if (isOnGround)
                jumps = 2;
        });
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
