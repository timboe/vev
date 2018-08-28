package timboe.vev;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

public class VEVGame extends Game {

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
    objectOutputStream.writeObject(World.getInstance().getTile(0,0));
    objectOutputStream.flush();
    objectOutputStream.close();
  }
}
