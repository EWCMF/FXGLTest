package game;

import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;
import static game.BasicGameTypes.*;

public class BasicGameFactory implements EntityFactory {

    @Spawns("playerBullet")
    public Entity newBullet(SpawnData data) {
        return entityBuilder()
                .type(BULLET)
                .at((Point2D) data.get("position"))
                .viewWithBBox(new Rectangle(12,3, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 650))
                .with(new OffscreenCleanComponent())
                .build();
    }

    @Spawns("start")
    public Entity newStart(SpawnData data) {
        return entityBuilder()
                .type(START)
                .from(data)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.setBodyType(BodyType.DYNAMIC);
        physicsComponent.addGroundSensor(new HitBox(BoundingShape.box(48, 94)));

        physicsComponent.setFixtureDef(new FixtureDef().friction(0.0f));
        return entityBuilder()
                .type(PLAYER)
                .from(data)
                //.view(new Rectangle(25, 25, Color.RED))
                .bbox(new HitBox(BoundingShape.box(48, 94)))
                .with(physicsComponent)
                .with(new CollidableComponent(true))
                .with(new PlayerComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        return entityBuilder()
                .type(WALL)
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("target")
    public Entity newTarget(SpawnData data) {
        return entityBuilder()
                .type(TARGET)
                .from(data)
                .viewWithBBox(new Circle(16, 16, 15, Color.BLACK))
                .with(new CollidableComponent(true))
                .build();
    }
}
