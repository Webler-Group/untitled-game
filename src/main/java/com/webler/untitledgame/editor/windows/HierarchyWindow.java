package com.webler.untitledgame.editor.windows;

import com.webler.goliath.core.GameObject;
import com.webler.goliath.core.Scene;
import com.webler.untitledgame.editor.EditorComponent;
import com.webler.untitledgame.editor.controllers.EditorController;
import com.webler.untitledgame.level.levelmap.Entity;
import com.webler.untitledgame.level.levelmap.Light;
import com.webler.untitledgame.level.levelmap.Platform;
import com.webler.untitledgame.level.levelmap.Serializable;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTreeNodeFlags;

import java.nio.file.Path;
import java.util.List;

public class HierarchyWindow {
    private EditorComponent editor;
    private int selectedIdx;

    public HierarchyWindow(EditorComponent editor) {
        this.editor = editor;
        selectedIdx = -1;
    }

    public void imgui() {
        Scene scene = editor.getEntity().getScene();
        String levelName = editor.getCurrentPath() == null ?
                "<Unsaved>" : Path.of(editor.getCurrentPath()).getFileName().toString();
        int i = 0;

        ImGui.setNextWindowSize(300, 150, ImGuiCond.FirstUseEver);

        ImGui.begin("Hierarchy");

        if(ImGui.treeNode(levelName)) {
            imguiLevelObjectNode(scene.getEntityByName("Editor"), i++);
            if(ImGui.treeNode("Platforms")) {
                List<GameObject> platformGameObjects = scene.getEntitiesByTag(Platform.TAG);
                for(GameObject platformGameObject : platformGameObjects) {
                    imguiLevelObjectNode(platformGameObject, i++);
                }
                ImGui.treePop();
            }
            if(ImGui.treeNode("Lights")) {
                List<GameObject> lightGameObjects = scene.getEntitiesByTag(Light.TAG);
                for(GameObject lightGameObject : lightGameObjects) {
                    imguiLevelObjectNode(lightGameObject, i++);
                }
                ImGui.treePop();
            }
            if(ImGui.treeNode("Entities")) {
                List<GameObject> entityGameObjects = scene.getEntitiesByTag(Entity.TAG);
                for(GameObject entityGameObject : entityGameObjects) {
                    imguiLevelObjectNode(entityGameObject, i++);
                }
                ImGui.treePop();
            }
            ImGui.treePop();
        }
        ImGui.end();
    }

    private void imguiLevelObjectNode(GameObject gameObject, int index) {
        EditorController controller = gameObject.getComponent(EditorController.class, "Controller");
        int nodeFlags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
        if(selectedIdx == index) {
            nodeFlags |= ImGuiTreeNodeFlags.Selected;
        }
        ImGui.treeNodeEx(controller.toString(), nodeFlags);
        if(ImGui.isItemClicked()) {
            selectedIdx = index;
            editor.selectGameObject(gameObject);
        }
    }
}