package game.level;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import game.BasicGameTypes;
import game.characters.HPComponent;
import javafx.geometry.Rectangle2D;

import java.util.List;

public class BreakableWallComponent extends Component {
    private HPComponent hp;

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            List<Entity> hidden = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(entity.getX()-1, entity.getY(), entity.getWidth()+2, entity.getHeight()));
            hidden.stream()
                    .filter(e -> e.isType(BasicGameTypes.HIDDEN))
                    .findAny()
                    .ifPresent(e -> {
                        FXGL.animationBuilder()
                                .onFinished(e::removeFromWorld)
                                .fadeOut(e)
                                .buildAndPlay();
                    });
            entity.removeFromWorld();
        }
    }
}
