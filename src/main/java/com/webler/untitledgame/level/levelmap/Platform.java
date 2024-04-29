package com.webler.untitledgame.level.levelmap;

import org.w3c.dom.Element;

public class Platform implements Serializable {
    public static final String TAG = "platform";
    public int x;
    public int y;
    public int width;
    public int height;
    public int top;

    @Override
    public void deserialize(Element platformElement) {
        int width = Integer.parseInt(platformElement.getAttribute("width"));
        int height = Integer.parseInt(platformElement.getAttribute("height"));
        int top = Integer.parseInt(platformElement.getAttribute("top"));
        int x = Integer.parseInt(platformElement.getAttribute("x"));
        int y = Integer.parseInt(platformElement.getAttribute("y"));

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.top = top;
    }

    @Override
    public void serialize(Element platformElement) {
        platformElement.setAttribute("width", Integer.toString(width));
        platformElement.setAttribute("height", Integer.toString(height));
        platformElement.setAttribute("top", Integer.toString(top));
        platformElement.setAttribute("x", Integer.toString(x));
        platformElement.setAttribute("y", Integer.toString(y));
    }

    public Platform(int x, int y, int width, int height, int top) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.top = top;
    }

    public Platform() {
        x = 0;
        y = 0;
        width = 1;
        height = 1;
        top = 0;
    }
}