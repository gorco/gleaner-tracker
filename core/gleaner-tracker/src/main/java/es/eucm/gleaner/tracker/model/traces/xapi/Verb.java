package es.eucm.gleaner.tracker.model.traces.xapi;

import java.util.Map;

/**
 * Created by Antonio on 28/07/2015.
 */
public class Verb {

    String id;

    Map<String, String> display;

    public Verb(String id, Map<String, String> display) {
        this.id = id;
        this.display = display;
    }
}
