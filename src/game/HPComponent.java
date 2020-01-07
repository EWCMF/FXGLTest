package game;

import com.almasb.fxgl.entity.components.IntegerComponent;

public class HPComponent extends IntegerComponent {
    private int maxHP;

    public HPComponent(int hp) {
        super(hp);
        maxHP = hp;
    }

    public int getMaxHP() {
        return maxHP;
    }
}
