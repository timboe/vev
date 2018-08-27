package timboe.destructor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import timboe.destructor.entity.Tile;
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
    GameState.getInstance().setToTitleScreen();
    Sounds.getInstance().doMusic();
	}
	
  @Override
  public void dispose () {
    try {
      persist();
    } catch (IOException ex) {
      Gdx.app.error("persist IO exception",ex.getMessage());
    } catch (ClassNotFoundException ex) {
      Gdx.app.error("persist class exception",ex.getMessage());
    }
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    GameState.getInstance().dispose();
  }

  private void persist() throws IOException, ClassNotFoundException {
    FileOutputStream fileOutputStream = new FileOutputStream("VEV_save.txt");
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
    for (int x = 0; x < Param.TILES_X; ++x ) {
      for (int y = 0; y < Param.TILES_Y; ++y) {
        objectOutputStream.writeObject(World.getInstance().getTile(x,y));
      }
    }
    objectOutputStream.flush();
    objectOutputStream.close();
  }
}
