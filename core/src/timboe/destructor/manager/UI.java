package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import timboe.destructor.Param;
import timboe.destructor.enums.UIMode;
import timboe.destructor.screen.GameScreen;

public class UI {

  private static UI ourInstance;
  public static UI getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new UI(); }
  public void dispose() { ourInstance = null; }

  private Table table;
  private Skin skin;

  public Table getTable() {
    return table;
  }

  private UI() {
    reset();
  }

  public void reset() {
    table = new Table();
    table.setFillParent(true);
    table.row().fillY();
    table.top();
    //    table.right().top().pad(Param.TILE_S*2);
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(Textures.getInstance().getUIAtlas());

    GameState.getInstance().getUIStage().addActor(table);

    final Window window = new Window("Particle Destructor", skin, "default");
    window.setResizable(false);
    window.setMovable(false);
    table.add(window).width(Param.DISPLAY_X/4);

    window.addListener(new ClickListener(){
      @Override
      public void clicked(InputEvent event, float x, float y){
        Gdx.app.log("window","clicked");
      }
    });


    final TextButton button = new TextButton("Click me", skin, "default");
    window.add(button).expandX().width(100).height(100);
    button.addListener(new ChangeListener(){
      @Override
      public void changed(ChangeEvent event, Actor actor){
        Gdx.app.log("button","clicked");
        button.setText("You clicked the button");
        GameState.getInstance().uiMode = UIMode.kPLACE_BUILDING;
      }
    });

    table.debugAll();
  }

}
