package timboe.vev;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.files.FileHandle;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import timboe.vev.enums.Particle;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.IntroState;
import timboe.vev.manager.Persistence;
import timboe.vev.manager.UIIntro;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

import static timboe.vev.enums.Particle.kBlank;
import static timboe.vev.enums.Particle.kE;
import static timboe.vev.enums.Particle.kH;
import static timboe.vev.enums.Particle.kM;
import static timboe.vev.enums.Particle.kQ;
import static timboe.vev.enums.Particle.kW;
import static timboe.vev.enums.Particle.kZ;

public class VEVGame extends Game {







  @Override
  public void create () {
    Persistence.create();
    Lang.create();
    Camera.create();
    Textures.create();
    GameState.create();
    IntroState.create();
    Sounds.create();
    World.create();
    Persistence.getInstance().tryLoadGameSave();
    UIIntro.create();
    UI.create();

    GameState.getInstance().setGame(this);
    GameState.getInstance().setToTitleScreen();
    Sounds.getInstance().doMusic(true);
	}
	
  @Override
  public void dispose () {
    Persistence.getInstance().trySave();
    UIIntro.getInstance().dispose();
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    IntroState.getInstance().dispose();
    GameState.getInstance().dispose();
    Persistence.getInstance().dispose();
  }

}
