package core.utils.components.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BlendUtil {

  /**
   * Sets the blending mode to the default blending used in this project (PMA Blending).
   */
  public static void setBlending(){
    setBlending(null);
  }

  /**
   * Sets the blending mode to the default blending used in this project (PMA Blending).
   * @param batch The SpriteBatch to set the blending mode for. If null, sets the OpenGL blending mode directly.
   */
  public static void setBlending(SpriteBatch batch){
    setPMABlending(batch);
  }

  /**
   * Sets the blending mode to Pre-Multiplied Alpha (PMA) Blending.
   * @param batch The SpriteBatch to set the blending mode for. If null, sets the OpenGL blending mode directly.
   */
  public static void setPMABlending(SpriteBatch batch){
    if (batch == null){
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    } else {
      batch.enableBlending();
      batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
  }

  /**
   * Sets the blending mode to Straight Alpha Blending.
   * @param batch The SpriteBatch to set the blending mode for. If null, sets the OpenGL blending mode directly.
   */
  public static void setStraightAlphaBlending(SpriteBatch batch){
    if(batch == null){
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    } else {
      batch.enableBlending();
      batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
  }

}
