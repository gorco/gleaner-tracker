package es.eucm.gleaner.tracker.model.traces.xapi;

/**
 * Created by Antonio on 28/07/2015.
 */
public class Actor {

    String objectType = "Agent";

    Account account;

    String name;

    public Actor(Account account, String name) {
        this.account = account;
        this.name = name;
    }
}
