package co.uk.timmartin.vev.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import co.uk.timmartin.vev.enums.Particle;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.Sounds;

/**
 * Created by Tim on 13/01/2018.
 */

public class ParticleSelectButton extends ChangeListener {
  @Override
  public void changed(ChangeEvent event, Actor actor) {
    Particle p = (Particle) actor.getUserObject();
    boolean invert = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    GameState.getInstance().reduceSelectedSet(p, invert);
    Sounds.getInstance().OK();
  }
}
