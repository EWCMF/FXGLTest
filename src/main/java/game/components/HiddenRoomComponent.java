package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.image.Image;

public class HiddenRoomComponent extends Component {
    @Override
    public void onAdded() {
        int style = entity.getProperties().getInt("style");
        Image image;

        switch (style) {
            case 1:
                image = FXGL.image("hiddenGray1.png");
                break;
            case 2:
                image = FXGL.image("hiddenGray2.png");
                break;
            case 3:
                image = FXGL.image("hiddenGray3.png");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + style);
        }
        Texture texture = new Texture(image);
        entity.getViewComponent().addChild(texture);
    }

    public void fadeOut() {
        FXGL.animationBuilder().fadeOut(entity).buildAndPlay();
    }

    public void fadeIn() {
        FXGL.animationBuilder().fadeIn(entity).buildAndPlay();
    }
}
