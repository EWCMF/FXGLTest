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
    private boolean locked = false;
    private AnimatedTexture texture;
    private AnimationChannel animOpen, animClosed, animClosing;
    private String openType;
    private PhysicsComponent physics;

    public SideDoorComponent() {
        Image image = FXGL.image("sideDoor.png");

        animClosed = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 0, 0);
        animOpen = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 0, 9);
        animClosing = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 9, 18);

        texture = new AnimatedTexture(animClosed);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);

        openType = entity.getString("openType");
    }

    public void checkCondition() {
        if (opened && !openType.equals("auto") && !locked)
            return;

        if (openType.equals("auto") && !opened)
            openDoor();
        else if (openType.equals("auto"))
            closeDoor();

        if (openType.equals("key") && FXGL.getb("hasKeycard"))
            openDoor();
    }

    public void openDoor() {
        opened = true;
        texture.playAnimationChannel(animOpen);
        FXGL.runOnce(() -> {
            entity.getComponent(PhysicsComponent.class).getBody().setActive(false);
        }, Duration.seconds(0.5));
    }

    public void closeDoor() {
        opened = false;
        texture.playAnimationChannel(animClosing);
        entity.getComponent(PhysicsComponent.class).getBody().setActive(true);
    }

    public boolean isOpened() {
        return opened;
    }

    public void lockDoor() {
        if (opened)
            closeDoor();
        locked = true;
    }
}
