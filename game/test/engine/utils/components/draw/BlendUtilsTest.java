package engine.utils.components.draw;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// tests for @link BlendUtils; OpenGL and the SpriteBatch are replaced by mocks
class BlendUtilsTest {

  private GL20 savedGl;
  private GL20 gl;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    // the tests have no real OpenGL, so Gdx.gl is replaced and restored afterwards
    savedGl = Gdx.gl;
    gl = mock(GL20.class);
    Gdx.gl = gl;
    batch = mock(SpriteBatch.class);
  }

  @AfterEach
  void restoreGl() {
    Gdx.gl = savedGl;
  }

  @Test
  void pmaBlendingIsSetOnTheBatch() {
    BlendUtils.setPMABlending(batch);
    verify(batch).enableBlending();
    verify(batch).setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    verifyNoInteractions(gl);
  }

  @Test
  void pmaBlendingWithoutBatchIsSetInOpenGl() {
    BlendUtils.setPMABlending(null);
    verify(gl).glEnable(GL20.GL_BLEND);
    verify(gl).glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Test
  void straightAlphaBlendingIsSetOnTheBatch() {
    BlendUtils.setStraightAlphaBlending(batch);
    verify(batch).enableBlending();
    verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    verifyNoInteractions(gl);
  }

  @Test
  void straightAlphaBlendingWithoutBatchIsSetInOpenGl() {
    BlendUtils.setStraightAlphaBlending(null);
    verify(gl).glEnable(GL20.GL_BLEND);
    verify(gl).glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Test
  void defaultBlendingIsPmaBlending() {
    BlendUtils.setBlending(batch);
    verify(batch).enableBlending();
    verify(batch).setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Test
  void defaultBlendingWithoutBatchIsSetInOpenGl() {
    BlendUtils.setBlending();
    verify(gl).glEnable(GL20.GL_BLEND);
    verify(gl).glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }
}
