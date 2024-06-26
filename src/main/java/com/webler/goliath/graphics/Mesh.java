package com.webler.goliath.graphics;

import lombok.Getter;
import org.joml.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private static final int POS_INDEX = 0;
    private static final int POS_SIZE = 3;
    private static final int POS_OFFSET = 0;
    private static final int UV_INDEX = 1;
    private static final int UV_SIZE = 2;
    private static final int UV_OFFSET = 3;
    private static final int NORMAL_INDEX = 2;
    private static final int NORMAL_SIZE = 3;
    private static final int NORMAL_OFFSET = 5;
    private static final int VERT_SIZE = POS_SIZE + UV_SIZE + NORMAL_SIZE;
    private final Geometry geometry;
    private int vao;
    private int vbo;
    private int ebo;
    @Getter
    private final Matrix4d modelMatrix;
    @Getter
    private final Vector4d color;

    public Mesh(Geometry geometry) {
        this.geometry = geometry;
        modelMatrix = new Matrix4d().identity();
        color = new Vector4d(1, 1, 1, 1);
        init();
    }

    /**
    * Initializes OpenGL state. Called by #create () to initialize the OpenGL state before drawing is started
    */
    private void init() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, geometry.getVertices(), GL_STATIC_DRAW);
        glVertexAttribPointer(POS_INDEX,
                POS_SIZE,
                GL_FLOAT,
                false,
                VERT_SIZE * Float.BYTES,
                POS_OFFSET * Float.BYTES);
        glVertexAttribPointer(UV_INDEX,
                UV_SIZE,
                GL_FLOAT,
                false,
                VERT_SIZE * Float.BYTES,
                UV_OFFSET * Float.BYTES);
        glVertexAttribPointer(NORMAL_INDEX,
                NORMAL_SIZE,
                GL_FLOAT,
                false,
                VERT_SIZE * Float.BYTES,
                NORMAL_OFFSET * Float.BYTES);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, geometry.getIndices(), GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    /**
    * Destroys OpenGL resources. This is called by #destroy ( GLContext ) when the context is no longer needed
    */
    public void destroy() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }

    /**
    * Renders the geometry to the VAO. This is called by GLUT every frame to render the geometry
    */
    public void render() {
        glBindVertexArray(vao);

        glEnableVertexAttribArray(POS_INDEX);
        glEnableVertexAttribArray(UV_INDEX);
        glEnableVertexAttribArray(NORMAL_INDEX);

        DrawCall[] drawCalls = geometry.getDrawCalls();
        for (DrawCall drawCall : drawCalls) {
            glBindTexture(GL_TEXTURE_2D, drawCall.texId());
            glDrawElements(GL_TRIANGLES, drawCall.count(), GL_UNSIGNED_INT, (long) drawCall.offset() * Integer.BYTES);
        }

        glDisableVertexAttribArray(POS_INDEX);
        glDisableVertexAttribArray(UV_INDEX);
        glDisableVertexAttribArray(NORMAL_INDEX);

        glBindVertexArray(0);
    }

}
