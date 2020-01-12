package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
  public static Persistence getInstance() { return ourInstance; }
  public static void create() { ourInstance = new Persistence(); }
  public static boolean constructed() { return ourInstance != null; }

  // Persistent
  public float musicLevel;
  public float sfxLevel;
  public EnumMap<Particle,Integer> particleHues = new EnumMap<Particle,Integer>(Particle.class);
  public Vector<Integer> bestTimes = new Vector<Integer>();

  // Transient
  public JSONObject save = null;

  private Persistence() {
    reset();
    tryLoadSetting();
  }

  private void reset() {
    musicLevel = 1f;
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
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        e.printStackTrace();
      }
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
        musicLevel = (float)json.getDouble("music");
        sfxLevel = (float)json.getDouble("sfx");
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
        reset();
      }
    }
  }

  public void trySave() {
    if (!Gdx.files.isLocalStorageAvailable()) return;

    Gdx.app.log("trySave", "SAVE SETTINGS");
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

    if (GameState.getInstance().isGameOn()) {
      Gdx.app.log("trySave", "SAVING");
      save = new JSONObject();
      try {
        save.put("GameState", GameState.getInstance().serialise());
        save.put("World", World.getInstance().serialise());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public void flushSave() {
    if (!Gdx.files.isLocalStorageAvailable()) return;
    if (save == null) return;
    Gdx.app.log("flushSave", "FLUSHING TO DISK");

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
    }
  }


  public void dispose() {
    flushSave();
  }

}
