package es.eucm.gleaner.tracker.model.traces;

import java.util.HashMap;
import java.util.Map;

public class Events {

	public static final String PHASE_START = "phase_start";
	public static final String DEATH = "death";
	public static final String GAME_START = "game_start";
	public static final String GAME_END = "game_end";
	public static final String VAR_UPDATE = "var";
	public static final String CHOICE = "choice";
	public static final String SCORE = "score";
	public static final String ZONE = "zone";
	public static final String START = "start";

	public static final Map<String, String> toVerb = new HashMap<String, String>();

	static  {
		toVerb.put(PHASE_START, "phase_started");
		toVerb.put(DEATH, "died");
		toVerb.put(GAME_START, "started_game");
		toVerb.put(GAME_END, "ended_game");
		toVerb.put(VAR_UPDATE, "updated");
		toVerb.put(CHOICE, "choose");
		toVerb.put(SCORE, "changed_score");
		toVerb.put(ZONE, "entered");
		toVerb.put(START, "started");
	}
}
