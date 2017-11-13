package timboe.destructor;

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

}
