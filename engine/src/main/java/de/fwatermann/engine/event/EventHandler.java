package de.fwatermann.engine.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as event handlers.
 * This annotation is used to mark methods that should be invoked in response to a specific event.
 * The method should have a single parameter, which is the event object.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler { }
