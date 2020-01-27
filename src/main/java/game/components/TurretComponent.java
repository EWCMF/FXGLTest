package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.time.LocalTimer;
import game.BasicGameTypes;
import javafx.geometry.Point2D;
import javafx.util.Duration;


@Required(HPComponent.class)
public class TurretComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    boolean alertedRight = false;
    boolean alertedLeft = false;

    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        int alertRange = entity.getInt("alertRange");

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (alertedRight || alertedLeft) {
                basicEnemyAttack(player);
                enemyAttackInterval.capture();
            }
        }

        if (distanceToPlayerX(player) > -alertRange && distanceToPlayerX(player) < 0
                && distanceToPlayerY(player) > 0 && distanceToPlayerY(player) < alertRange / 1.5
                || distanceToPlayerY(player) < 0 && distanceToPlayerY(player) > -alertRange / 1.5) {
            alertedLeft = true;
        }
        else alertedLeft = false;

        if (distanceToPlayerX(player) < alertRange && distanceToPlayerX(player) > 0
                && distanceToPlayerY(player) > 0 && distanceToPlayerY(player) < alertRange / 1.5
                || distanceToPlayerY(player) < 0 && distanceToPlayerY(player) > -alertRange / 1.5) {
            alertedRight = true;
        }
        else alertedRight = false;
    }

    public double distanceToPlayerX(Entity player) {
        return player.getPosition().getX() - entity.getPosition().getX();
    }

    public double distanceToPlayerY(Entity player) {
        return player.getPosition().getY() - entity.getPosition().getY();
    }

    public void basicEnemyAttack(Entity player) {
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget =  player.getBoundingBoxComponent().getCenterWorld().add(0, -12).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        if (entity.isType(BasicGameTypes.TURRET))
            FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
        else
            FXGL.getGameWorld().spawn("eliteEnemyBullet", new SpawnData(enemyPosition).put("direction", enemyTarget));
    }

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            FXGL.spawn("enemyDeathEffect", entity.getPosition());
            entity.removeFromWorld();
        }
    }

    public void initHP() {
        hp.setValue(hp.getMaxHP());
    }
}
