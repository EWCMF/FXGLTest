package game.level;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import game.BasicGameTypes;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class ExitSwitchComponent extends Component {
    private boolean alreadyPressed = false;
    private AnimatedTexture texture;
    private AnimationChannel animOff, animOn;

    public ExitSwitchComponent() {
        Image image = FXGL.image("exitSwitch.png");

        animOff = new AnimationChannel(image, 7, 59, 64, Duration.seconds(1), 0, 0);
        animOn = new AnimationChannel(image, 7, 59, 64, Duration.seconds(0.4), 0, 6);

        texture = new AnimatedTexture(animOff);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }

    public void activate() {
        if (alreadyPressed)
            return;
        texture.playAnimationChannel(animOn);
        FXGL.getGameWorld().getEntitiesByType(BasicGameTypes.EXIT).get(0).getComponent(ExitDoorComponent.class).openDoor();
        alreadyPressed = true;
    }
}
