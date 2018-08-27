package timboe.destructor.DistanceField;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import timboe.destructor.DistanceField.LabelDF;
import timboe.destructor.manager.UI;


/** A button with a child {@link Label} to display text.
 * @author Nathan Sweet
 * Changed to use LabelDF*/

public class TextButtonDF extends Button {
  private final LabelDF label;
  private com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle style;

  public TextButtonDF (String text, Skin skin) {
    this(text, skin.get(com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class));
    setSkin(skin);
  }

  public TextButtonDF (String text, Skin skin, String styleName) {
    this(text, skin.get(styleName, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle.class));
    setSkin(skin);
  }

  public TextButtonDF (String text, com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle style) {
    super();
    setStyle(style);
    this.style = style;
    label = new LabelDF(text,  new LabelStyle(style.font, style.fontColor), UI.getInstance().dfShader_medium);
    label.setAlignment(Align.center);
    add(label).expand().fill();
    setSize(getPrefWidth(), getPrefHeight());
  }

  public void setStyle (ButtonStyle style) {
    if (style == null) throw new NullPointerException("style cannot be null");
    if (!(style instanceof com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle.");
    super.setStyle(style);
    this.style = (com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle)style;
    if (label != null) {
      com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle textButtonStyle = (com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle)style;
      LabelStyle labelStyle = label.getStyle();
      labelStyle.font = textButtonStyle.font;
      labelStyle.fontColor = textButtonStyle.fontColor;
      label.setStyle(labelStyle);
    }
  }

  public com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle getStyle () {
    return style;
  }

  public void draw (Batch batch, float parentAlpha) {
    Color fontColor;
    if (isDisabled() && style.disabledFontColor != null)
      fontColor = style.disabledFontColor;
    else if (isPressed() && style.downFontColor != null)
      fontColor = style.downFontColor;
    else if (isChecked() && style.checkedFontColor != null)
      fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style.checkedFontColor;
    else if (isOver() && style.overFontColor != null)
      fontColor = style.overFontColor;
    else
      fontColor = style.fontColor;
    if (fontColor != null) label.getStyle().fontColor = fontColor;
    super.draw(batch, parentAlpha);
  }

  public Label getLabel () {
    return label;
  }

  public Cell<LabelDF> getLabelCell () {
    return getCell(label);
  }

  public void setText (String text) {
    label.setText(text);
  }

  public CharSequence getText () {
    return label.getText();
  }
}
