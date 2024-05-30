package dsl.programmanalyzer;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public interface Relatable {
  Long getId();
}
