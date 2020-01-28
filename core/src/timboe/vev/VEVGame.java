package timboe.vev;

import com.badlogic.gdx.Game;

import timboe.vev.enums.FSM;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Persistence;
import timboe.vev.manager.StateManager;
import timboe.vev.manager.UIIntro;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

public class VEVGame extends Game {


  @Override
  public void create () {
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
  public void dispose () {
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
