package game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

@Required(HPComponent.class)
public class PlayerComponent extends Component {

    private AnimatedTexture texture;
    private AnimationChannel animIdle, animIdleUp, animIdleDown, animWalk, animWalkUp, animWalkDown;

    private double aimUpVectorY = BasicGameApp.aimUpVectorY;
    private double aimDownVectorY = BasicGameApp.aimDownVectorY;

    private PhysicsComponent physics;
    private HPComponent hp;
    private int jumps = 2;

    private String[] weaponList = {"default", "shotgun", "machineGun"};
    private int currentWeapon = 0;

    private boolean canFire = true;
    private double defaultCooldown = 0.6;
    private double shotgunCooldown = 1;
    private double machineGunCooldown = 0.10;

    private int shotgunAmmo = BasicGameApp.ammoShotgun;
    private int machineGunAmmo = BasicGameApp.ammoMachineGun;

    private boolean isBeingDamaged = false;

    public PlayerComponent() {

        Image image = FXGL.image("player.png");

        animIdle = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 0, 0);
        animIdleUp = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 1, 1);
        animIdleDown = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 14, 14);

        animWalk = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 2,7);
        animWalkUp = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 8, 13);
        animWalkDown = new AnimationChannel(image, 21, 78, 94, Duration.seconds(1), 15, 20);


        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(32, 47));
        entity.getViewComponent().addChild(texture);

        physics.onGroundProperty().addListener((observableValue, old, isOnGround) -> {
            if (isOnGround)
                jumps = 2;
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (isMoving()) {
            if (texture.getAnimationChannel() != animWalk && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > aimUpVectorY && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < aimDownVectorY) {
                texture.loopAnimationChannel(animWalk);
            }
            else if (texture.getAnimationChannel() != animWalkUp && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < aimUpVectorY) {
                texture.loopAnimationChannel(animWalkUp);
            }
            else if (texture.getAnimationChannel() != animWalkDown && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > aimDownVectorY) {
                texture.loopAnimationChannel(animWalkDown);
            }
        } else {
            if (texture.getAnimationChannel() != animIdle && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > aimUpVectorY && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < aimDownVectorY) {
                texture.loopAnimationChannel(animIdle);
            }
            else if (FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() < aimUpVectorY) {
                texture.loopAnimationChannel(animIdleUp);
            }
            else if (texture.getAnimationChannel() != animIdleDown && FXGL.getInput().getVectorToMouse(entity.getPosition()).getY() > aimDownVectorY) {
                texture.loopAnimationChannel(animIdleDown);
            }
        }
    }

    private boolean isMoving() {
        return physics.isMovingX();
    }

    public void left() {

        getEntity().setScaleX(-1);
        physics.setVelocityX(-300);
    }

    public void right() {

        getEntity().setScaleX(1);
        physics.setVelocityX(300);
    }

    public void jump() {
        if (jumps == 0)
            return;
        physics.setVelocityY(-400);
        jumps--;
    }

    public void stop() {
        physics.setVelocityX(0);
    }

    public void changeWeapon() {
        if (currentWeapon < weaponList.length - 1) {
            currentWeapon++;
            FXGL.inc("weaponIndicatorPosition", 40);
        }
        else {
            currentWeapon = 0;
            FXGL.set("weaponIndicatorPosition", 40);
        }
    }

    public void changeWeaponReverse() {
        if (currentWeapon != 0) {
            currentWeapon--;
            FXGL.inc("weaponIndicatorPosition", -40);
        }
        else {
            currentWeapon = weaponList.length - 1;
            FXGL.set("weaponIndicatorPosition", 40 * weaponList.length);
        }
    }

    public void fire(Point2D aim, Point2D position) {
        if (canFire) {
            switch (weaponList[currentWeapon]) {
                case "default":
                    canFire = false;
                    SpawnData spawnData = new SpawnData(aim).put("direction", aim);
                    spawnData.put("position", position);
                    FXGL.spawn("defaultBullet", spawnData);
                    FXGL.runOnce(() -> {
                        canFire = true;
                        }, Duration.seconds(defaultCooldown));
                    return;
                case "shotgun":
                    if (shotgunAmmo != 0) {
                        canFire = false;
                        shotgunAmmo--;
                        FXGL.set("ammoShotgun", shotgunAmmo);
                        for (int i = 0; i <= 6; i++) {
                            SpawnData spawnDataShotgun = new SpawnData(aim).put("direction", aim.add(Math.random() * 0.1, Math.random() * 0.1));
                            spawnDataShotgun.put("position", position);
                            FXGL.spawn("shotgunPellet", spawnDataShotgun);
                        }
                        FXGL.runOnce(() -> {
                            canFire = true;
                        }, Duration.seconds(shotgunCooldown));
                    }
                    return;
                case "machineGun":
                    if (machineGunAmmo != 0) {
                        machineGunAmmo--;
                        FXGL.set("ammoMachineGun", machineGunAmmo);
                        canFire = false;
                        SpawnData spawnDataMachineGun = new SpawnData(aim).put("direction", aim);
                        spawnDataMachineGun.put("position", position);
                        FXGL.spawn("machineGunBullet", spawnDataMachineGun);
                        FXGL.runOnce(() -> {
                            canFire = true;
                        }, Duration.seconds(machineGunCooldown));
                    }
            }
        }
    }

    public void onHit(int damage) {
        if (isBeingDamaged)
            return;

        hp.setValue(hp.getValue() - damage);

        isBeingDamaged = true;

        FXGL.runOnce(() -> {
            isBeingDamaged = false;
        }, Duration.seconds(1));
    }

    public void restoreHP() {
        hp.setValue(hp.getMaxHP());
    }

    public int getShotgunAmmo() {
        return shotgunAmmo;
    }

    public int getMachineGunAmmo() {
        return machineGunAmmo;
    }




}
