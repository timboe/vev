package timboe.destructor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by Tim on 06/01/2018.
 * Use distance-field shader to render label text
 * Causes flush - use sparingly
 * https://stackoverflow.com/questions/36517050/libgdx-custom-shader-for-textbutton-font
 */


public class LabelDF extends Label {
  private ShaderProgram shader;

  public LabelDF(CharSequence text, Skin skin, ShaderProgram shader) {
    super(text, skin);
    this.shader = shader;
  }

  public LabelDF(CharSequence text, Skin skin, String styleName, ShaderProgram shader) {
    super(text, skin, styleName);
    this.shader = shader;
  }

  public LabelDF(CharSequence text, Skin skin, String fontName, Color color, ShaderProgram shader) {
    super(text, skin, fontName, color);
    this.shader = shader;
  }

  public LabelDF(CharSequence text, Skin skin, String fontName, String colorName, ShaderProgram shader) {
    super(text, skin, fontName, colorName);
    this.shader = shader;
  }

  public LabelDF(CharSequence text, LabelStyle style, ShaderProgram shader) {
    super(text, style);
    this.shader = shader;
  }

  public ShaderProgram getShader() {
    return shader;
  }

  public void setShader(ShaderProgram shader) {
    this.shader = shader;
  }

  @Override
  public void draw (Batch batch, float parentAlpha) {
    if (shader != null) batch.setShader(shader);
    super.draw(batch, parentAlpha);
    if (shader != null) batch.setShader(null);
  }
}
