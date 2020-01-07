package timboe.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class FullscreenToggle extends ChangeListener {

  @Override
  public void changed(ChangeEvent event, Actor actor) {
    CheckBox cb = (CheckBox)actor;
    set(cb.isChecked());
  }

  public void set(boolean fs) {
    Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
    if (!fs) {
      Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
    } else {
      Gdx.graphics.setFullscreenMode(currentMode);
    }
  }
}
