package de.fwatermann.dungine.ecs.components;

import de.fwatermann.dungine.audio.AudioContext;
import de.fwatermann.dungine.audio.AudioSource;
import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.utils.Disposable;

import java.util.UUID;

/**
 * Component to create and attach an audio source to an entity.
 */
public class AudioSourceComponent extends Component implements Disposable {

  private AudioSource source;

  public AudioSourceComponent(AudioContext context) {
    super(true);
    this.source = context.createSource(UUID.randomUUID().toString(), false, false);
  }

  /**
   * Gets the audio source associated with this component.
   *
   * @return the audio source
   */
  public AudioSource source() {
    return this.source;
  }

  /**
   * Sets the audio source associated with this component.
   *
   * @param source the audio source to set
   * @return the updated AudioSourceComponent
   */
  public AudioSourceComponent source(AudioSource source) {
    this.source = source;
    return this;
  }


  @Override
  public void dispose() {
    this.source.dispose();
  }
}
