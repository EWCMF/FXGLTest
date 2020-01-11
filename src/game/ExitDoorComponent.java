package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class ExitDoorComponent extends Component {
    private boolean opened = false;
    private AnimatedTexture texture;
    private AnimationChannel animClosed, animOpen;

    public ExitDoorComponent() {
        Image image = FXGL.image("exitDoor.png");

        animClosed = new AnimationChannel(image, 4, 64, 96, Duration.seconds(1), 0, 0);
        animOpen = new AnimationChannel(image, 4, 64, 96, Duration.seconds(0.4), 0, 11);

        texture = new AnimatedTexture(animClosed);
        texture.loop();
    }

    @Override
    public void onUpdate(double tpf) {
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);

        if (entity.getProperties().getBoolean("startOpened"))
            openDoor();
    }

    public void openDoor() {
        opened = true;
        texture.playAnimationChannel(animOpen);
    }

    public boolean isOpened() {
        return opened;
    }
}
