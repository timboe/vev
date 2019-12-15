package timboe.vev;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.Random;

import timboe.vev.enums.Colour;
import timboe.vev.pathfinding.IVector2;

public class Util {

  public static final Random R = new Random();

  public static JSONObject serialiseVec3(Vector3 v) throws JSONException {
    if (v == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("x", v.x);
    json.put("y", v.y);
    json.put("z", v.z);
    return json;
  }

  public static JSONObject serialiseVec2(Vector2 v) throws JSONException {
    if (v == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("x", v.x);
    json.put("y", v.y);
    return json;
  }

  public static Vector2 deserialiseVec2(JSONObject json) throws JSONException {
    if (json == null) {
      return null;
    }
    double x = json.getDouble("x");
    double y = json.getDouble("y");
    return new Vector2((float)x,(float)y);
  }

  public static Vector3 deserialiseVec3(JSONObject json) throws JSONException {
    if (json == null) {
      return null;
    }
    double x = json.getDouble("x");
    double y = json.getDouble("y");
    double z = json.getDouble("z");
    return new Vector3((float)x,(float)y,(float)z);
  }

  // Why -2? It's to allow for an buffer for odd world sizes
  public static boolean inBounds(int x, int y, boolean checkAgainstIntro) {
    if (checkAgainstIntro) {
      return !needsClamp(x, 0, Param.TILES_INTRO_X - 2) && !needsClamp(y, 0, Param.TILES_INTRO_Y - 2);
    } else {
      return !needsClamp(x, 0, Param.TILES_X - 2) && !needsClamp(y, 0, Param.TILES_Y - 2);
    }
  }

  public static boolean inBounds(IVector2 v, boolean checkAgainstIntro) {
    return inBounds(v.x, v.y, checkAgainstIntro);
  }

  public static float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
  }

  public static int clamp(int val, int min, int max) {
    return Math.max(min, Math.min(max, val));
  }

  public static void renderClear() {
    Gdx.gl.glClearColor(.1529f, .1255f, .1922f, 1);
    Gdx.gl.glLineWidth(3);
    Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
  }

  private static boolean needsClamp(float val, float min, float max) { return !(val == clamp(val,min,max)); }





}
