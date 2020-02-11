package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import timboe.vev.Param;
import timboe.vev.enums.Particle;

import static timboe.vev.enums.Particle.kBlank;
import static timboe.vev.enums.Particle.kE;
import static timboe.vev.enums.Particle.kH;
import static timboe.vev.enums.Particle.kM;
import static timboe.vev.enums.Particle.kQ;
import static timboe.vev.enums.Particle.kW;
import static timboe.vev.enums.Particle.kZ;

public class Persistence {
  private static Persistence ourInstance = null;

  public static Persistence getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Persistence();
  }

  public static boolean constructed() {
    return ourInstance != null;
  }

  // Persistent
  public float musicLevel;
  public float sfxLevel;
  public EnumMap<Particle, Integer> particleHues = new EnumMap<Particle, Integer>(Particle.class);
  public Vector<Integer> bestTimes = new Vector<Integer>();

  // Transient
  public JSONObject save = null;

  private Persistence() {
    reset();
    tryLoadSetting();
  }

  private void reset() {
    musicLevel = 0.5f;
    sfxLevel = 1f;
    particleHues.put(kH, 1080);
    particleHues.put(kW, 791);
    particleHues.put(kZ, 385);
    particleHues.put(kE, 480);
    particleHues.put(kM, 958);
    particleHues.put(kQ, 277);
    particleHues.put(kBlank, 7);
    bestTimes.clear();
    bestTimes.add(0);
    bestTimes.add(0);
    bestTimes.add(0);
    bestTimes.add(0);
  }

  public void tryLoadGameSave() {
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
        save = new JSONObject((String) objectIn.readObject());
        objectIn.close();
      } catch (IOException e) {
        e.printStackTrace();
        Gdx.app.error("tryLoadGameSave", "IOException!");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        Gdx.app.error("ClassNotFoundException", "IOException!");
      } catch (JSONException e) {
        e.printStackTrace();
        Gdx.app.error("JSONException", "IOException!");
      }
      Gdx.app.log("tryLoadGameSave", "Load save file complete");
    } else {
      Gdx.app.log("tryLoadGameSave", "No save file");
    }
  }

  public void tryLoadSetting() {
    if (!Gdx.files.isLocalStorageAvailable()) {
      return;
    }

    FileHandle settings = Gdx.files.local(Param.SETTINGS_FILE);
    if (settings.exists()) {
      try {
        JSONObject json = new JSONObject(settings.readString());
        musicLevel = (float) json.getDouble("music");
        sfxLevel = (float) json.getDouble("sfx");
        JSONObject hue = json.getJSONObject("hue");
        for (Particle p : Particle.values()) {
          particleHues.put(p, hue.getInt(p.name()));
        }
        bestTimes.clear();
        bestTimes.add(json.getInt("bestTime0"));
        bestTimes.add(json.getInt("bestTime1"));
        bestTimes.add(json.getInt("bestTime2"));
        bestTimes.add(json.getInt("bestTime3"));
      } catch (JSONException e) {
        e.printStackTrace();
        Gdx.app.error("tryLoadSetting", "JSONException! Resetting");
        reset();
      }
    }
  }

  public void trySaveSettings() {
    if (!Gdx.files.isLocalStorageAvailable()) return;

    Gdx.app.log("trySave", "Save Settings");
    FileHandle settings = Gdx.files.local(Param.SETTINGS_FILE);
    JSONObject jsonSettings = new JSONObject();
    try {
      jsonSettings.put("music", musicLevel);
      jsonSettings.put("sfx", sfxLevel);
      JSONObject hue = new JSONObject();
      for (Particle p : Particle.values()) {
        hue.put(p.name(), particleHues.get(p));
      }
      jsonSettings.put("hue", hue);
      jsonSettings.put("bestTime0", bestTimes.get(0));
      jsonSettings.put("bestTime1", bestTimes.get(1));
      jsonSettings.put("bestTime2", bestTimes.get(2));
      jsonSettings.put("bestTime3", bestTimes.get(3));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    settings.writeString(jsonSettings.toString(), false);
  }

  public void trySaveGame() {
    if (!Gdx.files.isLocalStorageAvailable()) return;

    Gdx.app.log("trySave", "Streaming game data");
    save = new JSONObject();
    try {
      save.put("GameState", GameState.getInstance().serialise());
      save.put("World", World.getInstance().serialise());
      save.put("Camera", Camera.getInstance().serialise());
    } catch (JSONException e) {
      e.printStackTrace();
      Gdx.app.error("trySaveGame", "JSONException!");
    }
  }

  public void flushSaveGame() {
    if (!Gdx.files.isLocalStorageAvailable()) return;
    if (save == null) return;
    Gdx.app.log("flushSaveGame", "Flushing save data to disk");

    FileHandle handle = Gdx.files.local(Param.SAVE_FILE);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzipOut;
    try {
      gzipOut = new GZIPOutputStream(baos);
      ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
      objectOut.writeObject(save.toString());
      objectOut.close();
      baos.writeTo(handle.write(false));
    } catch (IOException e) {
      e.printStackTrace();
      Gdx.app.error("flushSaveGame", "IOException!");
    }
  }

  public void deleteSave() {
    if (!Gdx.files.isLocalStorageAvailable()) return;
    FileHandle fh = Gdx.files.local(Param.SAVE_FILE);
    fh.delete();
    save = null;
    Gdx.app.log("deleteSave", "WARNING!!! Deleting save game");
  }

  public void dispose() {
    trySaveSettings();
    flushSaveGame();
    ourInstance = null;
  }

}
