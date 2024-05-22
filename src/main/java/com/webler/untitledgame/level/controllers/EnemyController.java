package com.webler.untitledgame.level.controllers;

import com.webler.goliath.colliders.BoxCollider3D;
import com.webler.untitledgame.components.Level;
import com.webler.untitledgame.components.PathFinder;

public class EnemyController extends EntityController {
    public EnemyController(Level level, BoxCollider3D collider, PathFinder pathFinder, double speed) {
        super(level, collider, new String[] {"fixed", "player"}, pathFinder, speed);
    }

    /**
    * Called when the player starts. This is where we add the enemy and focusable objects to the
    */
    @Override
    public void start() {
        level.addObjectToGroup(gameObject, "enemy");
        level.addObjectToGroup(gameObject, "focusable");
    }

    /**
    * Updates physics based on time. This is called every frame to ensure that we don't accidentally miss any collisions
    * 
    * @param dt - Time since last frame
    */
    @Override
    public void update(double dt) {
        friction = onGround ? 10 : 2.5;
        updatePhysics(dt);
    }

    /**
    * Removes enemy and focusable objects from the level. This is called when the player is no longer in use
    */
    @Override
    public void destroy() {
        level.removeObjectFromGroup(gameObject, "enemy");
        level.removeObjectFromGroup(gameObject, "focusable");
    }

    /**
    * Returns the name of the Enemy. This is used to display the information in the GUI.
    * 
    * 
    * @return a String that represents the name of the Enemy to be displayed in the GUI. The name can be up to 100 characters
    */
    @Override
    public String getName() {
        return "Enemy";
    }
}
