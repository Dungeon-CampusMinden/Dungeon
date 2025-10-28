package testingUtils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/** Initializes a headless libgdx environment for testing. */
public class LibgdxTestStartupListener implements TestExecutionListener {
  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

  private static void initLibgdx() {
    System.out.println("Initializing LibGDX for tests...");
    Gdx.files = new HeadlessFiles();
    Gdx.audio = new MockAudio();
    Gdx.app = new HeadlessApplication(new MockApplicationListener());
    System.out.println("LibGDX initialized successfully.");
  }

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    if (INITIALIZED.compareAndSet(false, true)) {
      initLibgdx();
    }
  }

  private static class MockApplicationListener implements ApplicationListener {
    @Override
    public void create() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void render() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {}
  }
}
