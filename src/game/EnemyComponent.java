package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

@Required(HPComponent.class)
public class EnemyComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private HPComponent hp;
    boolean alerted = false;

    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * 2) + 2))) {
            if (alerted) {
                basicEnemyAttack(player);
                enemyAttackInterval.capture();
            }
        }

        alerted = distanceToPlayer(player) < entity.getInt("alertRange");
    }

    public double distanceToPlayer(Entity player) {
        return player.getPosition().distance(getEntity().getPosition());
    }

    public void basicEnemyAttack(Entity player) {
        FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(entity.getPosition()).put("direction", player.getPosition().subtract(entity.getPosition())));
    }

    public void onHit(int damage) {
        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0) {
            entity.removeFromWorld();
        }
    }

    public void initHP() {
        hp.setValue(hp.getMaxHP());
    }
}
