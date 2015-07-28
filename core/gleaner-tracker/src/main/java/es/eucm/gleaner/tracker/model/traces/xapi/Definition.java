package es.eucm.gleaner.tracker.model.traces.xapi;

import java.lang.*;
import java.lang.Object;
import java.util.Map;

/**
 * Created by Antonio on 28/07/2015.
 */
public class Definition {

    String type;
    Map<String, java.lang.Object> extensions;

    public Definition(String type, Map<String, Object> extensions) {
        this.type = type;
        this.extensions = extensions;
    }

    public Definition(String type) {
        this.type = type;
    }
}
