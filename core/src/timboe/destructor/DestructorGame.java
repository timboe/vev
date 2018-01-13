package timboe.destructor;

import com.badlogic.gdx.Game;

import timboe.destructor.manager.*;

public class DestructorGame extends Game {

  @Override
  public void create () {
    Camera.create();
    Textures.create();
    GameState.create();
    Sounds.create();
    World.create();
    UI.create();

    GameState.getInstance().setGame(this);
    World.getInstance().generate();
    GameState.getInstance().setToGameScreen();
	}
	
  @Override
  public void dispose () {
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    GameState.getInstance().dispose();
  }
}
