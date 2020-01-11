package timboe.vev;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.EnumMap;
import java.util.Vector;

import timboe.vev.enums.Particle;
import timboe.vev.enums.QueueType;

public class Param {




  public static final int ZONES_X = 3; // Constant... or not to constant
  public static final int ZONES_Y = 3;
  private static final int ZONES_MAX = Math.max(ZONES_X, ZONES_Y);

  private static final int INTRO_STRETCH = 2;
  public static final int TILES_INTRO_X = 64;
  public static final int TILES_INTRO_Y = TILES_INTRO_X * INTRO_STRETCH;

  public static final int TILES_INTRO_X_MID = TILES_INTRO_X/2;
  public static final int TILES_INTRO_Y_MID = (TILES_INTRO_X * (INTRO_STRETCH-1)) + (TILES_INTRO_X/2);
  public static final float TILES_INTRO_ZOOM = .25f;

  public static final int TILES_X = 128+32;
  public static final int TILES_Y = 128+32;
  private static final int TILES_MIN = Math.min(TILES_X, TILES_Y);
  public static final int TILES_MAX = Math.max(TILES_X, TILES_Y);

  public static final int TILE_S = 16;

  public static final int WORLD_SEED = 0; // 0 to disable

  public static final int SPRITE_SCALE = 2;
  public static final float PARTICLE_VELOCITY = 32f*2f;
  public static final float PARTICLE_AT_TARGET = 2f;
  public static final float NEW_PARTICLE_MEAN = 5f; // mean number of particles to place
  public static final float NEW_PARTICLE_WIDTH = 10f; // width of new particles dist
  public static final int NEW_PARTICLE_MAX = 200; // max number of particles to place TODO remove this
  public static final float NEW_PARTICLE_TIME = 0.2f; // max time before new particle is sent into world from holding pen
  public static final float PARTICLE_BORED_TIME = 60f; // max seconds until bored (may wander off)
  public static final float PARTICLE_WANDER_CHANCE = 0.001f; // if bored - note this is per animation frame so set it low
  public static final int PARTICLE_WANDER_R = TILES_MIN / 8; // Max distance to wander

  public static final int MIN_GREEN_ZONE = 4;
  public static final int MAX_GREEN_ZONE = 6;

  public static final int MIN_GREEN_HILL = 2;
  public static final int MIN_RED_HILL = 2;

  public static final int KRINKLE_OFFSET = (TILES_MIN/ZONES_MAX) / 10; // To leave room for edge-red-green-hill1-hill2 x2
  public static final int KRINKLE_GAP = 3; // Tiles to leave clear between krinkles
  public static final int MAX_KRINKLE = 3; // Must be ODD
  public static final int NEAR_TO_EDGE = (MAX_KRINKLE * 2) + 1; // Min close-able space. If one went +ve and the other -ve,
  public static final int EDGE_ADJUSTMENT = (MAX_KRINKLE/2)+1;
  public static final boolean ALLOW_TILING_ERRORS = false;

  public static final int N_PATCH_TRIES = 25;
  public static final int WIGGLE = 8; // Random pixel offset for foliage
  public static final float PATCH_DENSITY = 0.5f;

  public static final int FOREST_SIZE = 7;
  public static final int MIN_FORESTS = 2;
  public static final int MAX_FORESTS = 5;

  public static final int TIBERIUM_SIZE = 5;
  public static final int MIN_TIBERIUM_PATCH = 2;
  public static final int MAX_TIBERIUM_PATCH = 3;

  public static final int WARP_SIZE = 10;
  public static final int MIN_WARP = 2;
  public static final int MAX_WARP = 3;
  public static final float WARP_TRANSPARENCY = 0.5f;
  public static final float WARP_ROTATE_SPEED = 3f;
  public static final float WARP_SHAKE = 5f;
  public static final float WARP_SPAWN_TIME_INITIAL = 5f; // seconds between spawn
  public static final float WARP_SPAWN_TIME_REDUCTION = 0.01f; // time reduced by this every spawn TODO make no higher than .01f
  public static final float WARP_SPAWN_MEAN_INCREASE = .25f; // increase in mean number of spawned
  public static final float WARP_SPAWN_WIDTH_INCREASE = 0.1f; // increase in width of mean number
  public static final float WARP_SPAWN_TIME_MIN = 0.5f; // min time between spawn

  public static final float BUILD_TIME = .5f; // Time in seconds between build phases
  public static final float BUILDING_QUEUE_MOVE_TIME = .2f; // Time in seconds between particles moving through queue
  public static final float BUILDING_SHAKE = 2.5f;
  public static final float BUILDING_DISASSEMBLE_BONUS = 0.9f; //^(building level)
  public static final float BUILDING_REFUND_AMOUND = 0.8f; //Recoup % of all building costs


  public static final float LONG_PRESS_TIME = 0.15f;

  public static final int N_TIBERIUM_SPRITES = 4; // Number of sprites
  public static final int N_BALL_SPRITES = 6; // number of sprites
  public static final int N_TRUCK_SPRITES = 8;
  public static final int MAX_FRAMES = Math.max(N_TIBERIUM_SPRITES, Math.max(N_BALL_SPRITES, N_TRUCK_SPRITES));

  public static final float HILL_IN_HILL_PROB = .2f;
  public static final float STAIRS_PROB = .8f;
  public static final float FOLIAGE_PROB = .008f;
  public static final float TREE_PROB = .3f; // Given foliage, % of it being a tree

  public static final int MIN_STAIRCASES = 1;

  public static final int MIN_DIST = 2; // Minimum number of steps to do for a feature
  public static final int MAX_DIST = 7; // Maximum number of steps to do for a feature

  public static final int N_GRASS_VARIANTS = 13;
  public static final int N_BORDER_VARIANTS = 4;

  public static final int N_BUSH = 4;
  public static final int N_TREE = 4;
  public static final int N_BUILDING = 5;

  public static final float SCROLL_ZOOM = 0.1f;
  public static final float TRANSLATE_MOD = 2;
  public static final float FLING_MOD = TRANSLATE_MOD * 0.01f;

  public static final float ZOOM_MIN = 0.1f;
  public static final float ZOOM_MAX = 3.0f;

  public static final float DESIRED_FPS = 60; // FPS ANIM_SPEED is tuned for
  public static final float FRAME_TIME = (1f/DESIRED_FPS);
  public static int FRAME = 0;

  public static final int PARTICLES_SMALL = 1000;
  public static final int PARTICLES_MED = 5000;
  public static final int PARTICLES_LARGE = 20000;
  public static final int PARTICLES_XL = 5000;


  public static final float ANIM_TIME = 1/20f; // I.e. 12 frames per second

  public static final Color HIGHLIGHT_GREEN = new Color(0f, 1f, 0f, 1f);
  public static final Color HIGHLIGHT_RED = new Color(1f, 0f, 0f, 1f);
  public static final Color HIGHLIGHT_YELLOW = new Color(1f, 1f, 0f, 1f);

  public static final int DISPLAY_X = 1920;
  public static final int DISPLAY_Y = Math.round(DISPLAY_X * (9f/16f));
  public static final int UI_WIDTH_INTRO = 450;

  public static final int PLAYER_STARTING_ENERGY = 5000 * 10;

  public static final int TRUCK_INITIAL_CAPACITY = 15000;
  public static final int TRUCK_LOAD_SPEED = 1250;
  public static final float TRUCK_SPEED_BONUS = 1.1f; //^(building level)

  public static final Color PARTICLE_Blank = new Color(128/255f, 128/255f, 128/255f, 1f);

  public static final int HSB_BASE_SATURATION = 61;
  public static final int HSB_BASE_BRIGHTNESS = 81;

  public static final int HSB_HIGHLIGHT_HUE_MOD = 12;
  public static final int HSB_HIGHLIGHT_SATURATION = 51;
  public static final int HSB_HIGHLIGHT_BRIGHTNESS = 86;

  public static final int HSB_OUTLINE_HUE_MOD = 333;
  public static final int HSB_OUTLINE_SATURATION = 58;
  public static final int HSB_OUTLINE_BRIGHTNESS = 53;

  public static final int HSB_SHADOW_HUE_MOD = 276;
  public static final int HSB_SHADOW_SATURATION = 46;
  public static final int HSB_SHADOW_BRIGHTNESS = 31;

  public static final int DEBUG_INITIAL = 0;
  private static final boolean FAKE_ANDROID = false;
  public static final boolean IS_ANDROID = FAKE_ANDROID || Gdx.app.getType() == Application.ApplicationType.Android;

  public static final int QUEUE_INITIAL_SIZE = 9;
  public static final QueueType QUEUE_INITIAL_TYPE = QueueType.kSIMPLE;

  public static final String SAVE_FILE = "data/VEV_save.dat";
  public static final String SETTINGS_FILE = "data/VEV_settings.json";
}
