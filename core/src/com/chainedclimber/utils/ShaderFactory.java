package com.chainedclimber.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderFactory {

    public static final String DEFAULT_VERTEX_SHADER = 
        "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
        "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
        "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
        "uniform mat4 u_projTrans;\n" +
        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoords;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
        "   v_color.a = v_color.a * (255.0/254.0);\n" +
        "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
        "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
        "}\n";

    public static final String GRAYSCALE_FRAGMENT_SHADER = 
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoords;\n" +
        "uniform sampler2D u_texture;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "  vec4 texColor = texture2D(u_texture, v_texCoords);\n" +
        "  float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
        "  // Preserve alpha and multiply by vertex color (v_color)\n" +
        "  gl_FragColor = v_color * vec4(gray, gray, gray, texColor.a);\n" +
        "}";

    /**
     * Creates a shader program. If compilation fails, logs the error and returns null
     * (which causes SpriteBatch to use the default shader).
     */
    public static ShaderProgram createShader(String vert, String frag) {
        ShaderProgram shader = new ShaderProgram(vert, frag);
        
        if (!shader.isCompiled()) {
            Gdx.app.error("ShaderFactory", "Failed to compile shader:\n" + shader.getLog());
            // Fallback: return null so SpriteBatch uses default shader
            return null;
        }
        
        if (shader.getLog().length() > 0) {
            Gdx.app.log("ShaderFactory", "Shader compiled with warnings:\n" + shader.getLog());
        }
        
        return shader;
    }

    public static ShaderProgram createGrayscaleShader() {
        return createShader(DEFAULT_VERTEX_SHADER, GRAYSCALE_FRAGMENT_SHADER);
    }
}
