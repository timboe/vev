package co.uk.timmartin.vev.input;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

import co.uk.timmartin.vev.manager.Sounds;

public class ButtonHover extends FocusListener {
  @Override
  public boolean handle(Event event) {
    Button b = (Button) event.getListenerActor();
    if (!b.isDisabled() && event.getListenerActor() == event.getTarget() && event.toString().equals("enter")) {
      Sounds.getInstance().click();
    }
    return super.handle(event);
  }
}
