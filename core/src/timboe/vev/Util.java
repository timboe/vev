package timboe.vev;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.Random;

import timboe.vev.enums.Colour;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
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


  public static boolean doFade(ShapeRenderer sr, float delta, float[] fadeTimer) {
    sr.setProjectionMatrix(Camera.getInstance().getUiCamera().combined);
    sr.setColor(206f/255f, 101f/255f, 80f/255f, 1f);
    sr.begin(ShapeRenderer.ShapeType.Filled);
    strokeRect(sr, fadeTimer[0], fadeTimer[0]/4f);
    sr.setColor(176/255f, 78/255f, 80/255f, 1f);
    strokeRect(sr, fadeTimer[1], fadeTimer[1]/4f);
    sr.setColor(136/255f, 57/255f, 80/255f, 1f);
    strokeRect(sr, fadeTimer[2], fadeTimer[2]/4f);
    sr.end();
    Gdx.gl.glLineWidth(5);
    sr.begin(ShapeRenderer.ShapeType.Line);
    sr.setColor(72f/255f, 43f/255f, 81f/255f, 1f);
    strokeRect(sr, fadeTimer[0], fadeTimer[0]/4f);
    strokeRect(sr, fadeTimer[1], fadeTimer[1]/4f);
    strokeRect(sr, fadeTimer[2], fadeTimer[2]/4f);
    sr.end();
    fadeTimer[0] += (delta * 5);
    fadeTimer[0] *= 1.1;
    if (fadeTimer[0] > 2.5 && fadeTimer[1] == 0) fadeTimer[1] = fadeTimer[0] - 2.5f;
    if (fadeTimer[1] > 0) {
      fadeTimer[1] += (delta * 10);
      fadeTimer[1] *= 1.1;
    }
    if (fadeTimer[1] > 2.5 && fadeTimer[2] == 0) fadeTimer[2] = fadeTimer[1] - 2.5f;
    if (fadeTimer[2] > 0) {
      fadeTimer[2] += (delta * 10);
      fadeTimer[2] *= 1.1;
    }
    return (fadeTimer[2] > 1200);
  }

  private static void strokeRect(ShapeRenderer sr, float width, float angle) {
    final float midX = Param.DISPLAY_X / 2, midY = Param.DISPLAY_Y / 2;
    sr.rect(midX-width, midY-width,
            width,width,2*width,2*width,
            1,1,angle);
  }



}
