package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

import timboe.vev.Util;

public class Sounds {

  private final Random R = new Random();

  private final int nMove = 6, nSelect = 3, nMusic = 3, nElectric = 3, nFW = 2, nBoom = 3;
  private final Sound[] move = new Sound[nMove];
  private final Sound[] electric = new Sound[nElectric];
  private final Sound[] select = new Sound[nSelect];
  private final Sound[] woosh = new Sound[nFW];
  private final Sound[] boom = new Sound[nBoom];
  private Sound foot;
  private Sound blop;
  private Sound pulse;
  private Sound swoosh;
  private Sound thud;
  private Sound star;
  private Sound OK;
  private Sound cancel;
  private Sound click;
  private Sound poof;
  private Sound demolish;
  private Sound fanfare;
  private Sound processing;
  private long processingID;
  private Sound dirt;
  private Music[] theme = new Music[nMusic];

  private int track;
  private float sfxMod = 1f;
  private boolean paused = false;

  private static Sounds ourInstance;

  public static Sounds getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Sounds();
  }

  public void dispose() {
    foot.dispose();
    swoosh.dispose();
    blop.dispose();
    pulse.dispose();
    thud.dispose();
    star.dispose();
    OK.dispose();
    cancel.dispose();
    click.dispose();
    poof.dispose();
    demolish.dispose();
    fanfare.dispose();
    processing.dispose();
    dirt.dispose();
    for (int i = 0; i < nMusic; ++i) theme[i].dispose();
    for (int i = 0; i < nMove; ++i) move[i].dispose();
    for (int i = 0; i < nSelect; ++i) select[i].dispose();
    for (int i = 0; i < nElectric; ++i) electric[i].dispose();
    for (int i = 0; i < nFW; ++i) woosh[i].dispose();
    for (int i = 0; i < nBoom; ++i) boom[i].dispose();
    ourInstance = null;
  }

  public void sfxLevel(float level) {
    this.sfxMod = level;
  }

  private Sounds() {
    reset();
  }

  public void pause() {
    paused = true;
    theme[track].pause();
  }

  public void resume() {
    paused = false;
    theme[track].play();
  }

  public void musicVolume() {
    theme[track].setVolume(Persistence.getInstance().musicLevel);
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
    poof.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void click() {
    click.play(Persistence.getInstance().sfxLevel);
  }

  public void OK() {
    OK.play(Persistence.getInstance().sfxLevel);
  }

  public void cancel() {
    cancel.play(Persistence.getInstance().sfxLevel);
  }

  public void star() {
    star.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void thud() {
    thud.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void boop() {
    blop.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void fanfare() {
    fanfare.play(Persistence.getInstance().sfxLevel);
  }

  public void woosh() {
    if (paused) return;
    woosh[R.nextInt(nFW)].play(Persistence.getInstance().sfxLevel);
  }

  public void boom() {
    if (paused) return;
    boom[R.nextInt(nBoom)].play(Persistence.getInstance().sfxLevel);
  }

  public void zap() {
    electric[R.nextInt(nElectric)].play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void moveOrder() {
    move[R.nextInt(nMove)].play(Persistence.getInstance().sfxLevel);
  }

  public void selectOrder() {
    select[R.nextInt(nSelect)].play(Persistence.getInstance().sfxLevel);
  }

  public void foot() {
    foot.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void dirt() {
    dirt.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void demolish() {
    demolish.play(Persistence.getInstance().sfxLevel * sfxMod);
  }

  public void pulse() {
    swoosh.play(Persistence.getInstance().sfxLevel);
    pulse.play(Persistence.getInstance().sfxLevel);
  }

  public void processing(boolean enable) {
    if (enable) {
      processing.setVolume(processingID, Persistence.getInstance().sfxLevel * sfxMod);
    } else {
      processing.setVolume(processingID, 0);
    }
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
    cancel = Gdx.audio.newSound(Gdx.files.internal("405548__raclure__cancel-miss-chime.wav"));
    star = Gdx.audio.newSound(Gdx.files.internal("shooting_star-Mike_Koenig-1132888100.mp3"));
    thud = Gdx.audio.newSound(Gdx.files.internal("Thud-SoundBible.com-395560493.mp3"));
    swoosh = Gdx.audio.newSound(Gdx.files.internal("Swoosh 1-SoundBible.com-231145780.mp3"));
    pulse = Gdx.audio.newSound(Gdx.files.internal("138421__cameronmusic__pulse-1.wav"));
    blop = Gdx.audio.newSound(Gdx.files.internal("Blop-Mark_DiAngelo-79054334.mp3"));
    foot = Gdx.audio.newSound(Gdx.files.internal("365810__fxkid2__cute-walk-run-c.wav"));
    dirt = Gdx.audio.newSound(Gdx.files.internal("427074__doorajar__dirtshovel.wav"));
    processing = Gdx.audio.newSound(Gdx.files.internal("28783__voktebef__8bit.wav"));
    processingID = processing.loop(0);
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
    fanfare = Gdx.audio.newSound(Gdx.files.internal("321937__pel2na__two-kazoo-fanfare.wav"));
    woosh[0] = Gdx.audio.newSound(Gdx.files.internal("250200__selector__rocket-launch_1.wav"));
    woosh[1] = Gdx.audio.newSound(Gdx.files.internal("250200__selector__rocket-launch_2.wav"));
    boom[0] = Gdx.audio.newSound(Gdx.files.internal("250200__selector__rocket-launch_boom1.wav"));
    boom[1] = Gdx.audio.newSound(Gdx.files.internal("250200__selector__rocket-launch_boom2.wav"));
    boom[2] = Gdx.audio.newSound(Gdx.files.internal("250200__selector__rocket-launch_boom3.wav"));
  }

}
