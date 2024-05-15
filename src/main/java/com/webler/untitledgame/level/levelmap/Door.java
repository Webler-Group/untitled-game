package com.webler.untitledgame.level.levelmap;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

@Setter
@Getter
public class Door implements Serializable {
    public static final String TAG = "door";
    private int x, y;
    private Direction direction;

    public Door(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public Door() {}

    @Override
    public void deserialize(Element element) {
        int x = Integer.parseInt(element.getAttribute("x"));
        int y = Integer.parseInt(element.getAttribute("y"));
        Direction direction = Direction.valueOf(element.getAttribute("direction"));

        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    @Override
    public void serialize(Element element) {
        element.setAttribute("x", String.valueOf(x));
        element.setAttribute("y", String.valueOf(y));
        element.setAttribute("direction", String.valueOf(direction));
    }
}
