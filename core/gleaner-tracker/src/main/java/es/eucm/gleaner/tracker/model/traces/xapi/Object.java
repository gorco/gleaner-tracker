package es.eucm.gleaner.tracker.model.traces.xapi;

import java.util.Map;

/**
 * Created by Antonio on 28/07/2015.
 */
public class Object {

    String id;
    String objectType = "Activity";
    Definition definition;

    public Object(String id, Definition definition) {
        this.id = id;
        this.definition = definition;
    }
}
