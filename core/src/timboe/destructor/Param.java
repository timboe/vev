package timboe.destructor;

public class Param {

  public static final int ZONES_X = 3; // Constant... or not to constant
  public static final int ZONES_Y = 3;

  public static final int TILES_X = 256;
  public static final int TILES_Y = 256;

  public static final int TILE_S = 16;

  public static final int SPRITE_SCALE = 2;

  public static final int MIN_GREEN_ZONE = 4;
  public static final int MAX_GREEN_ZONE = 6;

  public static final int MIN_GREEN_HILL = 2;
  public static final int MIN_RED_HILL = 2;

  public static final int MAX_KRINKLE = 3; // Must be ODD
  public static final int NEAR_TO_EDGE = MAX_KRINKLE * 2; // Min close-able space. If one went +ve and the other -ve,
  public static final int EDGE_ADJUSTMENT = (MAX_KRINKLE/2)+1;

  public static final float HILL_IN_HILL_PROB = .2f;
  public static final float STAIRS_PROB = .2f;
  public static final float FOLIAGE_PROB = .004f;
  public static final float TREE_PROB = .3f;

  public static final int MIN_STAIRCASES = 2;

  public static final int MIN_DIST = 2; // Minimum number of steps to do for a feature
  public static final int MAX_DIST = 7; // Maximum number of steps to do for a feature

  public static final int DISPLAY_X = 1920;
  public static final int DISPLAY_Y = 1080;

  public static final int MAX_FRAMES = 1;

  public static final int N_GRASS_VARIANTS = 13;
  public static final int N_BORDER_VARIANTS = 4;

  public static final int N_BUSH = 4;
  public static final int N_TREE = 4;

  public static final float SCROLL_ZOOM = 0.1f;

  public static final float ZOOM_MIN = 0.1f;
  public static final float ZOOM_MAX = 4.0f;

  public static final float DESIRED_FPS = 60; // FPS ANIM_SPEED is tuned for
  public static final float FRAME_TIME = (1f/DESIRED_FPS);

  //

  public static int DEBUG = 1;
}
