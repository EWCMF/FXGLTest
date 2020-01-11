package game.characters;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.util.Duration;

public class FlickerComponent extends Component {
    private boolean alreadyFlickering;

    public void flicker() {
        if (entity != null && !alreadyFlickering) {
            alreadyFlickering = true;
            entity.getViewComponent().setOpacity(0.1);
            FXGL.runOnce(() -> {
                if (entity != null)
                entity.getViewComponent().setOpacity(1);
            }, Duration.seconds(0.2));
            FXGL.runOnce(() -> {
                if (entity != null)
                entity.getViewComponent().setOpacity(0.1);
            }, Duration.seconds(0.4));
            FXGL.runOnce(() -> {
                if (entity != null)
                entity.getViewComponent().setOpacity(1);
            }, Duration.seconds(0.6));
            FXGL.runOnce(() -> {
                if (entity != null)
                entity.getViewComponent().setOpacity(0.1);
            }, Duration.seconds(0.8));
            FXGL.runOnce(() -> {
                if (entity != null)
                entity.getViewComponent().setOpacity(1);
                alreadyFlickering = false;
            }, Duration.seconds(1));
        }
    }
}
