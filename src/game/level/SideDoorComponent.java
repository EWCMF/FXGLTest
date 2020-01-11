package game.level;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class SideDoorComponent extends Component {
    private boolean opened = false;
    private AnimatedTexture texture;
    private AnimationChannel animOpen, animClosed;

    public SideDoorComponent() {
        Image image = FXGL.image("sideDoor.png");

        animClosed = new AnimationChannel(image, 10, 32, 160, Duration.seconds(1), 0, 0);
        animOpen = new AnimationChannel(image, 10, 32, 160, Duration.seconds(1), 0, 9);

        texture = new AnimatedTexture(animClosed);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);

        FXGL.runOnce(() -> {
            openDoor();
        }, Duration.seconds(10));
    }

    public void openDoor() {
        opened = true;
        texture.playAnimationChannel(animOpen);
        FXGL.runOnce(() -> {
            entity.getComponent(PhysicsComponent.class).getBody().setActive(false);
        }, Duration.seconds(0.5));
    }

    public boolean isOpened() {
        return opened;
    }
}
