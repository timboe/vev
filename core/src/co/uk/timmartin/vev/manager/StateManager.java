package co.uk.timmartin.vev.manager;

import com.badlogic.gdx.Gdx;
import com.google.gwt.thirdparty.json.JSONException;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.VEVGame;
import co.uk.timmartin.vev.entity.Warp;
import co.uk.timmartin.vev.enums.FSM;
import co.uk.timmartin.vev.screen.GameScreen;
import co.uk.timmartin.vev.screen.TitleScreen;

public class StateManager {
  private static StateManager ourInstance;

  public static StateManager getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new StateManager();
  }

  private GameScreen theGameScreen;
  public TitleScreen theTitleScreen;
  private VEVGame game;

  public FSM fsm;

  private StateManager() {
    reset();
  }

  private void reset() {
    fsm = FSM.kNO_STATE;
  }

  public void init(VEVGame vev) {
    this.theTitleScreen = new TitleScreen();
    this.theGameScreen = new GameScreen();
    this.game = vev;
    setToTitleScreen();
  }

  public void transitionToGameScreen() {
    if (fsm != FSM.kINTRO) {
      Gdx.app.error("transitionToGameScreen", "Unexpected called from " + fsm);
    }
    fsm = FSM.kTRANSITION_TO_GAME;
    Gdx.input.setInputProcessor(null);
    theTitleScreen.transitionOutTimers[0] = 0f;
    Sounds.getInstance().pulse();
  }

  public void transitionToTitleScreen() {
    if (fsm != FSM.kGAME && fsm != FSM.kGAME_OVER) {
      Gdx.app.error("transitionToTitleScreen", "Unexpected called from " + fsm);
    }
    fsm = (fsm == FSM.kGAME ? FSM.kTRANSITION_TO_INTRO_SAVE : FSM.kTRANSITION_TO_INTRO_NOSAVE);
    if (fsm == FSM.kTRANSITION_TO_INTRO_SAVE) {
      UI.getInstance().saving.setVisible(true);
    }
    Gdx.input.setInputProcessor(null);
    theGameScreen.transitionOutTimers[0] = 0f;
    Sounds.getInstance().pulse();
  }

  public void setToTitleScreen() {
    if (fsm != FSM.kTRANSITION_TO_INTRO_SAVE && fsm != FSM.kTRANSITION_TO_INTRO_NOSAVE && fsm != FSM.kNO_STATE) {
      Gdx.app.error("setToTitleScreen", "Unexpected called from " + fsm);
    }
    final boolean doSave = (fsm == FSM.kTRANSITION_TO_INTRO_SAVE);
    fsm = FSM.kFADE_TO_INTRO;
    Gdx.input.setInputProcessor(null);
    GameState.getInstance().clearPathingCache();
    if (doSave) {
      Persistence.getInstance().trySaveGame();
      World.getInstance().reset(false);
    } else {
      World.getInstance().reset(false);
    }
    game.setScreen(theTitleScreen);
    theTitleScreen.fadeIn = 100f;
    Sounds.getInstance().processing(false);
    UIIntro.getInstance().resetTitle("main");
  }

  public void titleScreenFadeComplete() {
    if (fsm != FSM.kFADE_TO_INTRO) {
      Gdx.app.error("titleScreenFadeComplete", "Unexpected called from " + fsm);
    }
    fsm = FSM.kINTRO;
    theTitleScreen.doInputHandles();
  }

  public void setToGameScreen() {
    if (fsm != FSM.kTRANSITION_TO_GAME) {
      Gdx.app.error("setToGameScreen", "Unexpected called from " + fsm);
    }
    fsm = FSM.kFADE_TO_GAME;
    game.setScreen(theGameScreen);
    theGameScreen.setMultiplexerInputs();
    theGameScreen.fadeIn = 100f;
    Gdx.input.setInputProcessor(null);
    GameState.getInstance().clearPathingCache();
    UI.getInstance().resetGame();

    if (World.getInstance().warpParticlesCached != -1) {
      // Only for a new game
      GameState.getInstance().warpParticles = World.getInstance().warpParticlesCached;
      Warp toFocusOn = GameState.getInstance().getWarpMap().values().iterator().next();
      Camera.getInstance().setCurrentPos(
              toFocusOn.getX() + (Param.WARP_SIZE / 2f * Param.TILE_S),
              toFocusOn.getY() + (Param.WARP_SIZE / 2f * Param.TILE_S));
    } else {
      try {
        Camera.getInstance().deserialise(Persistence.getInstance().save.getJSONObject("Camera"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public void gameScreenFadeComplete() {
    if (fsm != FSM.kFADE_TO_GAME) {
      Gdx.app.error("gameScreenFadeComplete", "Unexpected called from " + fsm);
    }
    fsm = FSM.kGAME;
    theGameScreen.doInputHandles();
    GameState.getInstance().showMainUITable(false);
  }

  public void gameOver() {
    if (fsm != FSM.kGAME) {
      Gdx.app.error("gameOver", "Unexpected called from " + fsm);
    }
    fsm = FSM.kGAME_OVER;
    UI.getInstance().showFin();
    Persistence.getInstance().deleteSave();
    Sounds.getInstance().fanfare();
  }

  public void dispose() {
    theTitleScreen.dispose();
    theGameScreen.dispose();
    ourInstance = null;
  }


}
