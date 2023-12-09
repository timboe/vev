package co.uk.timmartin.vev.DistanceField;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.Value;

import co.uk.timmartin.vev.manager.UI;

/**
 * A tooltip that shows a label.
 *
 * @author Nathan Sweet
 * Extended to use LabelDF
 */

public class TextTooltipDF extends Tooltip<LabelDF> {

  public TextTooltipDF(String text, Skin skin) {
    this(text, TooltipManager.getInstance(), skin.get(TextTooltip.TextTooltipStyle.class));
  }

  public TextTooltipDF(String text, Skin skin, String styleName) {
    this(text, TooltipManager.getInstance(), skin.get(styleName, TextTooltip.TextTooltipStyle.class));
  }

  public TextTooltipDF(String text, TextTooltip.TextTooltipStyle style) {
    this(text, TooltipManager.getInstance(), style);
  }

  public TextTooltipDF(String text, TooltipManager manager, Skin skin) {
    this(text, manager, skin.get(TextTooltip.TextTooltipStyle.class));
  }

  public TextTooltipDF(String text, TooltipManager manager, Skin skin, String styleName) {
    this(text, manager, skin.get(styleName, TextTooltip.TextTooltipStyle.class));
  }

  public TextTooltipDF(String text, final TooltipManager manager, TextTooltip.TextTooltipStyle style) {
    super(null, manager);

    LabelDF label = new LabelDF(text, style.label, UI.getInstance().dfShader);
    label.setWrap(true);

    getContainer().setActor(label);
    getContainer().width(new Value() {
      public float get(Actor context) {
        return Math.min(manager.maxWidth, getContainer().getActor().getGlyphLayout().width);
      }
    });

    setStyle(style);
  }

  public void setStyle(TextTooltip.TextTooltipStyle style) {
    if (style == null) throw new NullPointerException("style cannot be null");
    if (!(style instanceof TextTooltip.TextTooltipStyle))
      throw new IllegalArgumentException("style must be a TextTooltipStyle.");
    getContainer().getActor().setStyle(style.label);
    getContainer().setBackground(style.background);
    getContainer().maxWidth(style.wrapWidth);
  }

}
