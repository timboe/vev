package timboe.vev;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;

public class Lang {

  private static Lang ourInstance;
  public static Lang getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Lang(); }

  private Lang() {
    EN_GB.put("energy","Get energy from mining and deconstructing.\nSpend it on new buildings, and on upgrading them.");
    EN_GB.put("particles", "Particles in the world.\nDeconstruct them all to finish the game.\nParticles will be spawned by the Warp Portals,\nuntil they're empty.");
    EN_GB.put("wParticles", "Particles yet to spawn into the world.");
    EN_GB.put("disassembleTime", "Time to deconstruct particle.");
    EN_GB.put("disassembleEnergy", "Energy released by deconstructing particle.");
    EN_GB.put("musicVolume", "Music Volume.");
    EN_GB.put("sfxVolume", "Sound Effects Volume.");
    EN_GB.put("qSize", "Size of building's queue. Longer queues take longer to construct.");
    EN_GB.put("buildingPrice", "Energy cost of this building.");
    EN_GB.put("newGame", "Start a new game.\nWill overwrite any existing save.");
    EN_GB.put("loadGame", "Load from the single save slot.");
    EN_GB.put("settings", "Change sound volume\nand particle colours.");
    EN_GB.put("standingOrder","Setup Standing Move Order.\nSpawned # will be sent to this location.");
    EN_GB.put("standingBlank","Setup Overflow Standing Move Order.\nParticles which COULD be accepted,\nbut are turned away due to a full queue\nwill be sent here. NO cyclic loops.");
    EN_GB.put("select","Toggle selection mode.");
    EN_GB.put("pause","Show the pause menu.");
    EN_GB.put("particleSelect","Refine selection to just #");
    EN_GB.put("qSimple","Construct a straight queue.");
    EN_GB.put("qSpiral","Construct a spiral queue.");
    EN_GB.put("upgradeBuilding_A","Upgrade building.");
    EN_GB.put("upgradeBuilding_B","\nUpgrade Cost: ");
    EN_GB.put("upgradeBuilding_C","\nUpgrade Time: ");
    EN_GB.put("upgradeBuilding_D","\nBuilding Speed Bonus: ");
    EN_GB.put("upgradeBuilding_D_Mine","\nAverage Truck Speed Bonus: ");
    EN_GB.put("buildBuilding_A","Build: ");
    EN_GB.put("buildBuilding_B","\nAccepts: ");
    EN_GB.put("buildBuilding_B_Mine","\nMines Ore, Produces Energy");
    EN_GB.put("buildBuilding_C","\nBase Cost: ");
    EN_GB.put("wrecking","Demolish building.\nRecoup #% of building's cost.");
    EN_GB.put("saveAndQuit","Save the game and exit VEV.");
    EN_GB.put("quitToTitle","Exit to the title screen.");
    EN_GB.put("resume","Un-pause and resume.");
    EN_GB.put("fullscreen","Toggles fullscreen mode.");
    EN_GB.put("volume","Changes the volume.");
    EN_GB.put("settingsTime","Current game time.");

    EN_GB.put("UI_NEW", "NEW GAME");
    EN_GB.put("UI_LOAD", "CONTINUE");
    EN_GB.put("UI_SETTINGS", "SETTINGS");
    EN_GB.put("UI_EXIT", "EXIT");
    EN_GB.put("UI_HOW", "HOW TO PLAY");
    EN_GB.put("UI_ENERGY", "Energy");
    EN_GB.put("UI_PARTICLES", "Particles");
    EN_GB.put("UI_GAME_LENGTH", "CHOOSE GAME LENGTH");
    EN_GB.put("UI_SHORT", "SHORT");
    EN_GB.put("UI_MED", "MEDIUM");
    EN_GB.put("UI_LONG", "LONG");
    EN_GB.put("UI_XL", "X-LONG");
    EN_GB.put("UI_MUSIC", "MUSIC");
    EN_GB.put("UI_SFX", "SOUND FX");
    EN_GB.put("UI_N_PARTICLES", ": Deconstruct # Particles");
    EN_GB.put("UI_BEST_TIME", ". Best Time: # s");
    EN_GB.put("UI_CANCEL", "CANCEL");
    EN_GB.put("UI_BACK", "BACK");
    EN_GB.put("UI_CREDITS", "CREDITS");
    EN_GB.put("UI_SAVE_AND_QUIT", "SAVE &\nEXIT");
    EN_GB.put("UI_RESUME", "RESUME");
    EN_GB.put("UI_FULLSCREEN", "FULLSCREEN");
    EN_GB.put("UI_GENERATING", "GENERATING");
    EN_GB.put("UI_TIME", "Time");
    EN_GB.put("UI_FINISHED", "FINISHED!");
    EN_GB.put("UI_FINISHED_BEST", "NEW BEST TIME!");
    EN_GB.put("UI_END_TIME", "TIME:#s");
    EN_GB.put("UI_END_BEST_TIME", "BEST:#s");


  }

  private static HashMap<String, String> EN_GB = new HashMap<String, String>();

  public static String get(String key) {
    String[] parts = key.split("#");
    if (!EN_GB.containsKey(parts[0])) {
      Gdx.app.error("Get","MISSING TEXT FOR " + parts[0]);
      throw new AssertionError();
    }
    String result =  EN_GB.get(parts[0]);
    if (parts.length > 1) {
      result = result.replaceAll("#", parts[1]);
    }
    return result;
  }

}
