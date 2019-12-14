package timboe.vev;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import timboe.vev.entity.Entity;
import timboe.vev.enums.Particle;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
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

  private void settingsDefaults() {
    Param.MUSIC_LEVEL = 1f;
    Param.SFX_LEVEL = 1f;
    Param.PARTICLE_HUE.put(kH, 7);
    Param.PARTICLE_HUE.put(kW, 7);
    Param.PARTICLE_HUE.put(kZ, 7);
    Param.PARTICLE_HUE.put(kE, 7);
    Param.PARTICLE_HUE.put(kM, 7);
    Param.PARTICLE_HUE.put(kQ, 7);
    Param.PARTICLE_HUE.put(kBlank, 7);
  }

  private void tryLoadSetting() {
    if (!Gdx.files.isLocalStorageAvailable()) {
      settingsDefaults();
      return;
    }

    FileHandle settings = Gdx.files.local(Param.SETTINGS_FILE);
    if (settings.exists()) {
      try {
        JSONObject json = new JSONObject(settings.readString());
        Param.MUSIC_LEVEL = (float)json.getDouble("music");
        Param.SFX_LEVEL = (float)json.getDouble("sfx");
        JSONObject hue = json.getJSONObject("hue");
        for (Particle p : Particle.values()) {
          Param.PARTICLE_HUE.put(p, hue.getInt(p.name()));
        }
      } catch (JSONException e) {
        e.printStackTrace();
        settingsDefaults();
      }
    } else {
      settingsDefaults();
    }
  }

  private void tryLoad() {
    if (!Gdx.files.isLocalStorageAvailable()) {
      return;
    }

    FileHandle handle = Gdx.files.local(Param.SAVE_FILE);
    if (handle.exists()) {
      ByteArrayInputStream bais = new ByteArrayInputStream(handle.readBytes());
      GZIPInputStream gzipIn;
      try {
        gzipIn = new GZIPInputStream(bais);
        ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
        World.getInstance().loadedSave = new JSONObject((String) objectIn.readObject());
        objectIn.close();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private void trySave() {
    if (!Gdx.files.isLocalStorageAvailable()) return;

    Gdx.app.log("trySave", "SAVE SETTINGS");
    FileHandle settings = Gdx.files.local(Param.SETTINGS_FILE);
    JSONObject jsonSettings = new JSONObject();
    try {
      jsonSettings.put("music", Param.MUSIC_LEVEL);
      jsonSettings.put("sfx", Param.SFX_LEVEL);
      JSONObject hue = new JSONObject();
      for (Particle p : Particle.values()) {
        hue.put(p.name(), Param.PARTICLE_HUE.get(p));
      }
      jsonSettings.put("hue", hue);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    settings.writeString(jsonSettings.toString(), false);

    if (GameState.getInstance().isGameOn()) {
      Gdx.app.log("trySave", "SAVING");
      FileHandle handle = Gdx.files.local(Param.SAVE_FILE);
      JSONObject json = new JSONObject();
      try {
        json.put("GameState", GameState.getInstance().serialise());
        json.put("World", World.getInstance().serialise());
      } catch (JSONException e) {
        e.printStackTrace();
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzipOut = null;
      try {
        gzipOut = new GZIPOutputStream(baos);
        ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
        objectOut.writeObject(json.toString());
        objectOut.close();
        baos.writeTo(handle.write(false));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void create () {

    tryLoadSetting();
    Camera.create();
    Textures.create();
    GameState.create();
    Sounds.create();
    World.create();
    tryLoad();
    UI.create();

    GameState.getInstance().setGame(this);
    GameState.getInstance().setToTitleScreen();
    Sounds.getInstance().doMusic(true);
	}
	
  @Override
  public void dispose () {
    trySave();
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    GameState.getInstance().dispose();
  }

}
