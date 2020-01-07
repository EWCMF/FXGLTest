package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

public class EnemySensorComponent extends Component {
    private LocalTimer enemyAttackInterval;
    boolean alerted = false;

    public void onAdded() {
        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);

        if (enemyAttackInterval.elapsed(Duration.seconds(5))) {
            if (alerted) {
                basicEnemyAttack(player);
                enemyAttackInterval.capture();
                System.out.println("test");
            }
        }

        if (distanceToPlayer(player) < 600) {
            alerted = true;
        } else {
            alerted = false;
        }
    }

    public double distanceToPlayer(Entity player) {
        return player.getPosition().distance(getEntity().getPosition());
    }

    public void basicEnemyAttack(Entity player) {
        FXGL.getGameWorld().spawn("enemyBullet", new SpawnData(entity.getPosition()).put("direction", player.getPosition().subtract(entity.getPosition())));
    }
}
