package timboe.vev;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;

import timboe.vev.enums.Particle;

public class Lang {

  private static Lang ourInstance;

  public static Lang getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Lang();
  }

  private Lang() {
    EN_GB.put("energy", "Get energy from mining ore and deconstructing particles.\nSpend it on new buildings, and on upgrading them.");
    EN_GB.put("particles", "Particles in the world.\nDeconstruct them all to finish the game.\nParticles are spawned by white holes.");
    EN_GB.put("wParticles", "Particles yet to spawn into the world.");
    EN_GB.put("disassembleTime", "Time to deconstruct particle.");
    EN_GB.put("disassembleEnergy", "Energy released by deconstructing particle.");
    EN_GB.put("musicVolume", "Music Volume.");
    EN_GB.put("sfxVolume", "Sound Effects Volume.");
    EN_GB.put("qSize", "Size of building's queue.\nLonger queues take longer to construct and cost more.");
    EN_GB.put("buildingPrice", "Energy cost of this building.");
    EN_GB.put("newGame", "Start a new game.\nWill overwrite any existing save.");
    EN_GB.put("loadGame", "Load from the single save slot.");
    EN_GB.put("settings", "Change sound volume\nand particle colours.");
    EN_GB.put("howToPlay", "Controls, and game instructions.");
    EN_GB.put("credits", "Asset credits.");
    EN_GB.put("exit", "Exit to desktop.");
    EN_GB.put("standingOrder", "Setup standing move order.\nSpawned # will be sent to this location.");
    EN_GB.put("standingBlank", "Setup overflow standing move order.\nParticles which COULD be accepted,\nbut are turned away due to a full queue\nwill be sent here. NO cyclic loops.");
    EN_GB.put("select", "Toggle selection mode.");
    EN_GB.put("pause", "Show the pause menu.");
    EN_GB.put("particleSelect", "Left Click: Refine selection to just #\nShift+Left Click: Remove # from selection");
    EN_GB.put("qSimple", "Construct a straight queue.");
    EN_GB.put("qSpiral", "Construct a spiral queue.");
    EN_GB.put("upgradeBuilding_A", "Upgrade building.");
    EN_GB.put("upgradeBuilding_B", "\nUpgrade Cost: ");
    EN_GB.put("upgradeBuilding_C", "\nUpgrade Time: ");
    EN_GB.put("upgradeBuilding_D", "\nBuilding Speed Bonus: ");
    EN_GB.put("upgradeBuilding_D_Mine", "\nAverage Truck Speed Bonus: ");
    EN_GB.put("buildBuilding_A", "Build: ");
    EN_GB.put("buildBuilding_B", "\nAccepts: ");
    EN_GB.put("buildBuilding_B_Mine", "\nMines Ore, Produces Energy");
    EN_GB.put("buildBuilding_C", "\nBase Cost: ");
    EN_GB.put("wrecking", "Demolish building.\nRecoup #% of building's cost.");
    EN_GB.put("saveAndQuit", "Save the game and exit VEV.");
    EN_GB.put("quitToTitle", "Exit to the title screen.");
    EN_GB.put("resume", "Un-pause and resume.");
    EN_GB.put("fullscreen", "Toggles fullscreen mode.");
    EN_GB.put("volume", "Changes the volume.");
    EN_GB.put("settingsTime", "Current game time.");
    EN_GB.put("newGameOpeningA", "This white hole has spawned the first # particles into the land.");
    EN_GB.put("newGameOpeningB", "# more particles will follow, then the white holes will be depleted.");
    EN_GB.put("newGameOpeningC", "Deconstruct every particle in the shortest possible time.");
    EN_GB.put("ok", "OK");
    EN_GB.put("midGame", "The white holes are empty!\nDeconstruct the remaining # particles in the shortest possible time.");
    EN_GB.put("demolish", "Demolish Building?\nRecover # Energy");
    EN_GB.put("progressTimer", "Particle deconstruction or\nbuilding upgrade timer.");
    EN_GB.put("selectAll", "Select all particles.");
    EN_GB.put("level", "Level");


    EN_GB.put("UI_NEW", "NEW GAME");
    EN_GB.put("UI_LOAD", "CONTINUE");
    EN_GB.put("UI_SETTINGS", "SETTINGS");
    EN_GB.put("UI_EXIT", "EXIT");
    EN_GB.put("UI_HOW", "HOW TO PLAY");
    EN_GB.put("UI_ENERGY", "Energy");
    EN_GB.put("UI_PARTICLES", "Particles");
    EN_GB.put("UI_GAME_LENGTH", "CHOOSE GAME LENGTH");
    EN_GB.put("UI_LENGTH", "LENGTH");
    EN_GB.put("UI_PARTICLES_", "PARTICLES");
    EN_GB.put("UI_BEST", "BEST TIME");
    EN_GB.put("UI_SHORT", "SHORT");
    EN_GB.put("UI_MED", "MEDIUM");
    EN_GB.put("UI_LONG", "LONG");
    EN_GB.put("UI_XL", "X-LONG");
    EN_GB.put("UI_MUSIC", "MUSIC");
    EN_GB.put("UI_SFX", "SOUND FX");
    EN_GB.put("UI_N_PARTICLES", ": Deconstruct # Particles");
    EN_GB.put("UI_BEST_TIME", ". Best Time: #s");
    EN_GB.put("UI_CANCEL", "CANCEL");
    EN_GB.put("UI_BACK", "BACK");
    EN_GB.put("UI_CREDITS", "CREDITS");
    EN_GB.put("UI_SAVE_AND_QUIT", "SAVE &\nEXIT");
    EN_GB.put("UI_RESUME", "RESUME");
    EN_GB.put("UI_FULLSCREEN", "FULLSCREEN");
    EN_GB.put("UI_GENERATING", "GENERATING");
    EN_GB.put("UI_SAVING", "SAVING");
    EN_GB.put("UI_TIME", "Time");
    EN_GB.put("UI_FINISHED", "FINISHED!");
    EN_GB.put("UI_END_TIME", "YOUR TIME: #s");
    EN_GB.put("UI_END_BEST_TIME", "BEST TIME: #s");
    EN_GB.put("UI_BUILDINGS_PLACED", "BUILDINGS PLACED: #");
    EN_GB.put("UI_BUILDINGS_DESTROYED", "BUILDINGS DESTROYED: #");
    EN_GB.put("UI_PARTICLES_DESTROYED", "PARTICLES DECONSTRUCTED: #");
    EN_GB.put("UI_PARTICLE_BOUNCES", "PARTICLE BOUNCES: #");
    EN_GB.put("UI_DIFFICULTY", "GAME LENGTH: #");
    EN_GB.put("UI_TIBERIUM_MINED", "ORE MINED: #");
    EN_GB.put("UI_TREES_DEMOLISHED", "TREES BULLDOZED: #");
    EN_GB.put("UI_TAPS", "CLICKS: #");
    EN_GB.put("UI_BUILDING_UPGRADES", "BUILDING UPGRADES: #");
    EN_GB.put("UI_YES", "YES");
    EN_GB.put("UI_NO", "NO");
    EN_GB.put("UI_WARN", "WARNING: STARTING A NEW GAME WILL\nOVERWRITE YOUR EXISTING SAVE GAME!");
    EN_GB.put("UI_ALL", "ALL");


    EN_GB.put("UI_HELP_00", "A game by Tim Martin");
    EN_GB.put("UI_HELP_01", "Music by Chris Zabriskie (CC v4)");
    EN_GB.put("UI_HELP_02", "Is That You Or Are You You? / Divider / CGI Snake");
    EN_GB.put("UI_HELP_03", "Open Game Art by Buch (CC0 v1)");

    EN_GB.put("UI_HELP_A_00", "CONTROLS");
    EN_GB.put("UI_HELP_A_01", "LEFT CLICK");
    EN_GB.put("UI_HELP_A_02", "Select Particle / Building");
    EN_GB.put("UI_HELP_A_03", "Confirm Particle Move Order");
    EN_GB.put("UI_HELP_A_04", "Confirm Build New Building");
    EN_GB.put("UI_HELP_A_05", "Confirm Building Move Destination");
    EN_GB.put("UI_HELP_A_06", "LEFT CLICK AND DRAG");
    EN_GB.put("UI_HELP_A_07", "Select Particles Within Box");
    EN_GB.put("UI_HELP_A_08", "RIGHT CLICK");
    EN_GB.put("UI_HELP_A_09", "Cancel Particle / Building Selection");
    EN_GB.put("UI_HELP_A_10", "RIGHT CLICK AND DRAG / W-A-S-D");
    EN_GB.put("UI_HELP_A_11", "Move Map");
    EN_GB.put("UI_HELP_A_12", "MOUSE SCROLL / Q-E");
    EN_GB.put("UI_HELP_A_13", "Zoom Map");

    EN_GB.put("UI_ANDROID_A_00", "CONTROLS");
    EN_GB.put("UI_ANDROID_A_01", "TAP");
    EN_GB.put("UI_ANDROID_A_02", "Select Particle / Building");
    EN_GB.put("UI_ANDROID_A_03", "Confirm Particle Move Order");
    EN_GB.put("UI_ANDROID_A_04", "LONG-TAP AND DRAG");
    EN_GB.put("UI_ANDROID_A_05", "Select Particles Within Box");
    EN_GB.put("UI_ANDROID_A_06", "DRAG");
    EN_GB.put("UI_ANDROID_A_07", "Move Map");
    EN_GB.put("UI_ANDROID_A_08", "PINCH");
    EN_GB.put("UI_ANDROID_A_09", "Zoom Map");


    EN_GB.put("UI_HELP_B_00", "Get energy by harvesting ore and deconstructing particles.");
    EN_GB.put("UI_HELP_B_01", "Spend energy on creating and upgrading buildings.");
    EN_GB.put("UI_HELP_B_02", "Deconstruct particles in buildings.");
    EN_GB.put("UI_HELP_B_03", "Deconstruct all particles to win the game.");
    EN_GB.put("UI_HELP_B_04", "ORE PATCH");
    EN_GB.put("UI_HELP_B_05", "(ONLY FOUND IN DESERTS)");
    EN_GB.put("UI_HELP_B_06", "ORE REFINERY");
    EN_GB.put("UI_HELP_B_07", "(YOU CAN ONLY BUILD ON GRASS)");
    EN_GB.put("UI_HELP_B_08", "ORE TRUCK");
    EN_GB.put("UI_HELP_B_09", "ORE TRUCK'S ROUTE");

    EN_GB.put("UI_HELP_C_00", "Setup standing move orders to route particles from white holes");
    EN_GB.put("UI_HELP_C_01", "to (and between) deconstruction buildings.");
    EN_GB.put("UI_HELP_C_02", "WHITE HOLE");
    EN_GB.put("UI_HELP_C_03", "(ONLY FOUND IN DESERTS)");
    EN_GB.put("UI_HELP_C_04", "(SPAWNS PARTICLES UNTIL EMPTY)");
    EN_GB.put("UI_HELP_C_05", "<- DECONSTRUCTION");
    EN_GB.put("UI_HELP_C_06", "BUILDING'S QUEUE");
    EN_GB.put("UI_HELP_C_07", "<- DECONSTRUCTION");
    EN_GB.put("UI_HELP_C_08", "BUILDING'S ACCEPTED");
    EN_GB.put("UI_HELP_C_09", "PARTICLES");
    EN_GB.put("UI_HELP_C_10", Particle.kH.getString() + " PARTICLES");
    EN_GB.put("UI_HELP_C_11", "WHITE HOLE'S STANDING MOVE ORDER FOR " + Particle.kH.getString() + " PARTICLES");

    EN_GB.put("UI_HELP_D_00", "A game in LIBGDX by Tim Martin");
    EN_GB.put("UI_HELP_D_01", "A* implementation by Ben Ruijl");
    EN_GB.put("UI_HELP_D_02", "GRAPHICS & FONT ASSETS");
    EN_GB.put("UI_HELP_D_03", "Buch: Colony sim, match 3. Pawel Pastuszak: VisUI");
    EN_GB.put("UI_HELP_D_04", "SpriteFX: plants, lightning. OniMille: Crystal");
    EN_GB.put("UI_HELP_D_05", "Steve Matteson: Open Sans. Peter Hull: VT323");
    EN_GB.put("UI_HELP_D_06", "MUSIC & SOUND EFFECT ASSETS");
    EN_GB.put("UI_HELP_D_07", "Chris Zabriskie: Is That You Or Are You You?,");
    EN_GB.put("UI_HELP_D_08", "Divider, CGI Snake.");
    EN_GB.put("UI_HELP_D_09", "cameronmusic: pulse1. Planman: Poof of Smoke");
    EN_GB.put("UI_HELP_D_10", "FxKid2: Cute Walk Run 2. tix99: squeak toy");
    EN_GB.put("UI_HELP_D_11", "waveplay_old: Short Click. pel2na: Two Kazoo Fanfare.");
    EN_GB.put("UI_HELP_D_12", "MATTIX: Retro Explosion 5. Mark DiAngelo: Blop");
    EN_GB.put("UI_HELP_D_13", "man: Swoosh 1. Selector: rocket launch");
    EN_GB.put("UI_HELP_D_14", "Raclure: miss chime, affirmative chime");
    EN_GB.put("UI_HELP_D_15", "doorajar: DirtShovel. visual: Industrial Bass 1");

  }

  private static HashMap<String, String> EN_GB = new HashMap<String, String>();

  public static String get(String key) {
    String[] parts = key.split("#");
    if (!EN_GB.containsKey(parts[0])) {
      Gdx.app.error("Get", "MISSING TEXT FOR " + parts[0]);
      throw new AssertionError();
    }
    String result = EN_GB.get(parts[0]);
    if (parts.length > 1) {
      result = result.replaceAll("#", parts[1]);
    }
    return result;
  }

}
