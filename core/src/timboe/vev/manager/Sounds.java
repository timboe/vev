package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

import timboe.vev.Param;
import timboe.vev.Util;

public class Sounds {

  private final Random R = new Random();

  private final int nMove = 6, nSelect = 3, nMusic = 3, nElectric = 3;
  private final Sound[] move = new Sound[nMove];
  private final Sound[] electric = new Sound[nElectric];
  private final Sound[] select = new Sound[nSelect];
  private Sound foot;
  private Sound blop;
  private Sound pulse;
  private Sound swoosh;
  private Sound thud;
  private Sound star;
  private Sound OK;
  private Sound click;
  private Sound poof;
  private Sound demolish;
  private Music[] theme = new Music[nMusic];

  private int track;
  private float sfxMod = 1f;

  private static Sounds ourInstance;
  public static Sounds getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sounds(); }

  public void dispose() {
    foot.dispose();
    swoosh.dispose();
    blop.dispose();
    pulse.dispose();
    thud.dispose();
    star.dispose();
    OK.dispose();
    click.dispose();
    poof.dispose();
    demolish.dispose();
    for (int i = 0; i < nMusic; ++i) theme[i].dispose();
    for (int i = 0; i < nMove; ++i) move[i].dispose();
    for (int i = 0; i < nSelect; ++i) select[i].dispose();
    for (int i = 0; i < nElectric; ++i) electric[i].dispose();
    ourInstance = null;
  }

  public void sfxLevel(float level) {
    this.sfxMod = level;
  }

  private Sounds() {
    reset();
  }


  public void musicVolume() {
    Gdx.app.log("musicVolume","Now " + Param.MUSIC_LEVEL);
    theme[track].setVolume(Param.MUSIC_LEVEL);
  }

  public void doMusic(boolean initial) {
    if (!initial) {
      int trackTemp = track;
      while (trackTemp == track) trackTemp = Util.R.nextInt(nMusic);
      track = trackTemp; // Random - but not the same

    }
    theme[track].play();
    musicVolume();
  }

  public void poof() {
    poof.play(Param.SFX_LEVEL * sfxMod);
  }

  public void click() {
    click.play(Param.SFX_LEVEL);
  }

  public void OK() {
    OK.play(Param.SFX_LEVEL);
  }

  public void star() {
    star.play(Param.SFX_LEVEL * sfxMod);
  }

  public void thud() {
    thud.play(Param.SFX_LEVEL * sfxMod);
  }

  public void boop() {
    blop.play(Param.SFX_LEVEL * sfxMod);
  }

  public void zap() {
    electric[ R.nextInt(nElectric) ].play(Param.SFX_LEVEL * sfxMod);
  }

  public void moveOrder() {
    move[ R.nextInt(nMove) ].play(Param.SFX_LEVEL);
  }

  public void selectOrder() {
    select[ R.nextInt(nSelect) ].play(Param.SFX_LEVEL);
  }

  public void foot() {
    foot.play(Param.SFX_LEVEL * sfxMod);
  }

  public void demolish() { demolish.play( Param.SFX_LEVEL * sfxMod); }

  public void pulse() {
    swoosh.play(Param.SFX_LEVEL);
    pulse.play(Param.SFX_LEVEL);
  }


  private void reset() {
    track = 0;
    for (int i = 0; i < nMusic; ++i) {
      String path = "IsThatYouorAreYouYou.ogg";
      if (i == 1) path = "CGISnake.ogg";
      else if (i == 2) path = "Divider.ogg";
      theme[i] = Gdx.audio.newMusic(Gdx.files.internal(path));
      theme[i].setOnCompletionListener(new Music.OnCompletionListener() {
        @Override
        public void onCompletion(Music music) {
          doMusic(false);
        }
      });
    }
    poof = Gdx.audio.newSound(Gdx.files.internal("208111__planman__poof-of-smoke.wav"));
    click = Gdx.audio.newSound(Gdx.files.internal("399934__waveplay-old__short-click-snap-perc.wav"));
    OK = Gdx.audio.newSound(Gdx.files.internal("405547__raclure__affirmative-decision-chime.wav"));
    star = Gdx.audio.newSound(Gdx.files.internal("shooting_star-Mike_Koenig-1132888100.mp3"));
    thud = Gdx.audio.newSound(Gdx.files.internal("Thud-SoundBible.com-395560493.mp3"));
    swoosh = Gdx.audio.newSound(Gdx.files.internal("Swoosh 1-SoundBible.com-231145780.mp3"));
    pulse = Gdx.audio.newSound(Gdx.files.internal("138421__cameronmusic__pulse-1.wav"));
    blop = Gdx.audio.newSound(Gdx.files.internal("Blop-Mark_DiAngelo-79054334.mp3"));
    foot = Gdx.audio.newSound(Gdx.files.internal("365810__fxkid2__cute-walk-run-c.wav"));
    move[0] = Gdx.audio.newSound(Gdx.files.internal("379234__westington__skiffy1.wav"));
    move[1] = Gdx.audio.newSound(Gdx.files.internal("379233__westington__skiffy2.wav"));
    move[2] = Gdx.audio.newSound(Gdx.files.internal("379232__westington__skiffy3.wav"));
    move[3] = Gdx.audio.newSound(Gdx.files.internal("379231__westington__skiffy4.wav"));
    move[4] = Gdx.audio.newSound(Gdx.files.internal("379236__westington__skiffy7.wav"));
    move[5] = Gdx.audio.newSound(Gdx.files.internal("379239__westington__skiffy9.wav"));
    select[0] = Gdx.audio.newSound(Gdx.files.internal("379238__westington__skiffy5.wav"));
    select[1] = Gdx.audio.newSound(Gdx.files.internal("379237__westington__skiffy6.wav"));
    select[2] = Gdx.audio.newSound(Gdx.files.internal("379235__westington__skiffy8.wav"));
    electric[0] = Gdx.audio.newSound(Gdx.files.internal("Electric1-SoundBible.com-1439537520.mp3"));
    electric[1] = Gdx.audio.newSound(Gdx.files.internal("Electric2-SoundBible.com-742005847.mp3"));
    electric[2] = Gdx.audio.newSound(Gdx.files.internal("Electric3-SoundBible.com-1450168875.mp3"));
    demolish = Gdx.audio.newSound(Gdx.files.internal("441497__mattix__retro-explosion-05.wav"));
  }

}
