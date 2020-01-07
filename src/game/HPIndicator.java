package game;

import com.almasb.fxgl.animation.Interpolators;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class HPIndicator extends StackPane {
    private final int length = 300;
    private final int viewBound = 32;

    private HPComponent playerHP;

    private Rectangle inner;

    public HPIndicator(HPComponent health) {
        this.playerHP = health;

        var outer = new Rectangle(45 + viewBound, 45 + viewBound, length, 40);
        outer.setStroke(Color.BLACK);

        inner = new Rectangle(43 + viewBound, 43 + viewBound, length - 10, 35);
        inner.setStroke(Color.LIGHTGREEN);
        inner.setFill(Color.GREEN);

        inner.fillProperty().bind(
                Bindings.when(health.valueProperty().divide(playerHP.getMaxHP() * 1.0).greaterThan(0.3)).then(Color.LIGHTGREEN.brighter()).otherwise(Color.RED.brighter())
        );

        playerHP.valueProperty().addListener((o, old, hp) -> {
            hpChanged(hp.intValue());
        });

        getChildren().addAll(outer, inner);
    }

    private void hpChanged(int hp) {
        var timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(0.66), new KeyValue(inner.widthProperty(), hp * 1.0 / playerHP.getMaxHP() * length, Interpolators.LINEAR.EASE_OUT()))
        );
        timeline.play();
    }
}
