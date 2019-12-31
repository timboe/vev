package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import timboe.vev.DistanceField.LabelDF;
import timboe.vev.Lang;
import timboe.vev.Param;
import timboe.vev.enums.Particle;
import timboe.vev.enums.UIMode;

public class IntroUI {
  private static IntroUI ourInstance = null;
  public static IntroUI getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new IntroUI(); }
  public static boolean constructed() { return ourInstance != null; }
  public void dispose() { ourInstance = null; }

  private Set<Image> ballImages = new HashSet<Image>();

  // Settings cache
  private float cacheMusic, cacheSfx;
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
      Param.PARTICLE_HUE.put(p, cacheHue.get(p));
    }
    Textures.getInstance().updateParticleHues();
    Param.MUSIC_LEVEL = cacheMusic;
    Param.SFX_LEVEL = cacheSfx;
  }

  protected void resetTitle(String toShow) {
    final UI ui = UI.getInstance();
    Table tableIntro = new Table();
    tableIntro.setFillParent(true);
    tableIntro.debugAll();
    tableIntro.left();
    tableIntro.row().fillX();
    tableIntro.pad(Param.TILE_S * 2);

    GameState.getInstance().getUIStage().clear();
    GameState.getInstance().getUIStage().addActor(tableIntro);


    LabelDF vev = new LabelDF("VEV", ui.skin, "title", ui.dfShader_large);
    vev.setFontScale(25f);
    tableIntro.add(vev).padLeft(45);

    tableIntro.add(new Actor()).expand();

    Table titleWindow = ui.getWindow();

    if (toShow.equals("main")) {

      ui.uiMode = UIMode.kNONE;

      final Button newGame = ui.getTextButton(Lang.get("UI_NEW"), "newGame");
      newGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Dialog newGameDialog = new Dialog("", ui.skin) {
            protected void result(Object object) {
              if ((Integer)object >= 0) {
                GameState.getInstance().difficulty = (Integer)object;
                switch ((Integer)object) {
                  case 0: GameState.getInstance().warpParticles = Param.PARTICLES_SMALL; break;
                  case 1: GameState.getInstance().warpParticles = Param.PARTICLES_MED; break;
                  case 2: GameState.getInstance().warpParticles = Param.PARTICLES_LARGE; break;
                  case 3: GameState.getInstance().warpParticles = Param.PARTICLES_XL; break;
                  default: Gdx.app.error("NewGame", "Unknown button " + object);
                }
                if (!World.getInstance().getGenerated()) World.getInstance().launchAfterGen = true;
                else if (GameState.getInstance().theTitleScreen.fadeTimer == 0) {
                  GameState.getInstance().transitionToGameScreen();
                }
              }
            };
          };
          newGameDialog.pad(ui.PAD*4);
          newGameDialog.align(Align.center);
          newGameDialog.text(ui.getLabel(Lang.get("UI_GAME_LENGTH"),""));
          newGameDialog.button(ui.getTextButton(Lang.get("UI_SHORT"),"UI_N_PARTICLES#"+Integer.toString(Param.PARTICLES_SMALL)), 0);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_MED"),"UI_N_PARTICLES#"+Integer.toString(Param.PARTICLES_MED)), 1);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_LONG"),"UI_N_PARTICLES#"+Integer.toString(Param.PARTICLES_LARGE)), 2);
          newGameDialog.button(ui.getTextButton(Lang.get("UI_XL"),"UI_N_PARTICLES#"+Integer.toString(Param.PARTICLES_XL)), 3);
          newGameDialog.getButtonTable().row();
          Button c = ui.getTextButton(Lang.get("UI_CANCEL"),"");
          newGameDialog.button(c, -1);
          newGameDialog.getButtonTable().getCell(c).colspan(4);
          newGameDialog.key(Input.Keys.ENTER, 5).key(Input.Keys.ESCAPE, 0);
          newGameDialog.show(GameState.getInstance().getUIStage());
        }
      });
      titleWindow.add(newGame).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button loadGame = ui.getTextButton(Lang.get("UI_LOAD"),"loadGame");
      loadGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          World.getInstance().doLoad = true;
        }
      });
      if (World.getInstance().loadedSave == null) {
        loadGame.setDisabled(true);
      }
      titleWindow.add(loadGame).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button settingsButton = ui.getTextButton(Lang.get("UI_SETTINGS"),"settings");
      settingsButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          resetTitle("settings");
        }
      });
      titleWindow.add(settingsButton).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button howToPlayButton = ui.getTextButton(Lang.get("UI_HOW"),"");
      howToPlayButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
        }
      });
      titleWindow.add(howToPlayButton).pad(ui.SIZE_S).colspan(2).fillX();

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);
      Button exitGame = ui.getTextButton(Lang.get("UI_EXIT"),"");
      exitGame.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Gdx.app.exit();
        }
      });
      titleWindow.add(exitGame).pad(ui.SIZE_S).colspan(2).fillX();

    } else if (toShow.equals("settings")) {

      ui.uiMode = UIMode.kSETTINGS;

      LabelDF musicL = ui.getLabel("M", "musicVolume");
      titleWindow.add(musicL).pad(ui.SIZE_S).fillX();
      Slider musicSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
      musicSlider.setValue(Param.MUSIC_LEVEL);
      musicSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Param.MUSIC_LEVEL = ((Slider)actor).getValue();
          Sounds.getInstance().musicVolume();
        }
      });
      titleWindow.add(musicSlider).pad(ui.SIZE_S).width(ui.SIZE_S+ui.SIZE_M+ui.SIZE_L).fillX();

      titleWindow.row();
      LabelDF sfxL = ui.getLabel("S", "sfxVolume");
      titleWindow.add(sfxL).pad(ui.SIZE_S).fillX();
      Slider sfxSlider = new Slider(0, 1, .01f, false, ui.skin, "default-horizontal");
      sfxSlider.setValue(Param.SFX_LEVEL);
      sfxSlider.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          Param.SFX_LEVEL = ((Slider)actor).getValue();
        }
      });
      ui.addToWin(titleWindow, sfxSlider, ui.SIZE_S+ui.SIZE_M+ui.SIZE_L, ui.SIZE_S, 1);

      titleWindow.row();
      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);

      ballImages.clear();
      for (Particle p : Particle.values()) {
        if (p == Particle.kBlank) continue;
        assert p.getColourFromParticle() != null;
        Image i = ui.getImage("ball_" + p.getColourFromParticle().getString(),"");
        ballImages.add(i);
        ui.addToWin(titleWindow, i, ui.SIZE_M);
        Slider pSlider = new Slider(0, 360*3, 1, false, ui.skin, "default-horizontal");
        pSlider.setValue( Param.PARTICLE_HUE.get(p) );
        pSlider.setUserObject(p);
        pSlider.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            Param.PARTICLE_HUE.put( (Particle)actor.getUserObject(), (int) ((Slider)actor).getValue() );
            Textures.getInstance().updateParticleHues();
          }
        });
        ui.addToWin(titleWindow, pSlider, ui.SIZE_M+ui.SIZE_L, ui.SIZE_M, 1);
        titleWindow.row();
        cacheHue.put(p, Param.PARTICLE_HUE.get(p));
      }

      cacheMusic = Param.MUSIC_LEVEL;
      cacheSfx = Param.SFX_LEVEL;

      ui.separator(titleWindow, 2, Param.UI_WIDTH_INTRO);

      ui.addToWin(titleWindow, ui.getImageButton("tick",""), ui.SIZE_L, ui.SIZE_L, 1);
      ui.addToWin(titleWindow, ui.getImageButton("cross",""), ui.SIZE_L, ui.SIZE_L, 1);


    } else {
      Gdx.app.error("Intro UI","Unknown mode "+toShow);
      Gdx.app.exit();
    }


    tableIntro.add(titleWindow).right().top();

    LabelDF cred = ui.getLabel("A game by Tim Martin", "");
    LabelDF music1 = ui.getLabel("Music by Chris Zabriskie (CC v4)", "");
    LabelDF music2 = ui.getLabel("    Is That You Or Are You You? / Divider / CGI Snake", "");
    LabelDF art = ui.getLabel("Open Game Art by Buch", "");

    tableIntro.row();
    tableIntro.bottom().left();
    tableIntro.add(cred).left().colspan(3);
    tableIntro.row();
    tableIntro.add(music1).left().colspan(3);
    tableIntro.row();
    tableIntro.add(music2).left().colspan(3);
    tableIntro.row();
    tableIntro.add(art).left().colspan(3);

    Gdx.app.log("resetTitle", "made intro UI");
  }
}
