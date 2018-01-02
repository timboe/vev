package timboe.destructor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Json;

import timboe.destructor.manager.*;

public class DestructorGame extends Game {

	@Override
	public void create () {
		Camera.getInstance().create();
		GameState.getInstance().create();
		Textures.getInstance().create();
		Sounds.getInstance().create();
		World.getInstance().create();
		UI.getInstance().create();

		GameState.getInstance().setGame(this);
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
