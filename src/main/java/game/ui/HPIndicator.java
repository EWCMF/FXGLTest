package game.ui;

import com.almasb.fxgl.animation.Interpolators;
import game.BasicGameApp;
import game.components.HPComponent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class HPIndicator extends AnchorPane {
    private final int length = 300;
    private final int nodeX = BasicGameApp.hpInX;
    private final int nodeY = BasicGameApp.hpInY;

    private HPComponent playerHP;

    private Rectangle inner;
    private Polygon innerTest;

    public HPIndicator(HPComponent health) {
        this.playerHP = health;

        var outer = new Rectangle(nodeX, nodeY, length, 40);
        outer.setStroke(Color.BLACK);

        inner = new Rectangle(nodeX + 3, nodeY + 3, length - 6, 35);
        inner.setStroke(Color.LIGHTGREEN);
        inner.setFill(Color.GREEN);

        inner.fillProperty().bind(
                Bindings.when(health.valueProperty().divide(playerHP.getMaxHP() * 1.0).greaterThan(0.3)).then(Color.GREEN.brighter()).otherwise(Color.RED.brighter())
        );

        playerHP.valueProperty().addListener((o, old, hp) -> {
            hpChanged(hp.intValue());
        });
        getChildren().addAll(outer, inner);

    }

    private void hpChanged(int hp) {
        var timeline = new Timeline();
        if (hp != 0) {
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(0.44), new KeyValue(inner.widthProperty(), hp * 1.0 / playerHP.getMaxHP() * (length - 6), Interpolators.LINEAR.EASE_IN()))
            );
        }
        else {
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(0.44), new KeyValue(inner.widthProperty(), -1, Interpolators.LINEAR.EASE_IN()))
            );
        }
        timeline.play();
    }
}
