package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.vev.DistanceField.LabelDF;
import timboe.vev.Lang;
import timboe.vev.Param;
import timboe.vev.enums.Particle;
import timboe.vev.enums.UIMode;
import timboe.vev.input.FullscreenToggle;

public class UIIntro {
  private static UIIntro ourInstance = null;
  public static UIIntro getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new UIIntro(); }
  public static boolean constructed() { return ourInstance != null; }
  public void dispose() { ourInstance = null; }

  private Set<Image> ballImages = new HashSet<Image>();

  public FullscreenToggle fsListener = new FullscreenToggle();

  public int helpLevel = 1;

  public Table tableIntro = null;
  public Table tableHelp = null;

  // Settings cache
  private float cacheMusic, cacheSfx;
  private boolean cacheFullscreen;
  private EnumMap<Particle, Integer> cacheHue = new EnumMap<Particle, Integer>(Particle.class);

  public void retextureSprites() {
    for (Image i : ballImages) {
      String tex = (String)i.getUserObject();
      i.setDrawable( new TextureRegionDrawable( Textures.getInstance().getTexture(tex, false) ) );
    }
  }

  public void cancelSettingsChanges() {
    Gdx.app.log("cancelSettingsChanges","Cancelling");
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      Persistence.getInstance().particleHues.put(p, cacheHue.get(p));
    }
    Textures.getInstance().updateParticleHues();
    Persistence.getInstance().musicLevel = cacheMusic;
    Persistence.getInstance().sfxLevel = cacheSfx;
    Sounds.getInstance().musicVolume();
    if (cacheFullscreen != Gdx.graphics.isFullscreen()) {
      fsListener.set(cacheFullscreen);
    }
  }

  protected void resetTitle(String toShow) {
    final UI ui = UI.getInstance();

    tableIntro = new Table();
    tableIntro.setFillParent(true);
    tableHelp = new Table();
    tableHelp.setFillParent(true);

    if (Param.DEBUG_INITIAL > 0) {
      tableIntro.debugAll();
    }

    tableHelp.top().left();
//    tableHelp.row().fillX();
    tableHelp.pad(Param.TILE_S * 2);

    LabelDF vev = new LabelDF("VEV", ui.skin, "title", ui.dfShader_large);
    vev.setFontScale(6.5f);
    tableHelp.add(vev).padLeft(256).padTop(400);
    tableHelp.row();

    LabelDF cred = ui.getLabel("A game by Tim Martin", "");
    LabelDF music1 = ui.getLabel("Music by Chris Zabriskie (CC v4)", "");
    LabelDF music2 = ui.getLabel("    Is That You Or Are You You? / Divider / CGI Snake", "");
    LabelDF art = ui.getLabel("Open Game Art by Buch", "");
    cred.setFontScale(0.3f);
    music1.setFontScale(0.3f);
    music2.setFontScale(0.3f);
    art.setFontScale(0.3f);
    tableHelp.add(cred).padLeft(256).left();
    tableHelp.row();
    tableHelp.add(music1).padLeft(256).left();
    tableHelp.row();
    tableHelp.add(music2).padLeft(256).left();
    tableHelp.row();
    tableHelp.add(art).padLeft(256).left();

    IntroState.getInstance().getIntroHelpStage().clear();
    IntroState.getInstance().getIntroHelpStage().addActor(tableHelp);

    IntroState.getInstance().getUIStage().clear();
    IntroState.getInstance().getUIStage().addActor(tableIntro);


    Table titleWindow = ui.getWindow();
    if (toShow.equals("main")) {

      ui.uiMode = UIMode.kNONE;

      final Button newGame = ui.getTextButton(Lang.get("UI_NEW"), "newGame");
      newGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Dialog newGameDialog = new Dialog("", ui.skin) {
            protected void result(Object object) {
              if ((Integer) object >= 0) {
                GameState.getInstance().difficulty = (Integer) object;
                switch ((Integer) object) {
                  case 0:
                    GameState.getInstance().warpParticles = Param.PARTICLES_SMALL;
                    break;
                  case 1:
                    GameState.getInstance().warpParticles = Param.PARTICLES_MED;
                    break;
                  case 2:
                    GameState.getInstance().warpParticles = Param.PARTICLES_LARGE;
                    break;
                  case 3:
                    GameState.getInstance().warpParticles = Param.PARTICLES_XL;
                    break;
                  default:
                    Gdx.app.error("NewGame", "Unknown button " + object);
                }
                if (!World.getInstance().getGenerated()) World.getInstance().launchAfterGen = true;
                else GameState.getInstance().transitionToGameScreen();
              } else {
                Gdx.app.log("result","Pressed CANCEL");
              }
            }
          };
          newGameDialog.pad(ui.PAD * 4);
          newGameDialog.align(Align.center);
          newGameDialog.text(ui.getLabel(Lang.get("UI_GAME_LENGTH"), ""));
          newGameDialog.button(ui.getTextButton(Lang.get("UI_SHORT"), "UI_N_PARTICLES#" + Param.PARTICLES_SMALL), 0);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_MED"), "UI_N_PARTICLES#" + Param.PARTICLES_MED), 1);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_LONG"), "UI_N_PARTICLES#" + Param.PARTICLES_LARGE), 2);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_XL"), "UI_N_PARTICLES#" + Param.PARTICLES_XL), 3);
          newGameDialog.getButtonTable().row();
          Button c = ui.getTextButton(Lang.get("UI_CANCEL"), "");
          newGameDialog.button(c, -1);
          newGameDialog.getButtonTable().getCell(c).colspan(4);
          newGameDialog.key(Input.Keys.ENTER, 5).key(Input.Keys.ESCAPE, 0);
          newGameDialog.show(IntroState.getInstance().getUIStage());
        }
      });
      titleWindow.add(newGame).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button loadGame = ui.getTextButton(Lang.get("UI_LOAD"), "loadGame");
      loadGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          World.getInstance().doLoad = true;
        }
      });
      if (Persistence.getInstance().save == null) {
        loadGame.setDisabled(true);
      }
      titleWindow.add(loadGame).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button settingsButton = ui.getTextButton(Lang.get("UI_SETTINGS"), "settings");
      settingsButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          resetTitle("settings");
        }
      });
      titleWindow.add(settingsButton).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button howToPlayButton = ui.getTextButton(Lang.get("UI_HOW"), "");
      howToPlayButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          resetTitle("help");
        }
      });
      titleWindow.add(howToPlayButton).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button exitGame = ui.getTextButton(Lang.get("UI_EXIT"), "");
      exitGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Gdx.app.exit();
        }
      });
      titleWindow.add(exitGame).pad(ui.SIZE_S).colspan(2).fillX();

    } else if (toShow.equals("help")) {

      ui.uiMode = UIMode.kHELP;
      helpLevel = 1;
      Camera.getInstance().setHelpPos(helpLevel);

      ui.addToWin(titleWindow, ui.getTextButton("<",""), ui.SIZE_L, ui.SIZE_L, 1);
      ui.addToWin(titleWindow, ui.getTextButton(">",""), ui.SIZE_L, ui.SIZE_L, 1);

    } else if (toShow.equals("settings")) {

      ui.uiMode = UIMode.kSETTINGS;

      ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_MUSIC"), ""), ui.SIZE_L, ui.SIZE_S, 2);
      Slider musicSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
      musicSlider.setValue(Persistence.getInstance().musicLevel);
      musicSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Persistence.getInstance().musicLevel = ((Slider)actor).getValue();
          Sounds.getInstance().musicVolume();
          Sounds.getInstance().click();
        }
      });
      titleWindow.add(musicSlider).pad(ui.SIZE_S).width(ui.SIZE_S+ui.SIZE_M+ui.SIZE_L).fillX();

      titleWindow.row();
      ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_SFX"), ""), ui.SIZE_L, ui.SIZE_S, 2);
      Slider sfxSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
      sfxSlider.setValue(Persistence.getInstance().sfxLevel);
      sfxSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Persistence.getInstance().sfxLevel = ((Slider)actor).getValue();
          Sounds.getInstance().click();
        }
      });
      ui.addToWin(titleWindow, sfxSlider, ui.SIZE_S+ui.SIZE_M+ui.SIZE_L, ui.SIZE_S, 1);

      titleWindow.row();
      ui.addToWin(titleWindow, ui.getLabel(Lang.get("UI_FULLSCREEN"), ""), ui.SIZE_L, ui.SIZE_S, 2);
      CheckBox fsBox = new CheckBox("", ui.skin);
      fsBox.setChecked( Gdx.graphics.isFullscreen() );
      fsBox.addListener(fsListener);
      fsBox.getImage().setScaling(Scaling.fill);
      fsBox.getImageCell().size(ui.SIZE_S);
      ui.addToWin(titleWindow, fsBox, ui.SIZE_S, ui.SIZE_S, 1);

      titleWindow.row();
      ui.separator(titleWindow, 3, Param.UI_WIDTH_INTRO);

      ballImages.clear();
      for (Particle p : Particle.values()) {
        if (p == Particle.kBlank) continue;
        assert p.getColourFromParticle() != null;
        Image i = ui.getImage("ball_" + p.getColourFromParticle().getString(),"");
        ballImages.add(i);
        ui.addToWin(titleWindow, ui.getLabel(p.getString(),""), ui.SIZE_S);
        ui.addToWin(titleWindow, i, ui.SIZE_M);
        Slider pSlider = new Slider(0, 360*3, 1, false, ui.skin, "default-horizontal");
        pSlider.setValue( Persistence.getInstance().particleHues.get(p) );
        pSlider.setUserObject(p);
        pSlider.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Persistence.getInstance().particleHues.put( (Particle)actor.getUserObject(), (int) ((Slider)actor).getValue() );
            Textures.getInstance().updateParticleHues();
            Sounds.getInstance().click();
          }
        });
        ui.addToWin(titleWindow, pSlider, ui.SIZE_M+ui.SIZE_L, ui.SIZE_M, 1);
        titleWindow.row();
        cacheHue.put(p, Persistence.getInstance().particleHues.get(p));
      }

      cacheMusic = Persistence.getInstance().musicLevel;
      cacheSfx = Persistence.getInstance().sfxLevel;
      cacheFullscreen = Gdx.graphics.isFullscreen();

      ui.separator(titleWindow, 3, Param.UI_WIDTH_INTRO);

      ui.addToWin(titleWindow, ui.getImageButton("tick",""), ui.SIZE_L, ui.SIZE_L, 1);
      ui.addToWin(titleWindow, ui.getImageButton("cross",""), ui.SIZE_L, ui.SIZE_L, 1);


    } else {
      Gdx.app.error("Intro UI","Unknown mode "+toShow);
      Gdx.app.exit();
    }

    tableIntro.row().fillY();
    tableIntro.top();
    tableIntro.right();
    tableIntro.pad(Param.TILE_S*2);
    tableIntro.add(titleWindow);



    Gdx.app.log("resetTitle", "made intro UI");
  }
}
