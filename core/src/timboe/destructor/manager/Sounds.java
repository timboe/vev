package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

import timboe.destructor.Util;

public class Sounds {

  private final Random R = new Random();

  private final int nMove = 6, nSelect = 3, nMusic = 3;
  private final Sound[] move = new Sound[nMove];
  private final Sound[] select = new Sound[nSelect];
  private Sound foot;
  private Music[] theme = new Music[nMusic];

  private int track;
  private boolean sfx = true, music = false;

  private static Sounds ourInstance;
  public static Sounds getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sounds(); }

  public void dispose() {
    foot.dispose();
    for (int i = 0; i < nMusic; ++i) theme[i].dispose();
    for (int i = 0; i < nMove; ++i) move[i].dispose();
    for (int i = 0; i < nSelect; ++i) select[i].dispose();
    ourInstance = null;
  }

  private Sounds() {
    reset();
  }

  public void toggleMusic() {
    music = !music;
    doMusic();
  }

  public void doMusic() {
    if (music) {
      theme[track].play();
      int trackTemp = track;
      while (trackTemp == track) trackTemp = Util.R.nextInt(nMusic);
      track = trackTemp; // Random - but not the same
    } else {
      theme[track].stop();
      track = 0;
    }
  }

  public void moveOrder() {
    if (!sfx) return;
    move[ R.nextInt(nMove) ].play();
  }

  public void selectOrder() {
    if (!sfx) return;
    select[ R.nextInt(nSelect) ].play();
  }

  public void foot() {
    return;
//    foot.play();
  }

  public void reset() {
    track = 0;
    for (int i = 0; i < nMusic; ++i) {
      String path = "IsThatYouorAreYouYou.ogg";
      if (i == 1) path = "CGISnake.ogg";
      else if (i == 2) path = "Divider.ogg";
      theme[i] = Gdx.audio.newMusic(Gdx.files.internal(path));
      theme[i].setOnCompletionListener(new Music.OnCompletionListener() {
        @Override
        public void onCompletion(Music music) {
          doMusic();
        }
      });
    }
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
  }

}
