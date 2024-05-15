package com.webler.untitledgame.level.controllers;

import com.webler.goliath.colliders.BoxCollider3D;
import com.webler.goliath.core.GameObject;
import com.webler.goliath.core.Scene;
import com.webler.goliath.dialogs.DialogOption;
import com.webler.goliath.dialogs.components.DialogManager;
import com.webler.goliath.dialogs.events.DialogEndedEvent;
import com.webler.goliath.dialogs.events.DialogNextEvent;
import com.webler.goliath.dialogs.nodes.DialogOptionsNode;
import com.webler.goliath.dialogs.nodes.DialogTextNode;
import com.webler.goliath.eventsystem.listeners.EventHandler;
import com.webler.goliath.graphics.Color;
import com.webler.goliath.graphics.DebugDraw;
import com.webler.goliath.graphics.canvas.Canvas;
import com.webler.goliath.graphics.components.Camera;
import com.webler.goliath.input.Input;
import com.webler.goliath.math.MathUtils;
import com.webler.untitledgame.components.Level;
import com.webler.untitledgame.components.PathFinder;
import com.webler.untitledgame.level.events.DoorOpenedEvent;
import com.webler.untitledgame.level.events.ItemSelectedEvent;
import com.webler.untitledgame.level.inventory.Inventory;
import com.webler.untitledgame.prefabs.level.GunPrefab;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3d;

import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController extends EntityController {
    private Camera camera;
    private boolean canJump;
    @Getter
    private GameObject focusedObject;
    private State state;
    private Inventory inventory;
    @Setter
    @Getter
    private GameObject companion;
    private GameObject gun;
    private boolean shouldStopInteraction;

    public PlayerController(Level level, Camera camera, BoxCollider3D collider, Inventory inventory, PathFinder pathFinder) {
        super(level, collider, new String[]{ "npc", "fixed" }, pathFinder, 80);
        this.camera = camera;
        this.inventory = inventory;
        bounciness = 0;
        canJump = true;
        focusedObject = null;
        state = State.PLAYING;
        companion = null;
        gun = null;
        shouldStopInteraction = false;
    }

    public void collect(String itemName) {
        inventory.add(itemName);
        stopInteraction();
    }

    public void buy(String itemName, int price) {
        if(inventory.getItemCount("gold") >= price) {
            for (int i = 0; i < price; i++) {
                inventory.remove("gold");
            }
            inventory.add(itemName);
        }
    }

    public void equipGun(String itemName) {
        Scene scene = gameObject.getScene();
        boolean isSame = false;
        if(gun != null) {
            GunController gunController = gun.getComponent(GunController.class, "Controller");
            isSame = gunController.getItemName().equals(itemName);
            gun.remove();
        }
        if(!isSame) {
            gun = new GunPrefab(level, itemName).create(scene);
            scene.add(gun);
        } else {
            gun = null;
        }
    }

    @Override
    public void start() {
        level.addObjectToGroup(gameObject, "player");

        focusedObject = gameObject;
        startInteraction();
        level.getComponent(DialogManager.class, "DialogManager")
                .showDialog(new DialogTextNode("system__learn_controls",
                        new DialogOptionsNode(new DialogOption[]{
                                new DialogOption("player__no", false, null),
                                new DialogOption("player__yes", false,
                                        new DialogTextNode("system__controls_move",
                                                new DialogTextNode("system__controls_interact",
                                                        new DialogTextNode("system__controls_inventory", null))))
                        })));
    }

    @Override
    public void update(double dt) {
        switch (state) {
            case PLAYING:
                updatePlaying(dt);
                break;
            case INTERACTING:
                updateInteracting(dt);
                break;
            case LOOKING_INVENTORY:
                updateLookingInventory(dt);
                break;
        }
        updateGun();

        if(shouldStopInteraction) {
            state = State.PLAYING;
            shouldStopInteraction = false;
        }

        Input.setCursorLocked(state == State.PLAYING);

        friction = onGround ? 10 : 2;
        updatePhysics(dt);
    }

    @Override
    public void destroy() {
        level.removeObjectFromGroup(gameObject, "player");
    }

    @EventHandler
    public void onDialogEnded(DialogEndedEvent event) {
        stopInteraction();
    }

    @EventHandler
    public void onDialogNext(DialogNextEvent event) {
        if (event.getDialogName().equals("player__give_coffee")) {
            inventory.remove("caffe_latte");
        }
    }

    @EventHandler
    public void onDoorOpened(DoorOpenedEvent event) {
        if(event.getDoor() == focusedObject) {
            stopInteraction();
        }
    }

    @EventHandler
    public void onItemSelected(ItemSelectedEvent event) {
        switch (event.getItemName()) {
            case "ak47", "shotgun": {
                equipGun(event.getItemName());
                break;
            }
        }
    }

    private void startInteraction() {
        if(focusedObject != null) {
            state = State.INTERACTING;
            Controller controller = focusedObject.getComponent(Controller.class, "Controller");
            controller.interact();
        }
    }

    private void stopInteraction() {
        shouldStopInteraction = true;
    }

    private void updatePlaying(double dt) {

        if(gun != null) {
            Canvas canvas = gameObject.getGame().getCanvas();
            canvas.setColor(Color.WHITE);
            canvas.rect((float) canvas.getWidth() / 2 - 16, (float) canvas.getHeight() / 2 - 1, 32, 2);
            canvas.rect((float) canvas.getWidth() / 2 - 1, (float) canvas.getHeight() / 2 - 16, 2, 32);
        }

        Vector3d moveVec = new Vector3d();

        if(Input.keyPressed(GLFW_KEY_W)) {
            moveVec.x += 1;
        }
        if(Input.keyPressed(GLFW_KEY_S)) {
            moveVec.x -= 1;
        }
        if(Input.keyPressed(GLFW_KEY_A)) {
            moveVec.z -= 1;
        }
        if(Input.keyPressed(GLFW_KEY_D)) {
            moveVec.z += 1;
        }
        moveVec.mul(speed * (onGround ? 1 : 0.3));

        if(Input.keyPressed(GLFW_KEY_LEFT_SHIFT) && onGround && moveVec.x > 0) {
            moveVec.x *= 2;
        }

//        if(Input.keyPressed(GLFW_KEY_A)) {
//            yaw += rotationSpeed * dt;
//        }
//        if(Input.keyPressed(GLFW_KEY_D)) {
//            yaw -= rotationSpeed * dt;
//        }

        yaw += Input.mouseDeltaX() * -0.005;
        pitch = MathUtils.clamp(pitch + Input.mouseDeltaY() * 0.005, -1.5, 1.5);

        moveVec.rotateY(yaw);
        acceleration.set(moveVec);

        if(Input.keyPressed(GLFW_KEY_SPACE) && onGround && canJump) {
            velocity.y = 15;
            onGround = false;
            canJump = false;
        }
        if(!Input.keyPressed(GLFW_KEY_SPACE)) {
            canJump = true;
        }

        if(Input.keyBeginPress(GLFW_KEY_E)) {
            startInteraction();
        }

        if(Input.keyBeginPress(GLFW_KEY_TAB) || Input.keyBeginPress(GLFW_KEY_I)) {
            state = State.LOOKING_INVENTORY;
            inventory.setOpened(true);
        }

        updateCamera(dt);

        updateFocusedObject();
    }

    private void updateInteracting(double dt) {
        acceleration.x = 0;
        acceleration.z = 0;

        if(gameObject != focusedObject) {
            Vector3d focusPosition = focusedObject.getComponent(Controller.class, "Controller").getFocusPosition();

            camera.getGameObject().transform.position.set(new Vector3d(gameObject.transform.position).add(0, 0.75, 0));

            camera.direction.lerp(new Vector3d(focusPosition).sub(camera.getGameObject().transform.position), Math.min(dt * 10, 1));
        } else {
            updateCamera(dt);
        }
    }

    private void updateLookingInventory(double dt) {
        acceleration.x = 0;
        acceleration.z = 0;

        if(Input.keyBeginPress(GLFW_KEY_TAB) || Input.keyBeginPress(GLFW_KEY_ESCAPE) || Input.keyBeginPress(GLFW_KEY_I)) {
            inventory.setOpened(false);
            state = State.PLAYING;
        }

        updateCamera(dt);
    }

    private void updateCamera(double dt) {
        Vector3d direction = new Vector3d(0, 0, 1);
        direction.rotateX(pitch);
        direction.rotateY(yaw + Math.PI / 2);

        camera.getGameObject().transform.position.set(new Vector3d(gameObject.transform.position).add(0, 0.75, 0));
//        camera.direction.lerp(direction, Math.min(dt * 10, 1));
        camera.direction.set(direction);

        if(gun != null) {
            gun.transform.position.set(gameObject.transform.position);
            gun.transform.position.add(new Vector3d(0, 0.25, 1).rotateY(yaw));

            GunController gunController = gun.getComponent(GunController.class, "Controller");
            Vector3d rayHitPos = raycast(camera.getGameObject().transform.position, direction, 50, 0.5);

            if(rayHitPos != null) {
                double rayLength = rayHitPos.distance(camera.getGameObject().transform.position);
                double minRayLength = 8;

                Vector3d gunDirection = new Vector3d(rayHitPos).sub(gunController.getProjectilePosition().sub(new Vector3d(direction).mul(Math.max(minRayLength - rayLength, 0)))).normalize();
                gunController.yaw = Math.atan2(-gunDirection.z, gunDirection.x);
                gunController.pitch = Math.asin(-gunDirection.y);

                DebugDraw.get().addLine(new Vector3d(rayHitPos.x - 1, rayHitPos.y, rayHitPos.z), new Vector3d(rayHitPos.x + 1, rayHitPos.y, rayHitPos.z), Color.YELLOW);
                DebugDraw.get().addLine(new Vector3d(rayHitPos.x, rayHitPos.y - 1, rayHitPos.z), new Vector3d(rayHitPos.x, rayHitPos.y + 1, rayHitPos.z), Color.YELLOW);
                DebugDraw.get().addLine(new Vector3d(rayHitPos.x, rayHitPos.y, rayHitPos.z - 1), new Vector3d(rayHitPos.x, rayHitPos.y, rayHitPos.z + 1), Color.YELLOW);
            } else {
                gunController.yaw = yaw;
                gunController.pitch = pitch;
            }
        }
    }

    private void updateGun() {
        if(gun != null) {

            GunController gunController = gun.getComponent(GunController.class, "Controller");
            if(Input.mouseButtonBeginPress(GLFW_MOUSE_BUTTON_LEFT)  && state == State.PLAYING) {
                gunController.setShooting(true);
            } else if(!Input.mouseButtonPress() || state != State.PLAYING) {
                gunController.setShooting(false);
            }
        }
    }

    private void updateFocusedObject() {
        List<GameObject> focusableObjects = level.getObjectsByGroup("focusable");
        GameObject newFocusedObject = null;
        double currentDistance = 0;

        for(GameObject object : focusableObjects) {
            Controller controller = object.getComponent(Controller.class, "Controller");
            double distance = controller.getCenter().distance(camera.getGameObject().transform.position);
            if(controller.isInFrontOfPlayer() && (newFocusedObject == null || distance < currentDistance)) {
                newFocusedObject = object;
                currentDistance = distance;
            }
        }
        focusedObject = newFocusedObject;
    }

    private enum State {
        INTERACTING, PLAYING, LOOKING_INVENTORY
    }
}
