package timboe.destructor;

import com.badlogic.gdx.Game;
import timboe.destructor.manager.*;

public class DestructorGame extends Game {

	@Override
	public void create () {
		Textures.getInstance().create();
		Sprites.getInstance().create();
		Sounds.getInstance().create();
		World.getInstance().create();
		GameState.getInstance().create();

		GameState.getInstance().setGame(this);
		GameState.getInstance().setToTitleScreen();
	}
	
	@Override
	public void dispose () {
		Textures.getInstance().dispose();
		Sprites.getInstance().dispose();
		Sounds.getInstance().dispose();
		GameState.getInstance().dispose();
		World.getInstance().dispose();
	}
}
