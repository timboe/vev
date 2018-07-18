package timboe.destructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import timboe.destructor.pathfinding.IVector2;

import java.util.Random;

public class Util {

  public static final Random R = new Random();

  // Why -2? It's to allow for an buffer for odd world sizes
  public static boolean inBounds(int x, int y) {
    return !needsClamp(x, 0, Param.TILES_X - 2) && !needsClamp(y, 0, Param.TILES_Y - 2);
  }

  public static boolean inBounds(IVector2 v) {
    return inBounds(v.x, v.y);
  }

  public static float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
  }

  public static void renderClear() {
    Gdx.gl.glClearColor(.1529f, .1255f, .1922f, 1);
    Gdx.gl.glLineWidth(3);
    Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
  }

  public static boolean needsClamp(float val, float min, float max) { return !(val == clamp(val,min,max)); }





}
