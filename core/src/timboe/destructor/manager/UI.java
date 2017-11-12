package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import timboe.destructor.Param;

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
    table = new Table();
    table.setFillParent(true);
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(Textures.getInstance().getUIAtlas() );

    GameState.getInstance().getUIStage().addActor(table);
    table.right().top().pad(Param.TILE_S*2);

    final Window window = new Window("Particle Destructor", skin, "default");
    window.top();
    window.setResizable(false);
    window.setMovable(false);
    table.add(window).width(Param.DISPLAY_X/4);


    final TextButton button = new TextButton("Click me", skin, "default");
    window.add(button).expandX().width(100).height(100);
    button.addListener(new ClickListener(){
      @Override
      public void clicked(InputEvent event, float x, float y){
        button.setText("You clicked the button");
      }
    });


    table.debugAll();

  }

}
