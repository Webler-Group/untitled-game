package com.webler.untitledgame.editor;

import com.webler.goliath.core.Component;
import com.webler.goliath.core.GameObject;
import com.webler.goliath.core.Scene;
import com.webler.goliath.core.components.Transform;
import com.webler.goliath.graphics.Color;
import com.webler.goliath.graphics.components.Camera;
import com.webler.goliath.graphics.components.SpriteRenderer;
import com.webler.goliath.graphics.widgets.FileBrowser;
import com.webler.goliath.graphics.widgets.FileBrowserAction;
import com.webler.goliath.input.Input;
import com.webler.goliath.math.Rect;
import com.webler.untitledgame.components.Level;
import com.webler.untitledgame.editor.controllers.EditorController;
import com.webler.untitledgame.editor.windows.HierarchyWindow;
import com.webler.untitledgame.editor.windows.InspectorWindow;
import com.webler.untitledgame.editor.windows.LevelWindow;
import com.webler.untitledgame.level.levelmap.*;
import com.webler.untitledgame.prefabs.editor.DoorPrefab;
import com.webler.untitledgame.prefabs.editor.EntityPrefab;
import com.webler.untitledgame.prefabs.editor.PlatformPrefab;
import com.webler.untitledgame.prefabs.editor.LightPrefab;
import com.webler.untitledgame.scenes.TestParams;
import org.joml.Vector2d;
import org.joml.Vector4d;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class EditorComponent extends Component {
    public static final String SELECTABLE_TAG = "selectable";
    private Level level;
    private MenuBar menuBar;
    private HierarchyWindow hierarchyWindow;
    private LevelWindow levelWindow;
    private InspectorWindow inspectorWindow;
    private EditorConfig config;
    private String currentPath;
    private GameObject selectedGameObject;
    private boolean hierarchyWindowOpened;
    private boolean levelWindowOpened;
    private boolean inspectorWindowOpened;
    private FileBrowserAction fileBrowserAction;
    private Vector2d mouseBeginPosition;
    private Vector2d mousePosition;
    private Transform selectedObjectTransform;
    private MouseAction mouseAction;

    public EditorComponent(Level level, EditorConfig config, String currentPath) {
        this.level = level;
        this.config = config;
        this.currentPath = currentPath;
        selectedGameObject = null;
        hierarchyWindowOpened = true;
        levelWindowOpened = true;
        inspectorWindowOpened = true;
        fileBrowserAction = FileBrowserAction.NONE;
        mouseBeginPosition = new Vector2d();
        mousePosition = new Vector2d();
        selectedObjectTransform = null;
        mouseAction = MouseAction.MOVE;
    }

    @Override
    public void start() {
        menuBar = new MenuBar(this);
        hierarchyWindow = new HierarchyWindow(this);
        levelWindow = new LevelWindow(this);
        inspectorWindow = new InspectorWindow(this);
        if(currentPath != null) {
            loadLevel();
        }
    }

    @Override
    public void update(double dt) {
        handleInput();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void imgui() {

        menuBar.imgui();

        if(hierarchyWindowOpened) {
            hierarchyWindow.imgui();
        }

        if(levelWindowOpened) {
            levelWindow.imgui();
        }

        if(inspectorWindowOpened) {
            inspectorWindow.imgui();
        }

        switch (fileBrowserAction) {
            case OPEN:
                FileBrowser.open(FileBrowserAction.OPEN);
                break;
            case SAVE:
                FileBrowser.open(FileBrowserAction.SAVE);
        }
        fileBrowserAction = FileBrowserAction.NONE;

        if(FileBrowser.getModal()) {
            if(FileBrowser.getResultPath() != null) {
                switch (FileBrowser.getAction()) {
                    case OPEN:
                        currentPath = FileBrowser.getResultPath();
                        loadLevel();
                        break;
                    case SAVE:
                        currentPath = FileBrowser.getResultPath();
                        saveLevel();
                        break;
                }

            }
        }
    }

    public void handlePlay() {
        if(currentPath != null) {
            saveLevel();
            getEntity().getGame().playScene("LevelScene", new TestParams(currentPath));
        }
    }

    public void handleOpen() {
        fileBrowserAction = FileBrowserAction.OPEN;
    }

    public void handleNew() {

    }

    public void handleSave() {
        if(currentPath == null) {
            fileBrowserAction = FileBrowserAction.SAVE;
        } else {
            saveLevel();
        }
    }

    public void handleSaveAs() {
        fileBrowserAction = FileBrowserAction.SAVE;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void addPlatform() {
        Scene scene = getEntity().getScene();
        int x = (int)(scene.getCamera().getEntity().transform.position.x / config.gridWidth());
        int y = (int)(scene.getCamera().getEntity().transform.position.y / config.gridHeight());
        GameObject platformGameObject = new PlatformPrefab(new Platform(x, y, 1, 1, 0, level.getLevelMap().ceiling),
                config.gridWidth(), config.gridHeight()).create(scene);
        scene.add(platformGameObject);
    }

    public void addSpotLight() {
        Scene scene = getEntity().getScene();
        double x = scene.getCamera().getEntity().transform.position.x / config.gridWidth();
        double y = scene.getCamera().getEntity().transform.position.y / config.gridHeight();
        GameObject spotLightGameObject = new LightPrefab(new Light(x, y, 0.5, 5, 10, Color.WHITE),
                config.gridWidth(), config.gridHeight()).create(scene);
        scene.add(spotLightGameObject);
    }

    public void addEntity(String name) {
        Scene scene = getEntity().getScene();
        double x = scene.getCamera().getEntity().transform.position.x / config.gridWidth();
        double y = scene.getCamera().getEntity().transform.position.y / config.gridHeight();
        GameObject entity = new EntityPrefab(new Entity(name, x, y),
                config.gridWidth(), config.gridHeight()).create(scene);
        scene.add(entity);
    }

    public void addDoor() {
        Scene scene = getEntity().getScene();
        int x = (int)(scene.getCamera().getEntity().transform.position.x / config.gridWidth());
        int y = (int)(scene.getCamera().getEntity().transform.position.y / config.gridHeight());
        GameObject door = new DoorPrefab(new Door(x, y, Direction.DOWN),
                config.gridWidth(), config.gridHeight()).create(scene);
        scene.add(door);
    }

    public GameObject getSelectedGameObject() {
        return selectedGameObject;
    }

    public void selectGameObject(GameObject gameObject) {
        selectedGameObject = gameObject;
    }

    public boolean isHierarchyWindowOpened() {
        return hierarchyWindowOpened;
    }

    public boolean isLevelWindowOpened() {
        return levelWindowOpened;
    }

    public boolean isInspectorWindowOpened() {
        return inspectorWindowOpened;
    }

    public void setHierarchyWindowOpened(boolean hierarchyWindowOpened) {
        this.hierarchyWindowOpened = hierarchyWindowOpened;
    }

    public void setInspectorWindowOpened(boolean inspectorWindowOpened) {
        this.inspectorWindowOpened = inspectorWindowOpened;
    }

    public void setLevelWindowOpened(boolean levelWindowOpened) {
        this.levelWindowOpened = levelWindowOpened;
    }

    private void loadLevel() {
        Scene scene = gameObject.getScene();

        level.load(currentPath);

        clearLevel();

        List<Platform> platforms = level.getLevelMap().getPlatforms();
        for(Platform platform : platforms) {
            GameObject platformGameObject = new PlatformPrefab(platform,
                    config.gridWidth(), config.gridHeight()).create(scene);
            scene.add(platformGameObject);
        }

        List<Light> lights = level.getLevelMap().getLights();
        for(Light light : lights) {
            GameObject lightGameObject = new LightPrefab(light,
                    config.gridWidth(), config.gridHeight()).create(scene);
            scene.add(lightGameObject);
        }

        List<Entity> entities = level.getLevelMap().getEntities();
        for(Entity entity : entities) {
            GameObject entityGameObject = new EntityPrefab(entity,
                    config.gridWidth(), config.gridHeight()).create(scene);
            scene.add(entityGameObject);
        }

        List<Door> doors = level.getLevelMap().getDoors();
        for(Door door : doors) {
            GameObject doorGameObject = new DoorPrefab(door,
                    config.gridWidth(), config.gridHeight()).create(scene);
            scene.add(doorGameObject);
        }
    }

    private void saveLevel() {
        Scene scene = gameObject.getScene();

        level.getLevelMap().clear();

        List<GameObject> platformObjects = scene.getEntitiesByTag(Platform.TAG);
        for(GameObject platformObject : platformObjects) {
            Platform platform = (Platform) platformObject
                    .getComponent(EditorController.class, "Controller")
                    .getSerializable();
            level.getLevelMap().addPlatform(platform);
        }

        List<GameObject> lightObjects = scene.getEntitiesByTag(Light.TAG);
        for(GameObject lightObject : lightObjects) {
            Light light = (Light) lightObject
                    .getComponent(EditorController.class, "Controller")
                    .getSerializable();
            level.getLevelMap().addLight(light);
        }

        List<GameObject> entityObjects = scene.getEntitiesByTag(Entity.TAG);
        for(GameObject entityObject : entityObjects) {
            Entity entity = (Entity) entityObject
                    .getComponent(EditorController.class, "Controller")
                    .getSerializable();
            level.getLevelMap().addEntity(entity);
        }

        List<GameObject> doorObjects = scene.getEntitiesByTag(Door.TAG);
        for(GameObject doorObject : doorObjects) {
            Door door = (Door) doorObject
                    .getComponent(EditorController.class, "Controller")
                    .getSerializable();
            level.getLevelMap().addDoor(door);
        }

        level.save(currentPath);
    }

    private void handleInput() {
        Camera camera = gameObject.getScene().getCamera();
        Vector2d worldMousePosition = getWorldMousePosition();

        if(Input.mouseButtonBeginPress(GLFW_MOUSE_BUTTON_LEFT) ||
                Input.mouseButtonBeginPress(GLFW_MOUSE_BUTTON_RIGHT)
        ) {
            mouseBeginPosition.set(worldMousePosition);
        }
        mousePosition.set(worldMousePosition);

        if(Input.mouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            camera.getEntity().transform.position.x -= mousePosition.x - mouseBeginPosition.x;
            camera.getEntity().transform.position.y -= mousePosition.y - mouseBeginPosition.y;
        }

        GameObject hoveredGameObject = getHoveredGameObject();
        if(Input.mouseButtonBeginPress(GLFW_MOUSE_BUTTON_LEFT)) {

            selectedGameObject = hoveredGameObject;
            if(selectedGameObject != null) {
                selectedObjectTransform = new Transform(selectedGameObject.transform);
                if(Input.keyPressed(GLFW_KEY_S)) {
                    mouseAction = MouseAction.SCALE;
                } else {
                    mouseAction = MouseAction.MOVE;
                }
            }
        }

        if(selectedGameObject != null) {
            if(Input.mouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                switch(mouseAction) {
                    case SCALE:
                        scaleSelectedObject();
                        break;
                    case MOVE:
                        moveSelectedObject();
                        break;
                }
            }
        }
    }

    private void moveSelectedObject() {
        if(selectedGameObject != null) {
            Vector2d vector = getWorldMousePosition().sub(mouseBeginPosition);
            selectedGameObject.getComponent(EditorController.class, "Controller").move(selectedObjectTransform, mouseBeginPosition, vector);
        }
    }

    private void scaleSelectedObject() {
        if(selectedGameObject != null) {
            Vector2d vector = getWorldMousePosition().sub(mouseBeginPosition);
            selectedGameObject.getComponent(EditorController.class, "Controller").scale(selectedObjectTransform, mouseBeginPosition, vector);
        }
    }

    private GameObject getHoveredGameObject() {
        Vector2d worldMousePosition = getWorldMousePosition();
        List<GameObject> selectableObjects = gameObject.getScene().getEntitiesByTag(SELECTABLE_TAG);
        GameObject hoveredGameObject = null;
        int zIndex = 0;
        for(GameObject go : selectableObjects) {
            SpriteRenderer renderer = go.getComponent(SpriteRenderer.class, "Renderer");
            if(renderer.getBoundingRect().contains(worldMousePosition) && (hoveredGameObject == null || zIndex <= renderer.getzIndex())) {
                hoveredGameObject = go;
                zIndex = renderer.getzIndex();
            }
        }
        return hoveredGameObject;
    }

    private Vector2d getWorldMousePosition() {
        Camera camera = gameObject.getScene().getCamera();
        Rect levelViewport = levelWindow.getLevelViewport();
        Vector4d v = new Vector4d((Input.mouseX() - levelViewport.x) / levelViewport.width * 2 - 1,
                (1 - (Input.mouseY() - levelViewport.y) / levelViewport.height) * 2 - 1, 0.0, 1.0);
        v.mul(camera.getInversePVMatrix());
        return new Vector2d(v.x, v.y);
    }

    private void clearLevel() {
        Scene scene = gameObject.getScene();

        List<GameObject> platformsToRemove = scene.getEntitiesByTag(Platform.TAG);
        for(GameObject platform : platformsToRemove) {
            scene.remove(platform);
        }

        List<GameObject> lightsToRemove = scene.getEntitiesByTag(Light.TAG);
        for(GameObject light : lightsToRemove) {
            scene.remove(light);
        }

        List<GameObject> entitiesToRemove = scene.getEntitiesByTag(Entity.TAG);
        for(GameObject entity : entitiesToRemove) {
            scene.remove(entity);
        }

        List<GameObject> doorsToRemove = scene.getEntitiesByTag(Door.TAG);
        for(GameObject door : doorsToRemove) {
            scene.remove(door);
        }
    }

    private enum MouseAction {
        MOVE, SCALE
    }
}
