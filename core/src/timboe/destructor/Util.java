package timboe.destructor;

import timboe.destructor.pathfinding.IVector2;

import java.util.Random;

public class Util {

  public static Random R = new Random();

  public static boolean inBounds(int x, int y) {
    return !needsClamp(x, 0, Param.TILES_X - 1) && !needsClamp(y, 0, Param.TILES_Y - 1);
  }

  public static boolean inBounds(IVector2 v) {
    return inBounds(v.x, v.y);
  }

  public static float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
  }

  public static boolean needsClamp(float val, float min, float max) { return !(val == clamp(val,min,max)); }





}
