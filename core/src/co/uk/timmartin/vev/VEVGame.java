package co.uk.timmartin.vev;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import co.uk.timmartin.vev.enums.FSM;
import co.uk.timmartin.vev.manager.Camera;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.IntroState;
import co.uk.timmartin.vev.manager.Persistence;
import co.uk.timmartin.vev.manager.Sounds;
import co.uk.timmartin.vev.manager.StateManager;
import co.uk.timmartin.vev.manager.Textures;
import co.uk.timmartin.vev.manager.UI;
import co.uk.timmartin.vev.manager.UIIntro;
import co.uk.timmartin.vev.manager.World;

public class VEVGame extends Game {


  @Override
  public void create() {
    Persistence.create();
    Lang.create();
    Camera.create();
    Textures.create();
    StateManager.create();
    GameState.create();
    IntroState.create();
    Sounds.create();
    World.create();
    Persistence.getInstance().tryLoadGameSave();
    UIIntro.create();
    UI.create();

    StateManager.getInstance().init(this);
    Sounds.getInstance().doMusic(true);
  }

  @Override
  public void dispose() {
    if (StateManager.getInstance().fsm == FSM.kGAME) {
      // We were closed from the X button / Alt+F4 / Android... etc.
      Persistence.getInstance().trySaveGame();
    }
    UIIntro.getInstance().dispose();
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    IntroState.getInstance().dispose();
    GameState.getInstance().dispose();
    StateManager.getInstance().dispose();
    Persistence.getInstance().dispose(); // Flushes save file (if any) to disk
  }

}
