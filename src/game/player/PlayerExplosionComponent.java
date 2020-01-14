package game.player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import game.BasicGameApp;
import game.BasicGameTypes;
import game.characters.FlickerComponent;
import game.enemy.MovingEnemyComponent;
import game.enemy.TurretComponent;
import game.level.BreakableWallComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerExplosionComponent extends Component {
    @Override
    public void onAdded() {
        double minX = entity.getPosition().getX();
        double minY = entity.getPosition().getY();
        int damage = entity.getProperties().getInt("damage");

        List<Entity> affected = FXGL.getGameWorld().getEntitiesInRange(new Rectangle2D(minX, minY, 200, 200)).stream()
                .filter(e -> e.hasComponent(PlayerComponent.class)
                        || e.hasComponent(MovingEnemyComponent.class)
                        || e.hasComponent(TurretComponent.class)
                        || e.hasComponent(BreakableWallComponent.class))
                .collect(Collectors.toList());
        for (Entity value : affected) {
            if (value.hasComponent(PlayerComponent.class)) {
                value.getComponent(FlickerComponent.class).flicker();
                if (value.getScaleX() == -1) {
                    value.getComponent(PlayerComponent.class).onHit(3 * BasicGameApp.enemyDamageModifier, new Point2D(1, 0));
                }
                else {
                    value.getComponent(PlayerComponent.class).onHit(3 * BasicGameApp.enemyDamageModifier, new Point2D(-1, 0));
                }
            }
            if (value.hasComponent(MovingEnemyComponent.class)) {
                value.getComponent(MovingEnemyComponent.class).onHit(damage);
                value.getComponent(FlickerComponent.class).flicker();
                if (value.getScaleX() == -1)
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(200, -200);
                else
                    value.getComponent(PhysicsComponent.class).setLinearVelocity(-200, -200);
            }
            if (value.hasComponent(TurretComponent.class)) {
                value.getComponent(TurretComponent.class).onHit(damage);
                value.getComponent(FlickerComponent.class).flicker();
            }
            if (value.hasComponent(BreakableWallComponent.class))
                value.getComponent(BreakableWallComponent.class).onHit(damage);
        }
    }
}
