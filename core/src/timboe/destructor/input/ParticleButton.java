package timboe.destructor.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import timboe.destructor.enums.Colour;
import timboe.destructor.manager.GameState;

/**
 * Created by Tim on 01/01/2018.
 */

public class ParticleButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Colour c = (Colour)actor.getUserObject();
    GameState.getInstance().reduceSelectedSet(c, Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
  }

}
