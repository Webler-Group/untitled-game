package com.webler.untitledgame.scenes;

import com.webler.goliath.Game;
import com.webler.goliath.core.GameObject;
import com.webler.goliath.core.Scene;
import com.webler.goliath.core.SceneParams;
import com.webler.goliath.graphics.Spritesheet;
import com.webler.goliath.graphics.components.*;
import com.webler.goliath.utils.AssetPool;
import com.webler.untitledgame.components.GridLines;
import com.webler.untitledgame.components.Level;
import com.webler.untitledgame.editor.Dockspace;
import com.webler.untitledgame.editor.EditorComponent;
import com.webler.untitledgame.editor.EditorConfig;
import com.webler.untitledgame.editor.controllers.LevelMapEditorController;
import imgui.ImGui;

public class LevelEditorScene extends Scene {
    Dockspace dockspace;

    public LevelEditorScene(Game game) {
        super(game);
    }

    @Override
    public void init(SceneParams params) {
        TestParams testParams = (TestParams) params;

        AssetPool.addSpritesheet("assets/spritesheets/tileset.png",
                new Spritesheet(AssetPool.getTexture("assets/spritesheets/tileset.png"),
                        16,
                        16,
                        70,
                        9,
                        0
                )
        );

        GameObject cameraGameObject = new GameObject(this, "Camera");
        Camera camera = new OrthoCamera(1920, 1080);
        cameraGameObject.addComponent("Camera", camera);
        setCamera(camera);
        add(cameraGameObject);

        dockspace = new Dockspace("Dockspace", getGame());

        EditorConfig editorConfig = new EditorConfig(64, 64);
        GameObject editorGameObject = new GameObject(this, "Editor");
        Level level = new Level();
        editorGameObject.addComponent("Editor", new EditorComponent(level, editorConfig, testParams.getLevelPath()));
        editorGameObject.addComponent("GridLines", new GridLines(editorConfig));
        editorGameObject.addComponent("Level", level);
        editorGameObject.addComponent("Controller", new LevelMapEditorController(level));
        add(editorGameObject);
    }



    @Override
    public void imgui() {
        dockspace.begin();
        sceneImgui();
        ImGui.showDemoWindow();
        dockspace.end();
    }

    @Override
    public void draw() {}
}