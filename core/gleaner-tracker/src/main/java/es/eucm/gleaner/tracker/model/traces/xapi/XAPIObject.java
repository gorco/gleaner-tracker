package es.eucm.gleaner.tracker.model.traces.xapi;

/**
 * Created by Antonio on 28/07/2015.
 */
public class XAPIObject {

    Actor actor;
    Verb verb;
    es.eucm.gleaner.tracker.model.traces.xapi.Object object;

    public XAPIObject(Actor actor, Verb verb, es.eucm.gleaner.tracker.model.traces.xapi.Object object) {
        this.actor = actor;
        this.verb = verb;
        this.object = object;
    }
}
