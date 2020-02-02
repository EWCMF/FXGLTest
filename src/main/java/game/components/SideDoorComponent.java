package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import game.RunAndGunFXGLTypes;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class SideDoorComponent extends Component {
    private boolean opened = false;
    private boolean locked = false;
    private AnimatedTexture texture;
    private AnimationChannel animOpen, animClosed, animClosing;
    private String openType;
    private PhysicsComponent physics;
    private Entity trigger;
    private boolean animPlaying;

    public SideDoorComponent() {
        Image image = FXGL.image("sideDoor.png");

        animClosed = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 0, 0);
        animOpen = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 0, 9);
        animClosing = new AnimationChannel(image, 19, 32, 160, Duration.seconds(1), 9, 18);

        texture = new AnimatedTexture(animClosed);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);

        openType = entity.getString("openType");
    }

    public void checkCondition() {
        if (animPlaying)
            return;

        if (opened && !openType.equals("auto") && !locked)
            return;

        if (!opened && openType.equals("auto") && locked)
            return;

        trigger = FXGL.getGameWorld().getEntitiesByType(RunAndGunFXGLTypes.SIDEDOORTRIGGER)
                .stream()
                .filter(e -> e.isColliding(entity))
                .findAny()
                .get();

        List<Entity> actorsOnTrigger = FXGL.getGameWorld().getCollidingEntities(trigger)
                .stream()
                .filter(e -> e.isType(RunAndGunFXGLTypes.PLAYER) || e.isType(RunAndGunFXGLTypes.MOVINGENEMY) || e.isType(RunAndGunFXGLTypes.ELITEMOVINGENEMY))
                .collect(Collectors.toList());

        if (openType.equals("auto") && !opened)
            openDoor();
        else if (openType.equals("auto") && actorsOnTrigger.size() == 0)
            closeDoor();

        if (openType.equals("key")) {
            if (actorsOnTrigger.stream().noneMatch(e -> e.isType(RunAndGunFXGLTypes.PLAYER)))
                return;

            switch (entity.getProperties().getString("neededKey")) {
                case "blue":
                    if (FXGL.getb("hasKeycardBlue"))
                        openDoor();
                    else
                        FXGL.spawn("overheadText", new SpawnData(entity.getPosition().add(0, -50)).put("text", "The door needs a blue keycard to open."));
                    break;
                case "red":
                    if (FXGL.getb("hasKeycardRed"))
                        openDoor();
                    else
                        FXGL.spawn("overheadText", new SpawnData(entity.getPosition().add(0, -50)).put("text", "The door needs a red keycard to open."));
                    break;
                case "yellow":
                    if (FXGL.getb("hasKeycardYellow"))
                        openDoor();
                    else
                        FXGL.spawn("overheadText", new SpawnData(entity.getPosition().add(0, -50)).put("text", "The door needs a yellow keycard to open."));
                    break;
            }
        }
    }

    public void openDoor() {
        opened = true;
        animPlaying = true;
        texture.playAnimationChannel(animOpen);
        FXGL.runOnce(() -> {
            animPlaying = false;
        }, Duration.seconds(1));
        FXGL.runOnce(() -> {
            entity.getComponent(PhysicsComponent.class).getBody().setActive(false);
        }, Duration.seconds(0.5));
    }

    public void closeDoor() {
        opened = false;
        animPlaying = true;
        texture.playAnimationChannel(animClosing);
        FXGL.runOnce(() -> {
            animPlaying = false;
        }, Duration.seconds(1));
        entity.getComponent(PhysicsComponent.class).getBody().setActive(true);
    }

    public boolean isOpened() {
        return opened;
    }

    public void lockDoor() {
        if (opened)
            closeDoor();
        locked = true;
    }
}
