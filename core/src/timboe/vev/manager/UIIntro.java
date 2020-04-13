package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import timboe.vev.DistanceField.LabelDF;
import timboe.vev.Lang;
import timboe.vev.Param;
import timboe.vev.enums.Particle;
import timboe.vev.enums.UIMode;
import timboe.vev.input.FullscreenToggle;
import timboe.vev.input.NewGameButton;
import timboe.vev.input.NewGameDiag;

public class UIIntro {
  private static UIIntro ourInstance = null;

  public static UIIntro getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new UIIntro();
  }

  public static boolean constructed() {
    return ourInstance != null;
  }

  public void dispose() {
    ourInstance = null;
  }

  private Set<Image> ballImages = new HashSet<Image>();

  public FullscreenToggle fsListener = new FullscreenToggle();
  public NewGameButton newGameButton = new NewGameButton();
  public NewGameDiag newGameDiag = null;

  public int helpLevel = 1;

  public Table tableIntro = null;
  public Table tableHelp = null;
  public Table generating = null;

  public CheckBox fsBox = null;
  public CheckBox vibBox = null;

  // Settings cache
  private float cacheMusic, cacheSfx;
  private boolean cacheFullscreen, cacheVibrate;
  private EnumMap<Particle, Integer> cacheHue = new EnumMap<Particle, Integer>(Particle.class);

  private UIIntro() {
  }

  private void reset() {
    final UI ui = UI.getInstance();

    tableHelp = new Table();
    tableHelp.padLeft(Param.TILES_INTRO_X * Param.TILE_S);
    final int nScreens = 6;
    tableHelp.padBottom((Param.DISPLAY_Y * Param.TILES_INTRO_ZOOM * nScreens) + 101); //TODO un-magic this number

    if (Param.DEBUG_INITIAL > 0) {
      tableHelp.debugAll();
    }

    Vector<Container<Table>> helpContainers = new Vector<Container<Table>>();
    for (int i = 0; i < nScreens; ++i) {
      Container<Table> c = new Container<Table>();
      c.width(Param.DISPLAY_X * Param.TILES_INTRO_ZOOM).height(Param.DISPLAY_Y * Param.TILES_INTRO_ZOOM);
      tableHelp.add(c);
      tableHelp.row();
      c.setActor(new Table());
      c.getActor().top().left();
      helpContainers.add(c);
    }


    Table h0 = helpContainers.get(0).getActor();
    LabelDF vev = new LabelDF("VEV", ui.skin, "title", ui.dfShader_large);
    vev.setFontScale(6.4f);
    h0.add(vev).padLeft(16).left();
    h0.row();
    //
    addHelpLabel(h0, Lang.get("UI_HELP_00#"+Param.VERSION), 16);
    addHelpLabel(h0, Lang.get("UI_HELP_01"), 16);
    addHelpLabel(h0, Lang.get("UI_HELP_02"), 32);
    addHelpLabel(h0, Lang.get("UI_HELP_03"), 16);

    Table h1 = helpContainers.get(2).getActor();
    int h1pad = 128;
    if (Param.IS_ANDROID) {
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_00"), h1pad, 32);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_01"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_02"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_03"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_04"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_05"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_06"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_07"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_08"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_ANDROID_A_09"), h1pad + 64);
    } else {
      addHelpLabel(h1, Lang.get("UI_HELP_A_00"), h1pad, 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_01"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_02"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_03"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_04"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_05"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_06"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_07"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_08"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_09"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_10"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_11"), h1pad + 64);
      addHelpLabel(h1, Lang.get("UI_HELP_A_12"), h1pad + 32);
      addHelpLabel(h1, Lang.get("UI_HELP_A_13"), h1pad + 64);
    }

    Table h2 = helpContainers.get(3).getActor();
    addHelpLabel(h2, Lang.get("UI_HELP_B_00"), 16, 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_01"), 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_02"), 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_03"), 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_04"), 230, 30);
    addHelpLabel(h2, Lang.get("UI_HELP_B_05"), 180);
    addHelpLabel(h2, Lang.get("UI_HELP_B_06"), 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_07"), 16);
    addHelpLabel(h2, Lang.get("UI_HELP_B_08"), 160, 20);
    addHelpLabel(h2, Lang.get("UI_HELP_B_09"), 140, 50);

    Table h3 = helpContainers.get(4).getActor();
    addHelpLabel(h3, Lang.get("UI_HELP_C_00"), 16, 16);
    addHelpLabel(h3, Lang.get("UI_HELP_C_01"), 16);
    addHelpLabel(h3, Lang.get("UI_HELP_C_02"), 295, 30);
    addHelpLabel(h3, Lang.get("UI_HELP_C_03"), 255);
    addHelpLabel(h3, Lang.get("UI_HELP_C_04"), 230);
    addHelpLabel(h3, Lang.get("UI_HELP_C_05"), 90);
    addHelpLabel(h3, Lang.get("UI_HELP_C_06"), 90);
    addHelpLabel(h3, Lang.get("UI_HELP_C_07"), 90, 20);
    addHelpLabel(h3, Lang.get("UI_HELP_C_08"), 90);
    addHelpLabel(h3, Lang.get("UI_HELP_C_09"), 90);
    addHelpLabel(h3, Lang.get("UI_HELP_C_10"), 150, 25);
    addHelpLabel(h3, Lang.get("UI_HELP_C_11"), 90, 15);

    Table h4 = helpContainers.get(5).getActor();
    addHelpLabel(h4, Lang.get("UI_HELP_D_00"), h1pad, 20);
    addHelpLabel(h4, Lang.get("UI_HELP_D_01"), h1pad);
    addHelpLabel(h4, Lang.get("UI_HELP_D_02"), h1pad + 32);
    addHelpLabel(h4, Lang.get("UI_HELP_D_03"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_04"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_05"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_06"), h1pad + 32);
    addHelpLabel(h4, Lang.get("UI_HELP_D_07"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_08"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_09"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_10"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_11"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_12"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_13"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_14"), h1pad + 64);
    addHelpLabel(h4, Lang.get("UI_HELP_D_15"), h1pad + 64);

  }

  public void retextureSprites() {
    for (Image i : ballImages) {
      String tex = (String) i.getUserObject();
      i.setDrawable(new TextureRegionDrawable(Textures.getInstance().getTexture(tex, false)));
    }
  }

  public void cancelSettingsChanges() {
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      Persistence.getInstance().particleHues.put(p, cacheHue.get(p));
    }
    Textures.getInstance().updateParticleHues();
    Persistence.getInstance().musicLevel = cacheMusic;
    Persistence.getInstance().sfxLevel = cacheSfx;
    Persistence.getInstance().vibrate = cacheVibrate;
    Sounds.getInstance().musicVolume();
//    if (cacheFullscreen != Gdx.graphics.isFullscreen()) {
//      fsListener.set(cacheFullscreen, false);
//    }
  }

  private LabelDF helpLabel(String s) {
    LabelDF l = new LabelDF(s, UI.getInstance().skin, "wide", UI.getInstance().dfShader_medium);
    l.setFontScale(0.3f);
    return l;
  }

  private void addHelpLabel(Table t, String s, float left, float top) {
    t.add(helpLabel(s)).padTop(top).padLeft(left).left().row();
  }

  private void addHelpLabel(Table t, String s, float left) {
    addHelpLabel(t, s, left, 2);
  }

  public void resetTitle(String toShow) {
    final UI ui = UI.getInstance();

    // We only need to generate this once
    if (tableHelp == null) {
      reset();
    }

    newGameDiag = new NewGameDiag("", ui.skin);
    newGameButton.setDiag(newGameDiag);

    Table titleWindow = ui.getWindow();
    if (toShow.equals("main")) {
      windowMain(titleWindow);
    } else if (toShow.equals("help") || toShow.equals("credit")) {
      windowHelpCredits(titleWindow, toShow.equals("help"));
    } else if (toShow.equals("settings")) {
      windowSettings(titleWindow);
    } else {
      Gdx.app.error("Intro UI", "Unknown mode " + toShow);
      Gdx.app.exit();
    }

    tableIntro = new Table();
    tableIntro.setFillParent(true);
    if (Param.DEBUG_INITIAL > 0) {
      tableIntro.debugAll();
    }

    tableIntro.pad(Param.TILE_S * 2);
    tableIntro.top().right();
    tableIntro.add(titleWindow);

    Stage uiStage = IntroState.getInstance().getUIStage();
    uiStage.clear();
    uiStage.addActor(tableIntro);

    Table gParent = new Table();
    gParent.setFillParent(true);
    gParent.pad(Param.TILE_S * 2);
    gParent.top().left();
    generating = ui.getWindow();
    generating.add(ui.getLabel(Lang.get("UI_GENERATING"), "")).width(ui.SIZE_L * 2).height(ui.SIZE_M);
    gParent.add(generating);

    Stage genStage = IntroState.getInstance().getIntroGeneratingStage();
    genStage.clear();
    genStage.addActor(gParent);

    Stage helpStage = IntroState.getInstance().getIntroHelpStage();
    helpStage.clear();
    helpStage.addActor(tableHelp);

  }

  private void windowMain(Table titleWindow) {
    final UI ui = UI.getInstance();
    ui.uiMode = UIMode.kNONE;

    final Button newGame = ui.getTextButton(Lang.get("UI_NEW"), "newGame");
    newGame.addListener(newGameButton);
    titleWindow.add(newGame).pad(ui.SIZE_S / 2).colspan(2).fillX();

    titleWindow.row();
    ui.separator(titleWindow, 1);
    Button loadGame = ui.getTextButton(Lang.get("UI_LOAD"), "loadGame");
    loadGame.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Sounds.getInstance().OK();
        World.getInstance().doLoad = true;
      }
    });
    if (Persistence.getInstance().save == null) {
      Gdx.app.log("windowMain", "No save game, LOAD disabled");
      loadGame.setDisabled(true);
      World.getInstance().requestGenerate();
    } else {
      Gdx.app.log("windowMain", "Found save game, LOAD enabled");
      loadGame.setDisabled(false);
    }
    titleWindow.add(loadGame).pad(ui.SIZE_S / 2).colspan(1).fillX();

    titleWindow.row();
    ui.separator(titleWindow, 1);
    Button settingsButton = ui.getTextButton(Lang.get("UI_SETTINGS"), "settings");
    settingsButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Sounds.getInstance().OK();
        resetTitle("settings");
      }
    });
    titleWindow.add(settingsButton).pad(ui.SIZE_S / 2).colspan(1).fillX();

    titleWindow.row();
    ui.separator(titleWindow, 1);
    Button howToPlayButton = ui.getTextButton(Lang.get("UI_HOW"), "howToPlay");
    howToPlayButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Sounds.getInstance().OK();
        resetTitle("help");
      }
    });
    titleWindow.add(howToPlayButton).pad(ui.SIZE_S / 2).colspan(1).fillX();

    titleWindow.row();
    ui.separator(titleWindow, 1);
    Button credit = ui.getTextButton(Lang.get("UI_CREDITS"), "credits");
    credit.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Sounds.getInstance().OK();
        resetTitle("credit");
      }
    });
    titleWindow.add(credit).pad(ui.SIZE_S / 2).colspan(1).fillX();

    titleWindow.row();
    ui.separator(titleWindow, 1);
    Button exitGame = ui.getTextButton(Lang.get("UI_EXIT"), "exit");
    exitGame.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Gdx.app.exit();
      }
    });
    titleWindow.add(exitGame).pad(ui.SIZE_S / 2).colspan(1).fillX();
  }

  private void windowSettings(Table titleWindow) {
    final UI ui = UI.getInstance();
    ui.uiMode = UIMode.kSETTINGS;

    if (Param.DEBUG_INITIAL > 0) {
      titleWindow.debugAll();
    }

    titleWindow.left();
    ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_MUSIC"), ""), ui.SIZE_L, ui.SIZE_S, 4);
    Slider musicSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
    musicSlider.setValue(Persistence.getInstance().musicLevel);
    musicSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Persistence.getInstance().musicLevel = ((Slider) actor).getValue();
        Sounds.getInstance().musicVolume();
        Sounds.getInstance().click();
      }
    });
    ui.addToWin(titleWindow, musicSlider, ui.SIZE_L, ui.SIZE_S, 6);

    titleWindow.row();
    titleWindow.left();
    ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_SFX"), ""), ui.SIZE_L, ui.SIZE_S, 4);
    Slider sfxSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
    sfxSlider.setValue(Persistence.getInstance().sfxLevel);
    sfxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Persistence.getInstance().sfxLevel = ((Slider) actor).getValue();
        Sounds.getInstance().click();
      }
    });
    ui.addToWin(titleWindow, sfxSlider, ui.SIZE_L, ui.SIZE_S, 6);

    titleWindow.row();
    titleWindow.left();
//    fsBox = new CheckBox("", ui.skin);
//    fsBox.setChecked(Gdx.graphics.isFullscreen());
//    fsBox.addListener(fsListener);
//    fsBox.getImage().setScaling(Scaling.fill);
//    fsBox.getImageCell().size(ui.SIZE_S);
//    if (!Param.IS_ANDROID) {
//      ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_FULLSCREEN"), ""), ui.SIZE_L, ui.SIZE_S, 7);
//      ui.addToWin(titleWindow, fsBox, ui.SIZE_S, ui.SIZE_S, 3);
//      titleWindow.row();
//    }

    vibBox = new CheckBox("", ui.skin);
    vibBox.setChecked(Gdx.graphics.isFullscreen());
    vibBox.addListener(fsListener);
    vibBox.getImage().setScaling(Scaling.fill);
    vibBox.getImageCell().size(ui.SIZE_S);
    if (Param.IS_ANDROID) {
      ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_VIBRATE"), ""), ui.SIZE_L, ui.SIZE_S, 4);
      ui.addToWin(titleWindow, vibBox, ui.SIZE_S, ui.SIZE_S, 6);
      titleWindow.row();
    }

    ui.separator(titleWindow, 10, Param.UI_WIDTH_INTRO);

    ballImages.clear();
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      assert p.getColourFromParticle() != null;
      Image i = ui.getImage("ball_" + p.getColourFromParticle().getString(), "");
      ballImages.add(i);
      ui.addToWin(titleWindow, ui.getLabel(p.getString(), ""), ui.SIZE_S, ui.SIZE_S, 2);
      ui.addToWin(titleWindow, i, ui.SIZE_M, ui.SIZE_M, 2);
      Slider pSlider = new Slider(0, 360 * 3, 1, false, ui.skin, "default-horizontal");
      pSlider.setValue(Persistence.getInstance().particleHues.get(p));
      pSlider.setUserObject(p);
      pSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Persistence.getInstance().particleHues.put((Particle) actor.getUserObject(), (int) ((Slider) actor).getValue());
          Textures.getInstance().updateParticleHues();
          Sounds.getInstance().click();
        }
      });
      ui.addToWin(titleWindow, pSlider, ui.SIZE_L * 2, ui.SIZE_M, 6);
      titleWindow.row();
      cacheHue.put(p, Persistence.getInstance().particleHues.get(p));
    }

    cacheMusic = Persistence.getInstance().musicLevel;
    cacheSfx = Persistence.getInstance().sfxLevel;
    cacheVibrate = Persistence.getInstance().vibrate;
    cacheFullscreen = Gdx.graphics.isFullscreen();

    ui.separator(titleWindow, 10, Param.UI_WIDTH_INTRO);

    ui.addToWin(titleWindow, ui.getImageButton("tick", ""), ui.SIZE_L, ui.SIZE_L, 5);
    ui.addToWin(titleWindow, ui.getImageButton("cross", ""), ui.SIZE_L, ui.SIZE_L, 5);
  }

  private void windowHelpCredits(Table titleWindow, final boolean helpMode) {
    final UI ui = UI.getInstance();
    ui.uiMode = UIMode.kHELP;
    helpLevel = helpMode ? 2 : 5;
    Camera.getInstance().setHelpPos(helpLevel, false);
    if (helpMode) {
      ui.addToWin(titleWindow, ui.getTextButton("<", ""), ui.SIZE_L, ui.SIZE_L, 1);
      ui.addToWin(titleWindow, ui.getTextButton(">", ""), ui.SIZE_L, ui.SIZE_L, 1);
      titleWindow.row();
    }
    ui.addToWin(titleWindow, ui.getTextButton(Lang.get("UI_BACK"), ""), (ui.SIZE_L + ui.PAD) * 2, ui.SIZE_L, helpMode ? 2 : 1);

  }
}
