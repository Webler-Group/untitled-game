package com.webler.untitledgame.level.levelmap;

import org.w3c.dom.Element;

public interface Serializable {
    void deserialize(Element element);
    void serialize(Element element);
}
