package co.uk.timmartin.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import co.uk.timmartin.vev.manager.UIIntro;

public class FullscreenToggle extends ChangeListener {

  @Override
  public void changed(ChangeEvent event, Actor actor) {
    CheckBox cb = (CheckBox) actor;
    set(cb.isChecked(), true);
  }

  public void toggle() {
    set(!Gdx.graphics.isFullscreen(), false);
  }

  public void set(boolean fs, boolean fromChanged) {
    Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
    if (!fs) {
      Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
    } else {
      Gdx.graphics.setFullscreenMode(currentMode);
    }
    if (!fromChanged && UIIntro.getInstance().fsBox != null) {
      UIIntro.getInstance().fsBox.setChecked(fs);
    }
  }
}
