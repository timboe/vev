package timboe.destructor;

import timboe.destructor.enums.Colour;
import timboe.destructor.pathfinding.IVector2;

import java.util.Random;

public class Util {

  public static Random r = new Random();

  public static Integer rndInt(int max) {
    return r.nextInt(max);
  }

  public static float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
  }

  public static boolean needsClamp(float val, float min, float max) { return !(val == clamp(val,min,max)); }

  public static boolean inBounds(IVector2 v) {
    return (!(needsClamp(v.x, 0, Param.TILES_X) || needsClamp(v.y, 0, Param.TILES_Y)));
  }



}
