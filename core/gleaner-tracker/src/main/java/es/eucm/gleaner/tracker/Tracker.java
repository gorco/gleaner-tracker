package es.eucm.gleaner.tracker;

import es.eucm.gleaner.tracker.model.RestAPI;
import es.eucm.gleaner.tracker.model.TrackData;
import es.eucm.gleaner.tracker.model.traces.Events;
import es.eucm.gleaner.tracker.model.traces.InputTrace;
import es.eucm.gleaner.tracker.model.traces.InputTrace.Action;
import es.eucm.gleaner.tracker.model.traces.InputTrace.Device;
import es.eucm.gleaner.tracker.model.traces.LogicTrace;
import es.eucm.gleaner.tracker.model.traces.Trace;
import es.eucm.gleaner.network.requests.*;

import java.util.ArrayList;

public class Tracker implements ResourceCallback<TrackData>, RequestCallback {

	public static final int RETRIES = 10;

	/**
	 * Request helper
	 */
	protected final RequestHelper requestHelper;

	/**
	 * Traces
	 */
	private final TracesQueue traces;

	/**
	 * Current server uri
	 */
	private String serverUri;

	private String trackingCode;

	/**
	 * Track data
	 */
	private TrackData trackData;

	/**
	 * If the tracker currently tracking
	 */
	private boolean tracking;

	/**
	 * Number of erroneous communications with the server
	 */
	private int errors;

	/**
	 * Auth token
	 */
	private String authorization;

	/**
	 * Number of retries
	 */
	private int retry;

	/**
	 * If the we are waiting to a response from the server
	 */
	private boolean connecting;

	private ArrayList<ConnectionListener> connectionListeners;

	public Tracker(RequestHelper requestHelper) {
		this.requestHelper = requestHelper;
		this.traces = new TracesQueue(requestHelper, this);
		this.connectionListeners = new ArrayList<ConnectionListener>();
		this.errors = 0;
		this.tracking = true;
		this.connecting = false;
	}

	public void addConnectionListener(ConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/**
	 * Set the URL where Gleaner is listening
	 * 
	 * @param url
	 */
	public void setServerURL(String url) {
		this.serverUri = url;
	}

	/**
	 * @return Returns the URL from the Gleaner server
	 */
	public String getServerURL() {
		return serverUri;
	}

	/**
	 * Returns a request helper. It can be used to query traces already sent to
	 * the server
	 * 
	 * @return
	 */
	public RequestHelper getRequestHelper() {
		return requestHelper;
	}

	/**
	 * Returns track data
	 * 
	 * @return track data
	 */
	public TrackData getTrackData() {
		return trackData;
	}

	public String getAuthorization() {
		return authorization;
	}

	/**
	 * Sets the authorization header to be sent when the tracker starts the
	 * tracking
	 * 
	 * @param authorization
	 *            the auth token
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	/**
	 * @return Returns the input traces queue
	 */
	public TracesQueue getTracesQueue() {
		return traces;
	}

	/**
	 * Asks the server to track the game given by tracking code
	 */
	public void startTracking(String trackingCode) {
		this.trackData = null;
		this.trackingCode = trackingCode;
		traces.clear();
		connect();
	}

	/**
	 * Connects to the server
	 */
	private void connect() {
		if (!connecting) {
			connecting = true;
			String trackUrl = serverUri + RestAPI.START + trackingCode;
			/*requestHelper.url(trackUrl)
					.header("Authorization", getAuthorization())
					.post(this, TrackData.class, false);*/

			TrackData data = new TrackData();
			data.setAuthToken("test_unneeded");
			data.setPlayerName("Mario");
			success(data);
		}
	}

	/**
	 * @return Returns true if the tracker is ready to send traces (connected)
	 */
	public boolean isReady() {
		return trackData != null;
	}

	/**
	 * Tracks a trace
	 * 
	 * @param trace
	 *            the trace
	 */
	public <T extends Trace> void track(T trace) {
		if (!isReady() && retry > 0) {
			retry--;
			if (retry == 0) {
				connect();
			}
		}
		if (tracking) {
			traces.add(trace);
		}
	}

	// ---------------//
	// INPUT TRACKING //
	// ---------------//

	/**
	 * Adds an input trace
	 * 
	 * @param device
	 *            the input device
	 * @param context
	 *            the context for the trace (e.g. name of level or area)
	 * @param target
	 *            the id of the element that received the input
	 * @param action
	 *            the action performed (pressed, moved...)
	 * @param button
	 *            the button for the action (0 no button, 1 left lick, 2 middle
	 *            click, 3 right click)
	 * @param value1
	 *            a first value for the trace (e.g., this value con hold the x
	 *            coordinate from a press, or a keycode for a key pressed)
	 * @param value2
	 *            a second value for the trace (e.g. this value can hold the y
	 *            coordinate from a press)
	 * @param value3
	 *            a third value for the trace
	 * @param extra
	 *            A string with some extra information
	 */
	public void input(Device device, String context, String target,
			Action action, Integer button, Float value1, Float value2,
			Float value3, String extra) {
		InputTrace trace = new InputTrace(context, target, device.toString(),
				action.toString(), button, value1, value2, value3, extra);
		track(trace);
	}

	/**
	 * Logs a mouse left press input event. For the x and y coordinate is
	 * advisable to send the coordinates in the game coordinates system instead
	 * of in the window screen coordinates
	 * 
	 * @param context
	 *            the context in which happened (e.g. the phase name)
	 * @param target
	 *            the target of the input (e.g. the id of the element in the
	 *            phase that received the press event)
	 * @param x
	 *            x coordinate in the context
	 * @param y
	 *            y coordinate in the context
	 */
	public void press(String context, String target, float x, float y) {
		input(Device.MOUSE, context, target, Action.PRESSED, 1, x, y, null,
				null);
	}

	/**
	 * Logs a mouse left click input event. For the x and y coordinate is
	 * advisable to send the coordinates in the game coordinates system instead
	 * of in the window screen coordinates
	 * 
	 * @param context
	 *            the context in which happened (e.g. the phase name)
	 * @param target
	 *            the target of the input (e.g. the id of the element in the
	 *            phase that received the press event)
	 * @param x
	 *            x coordinate in the context
	 * @param y
	 *            y coordinate in the context
	 */
	public void click(String context, String target, float x, float y) {
		input(Device.MOUSE, context, target, Action.CLICKED, 1, x, y, null,
				null);
	}

	/**
	 * Logs a key pressed
	 * 
	 * @param context
	 *            the context
	 * @param keycode
	 *            the keycode
	 */
	public void keyPress(String context, int keycode) {
		input(Device.KEYBOARD, context, null, Action.PRESSED, keycode, null,
				null, null, null);
	}

	// ---------------//
	// INPUT TRACKING //
	// ---------------//

	/**
	 * Tracks a logic event
	 * 
	 * @param event
	 *            the event identifier. You can use some from
	 *            {@link es.eucm.gleaner.tracker.model.traces.Events}
	 * @param target
	 *            the target for the event (e.g. the phase in which happens)
	 * @param value1
	 *            some data for the event
	 * @param value2
	 *            some data for the event
	 * @param value3
	 *            some data for the event
	 */
	public void logic(String event, String target, Object value1,
			Object value2, Object value3) {
		LogicTrace trace = new LogicTrace(event, target, value1, value2, value3);
		track(trace);
	}

	/**
	 * Tracks a logic event
	 * 
	 * @param event
	 *            the event identifier. You can use some from
	 *            {@link es.eucm.gleaner.tracker.model.traces.Events}
	 * @param target
	 *            the target for the event (e.g. the phase in which happens)
	 * @param value1
	 *            some data for the event
	 * @param value2
	 *            some data for the event
	 */
	public void logic(String event, String target, Object value1, Object value2) {
		logic(event, target, value1, value2, null);
	}

	/**
	 * Logs that the game starts
	 */
	public void gameStart() {
		logic(Events.GAME_START, null, null, null, null);
	}

	/**
	 * Logs that the game ends
	 */
	public void gameEnd() {
		logic(Events.GAME_END, null, null, null, null);
	}

	/**
	 * Logs that a phase starts
	 * 
	 * @param phaseName
	 */
	public void phaseStart(String phaseName) {
		logic(Events.PHASE_START, phaseName, null, null, null);
	}

	/**
	 * Logs a variable update
	 * 
	 * @param varName
	 *            variable name
	 * @param newValue
	 *            the new value for the variable
	 */
	public void varUpdate(String varName, Object newValue) {
		logic(Events.VAR_UPDATE, varName, newValue, null, null);
	}

	/**
	 * Logs a death
	 * 
	 * @param context
	 *            context of the death
	 * @param identifier
	 *            identifier of the element that "died"
	 * @param x
	 *            x coordinate of the death
	 * @param y
	 *            y coordinate of the death
	 */
	public void death(String context, String identifier, Float x, Float y) {
		logic(Events.DEATH, context, identifier, x, y);
	}

	public void score(String context, int score) {
		logic(Events.SCORE, context, score, null, null);
	}

	/**
	 * Player has entered in a new zone
	 * 
	 * @param zoneId
	 *            the zone id
	 */
	public void zone(String zoneId) {
		logic(Events.ZONE, null, zoneId, null, null);
	}

	public void var(String varName, Object value) {
		logic(Events.VAR_UPDATE, varName, value, null, null);
	}

	public void choice(String choiceId, String choiceOption) {
		logic(Events.CHOICE, choiceId, choiceOption, null, null);
	}

	public void startGameplay() {
		logic(Events.START, null, null, null, null);
	}

	public void interact(String target) {
		interact(target, null);
	}

	public void interact(String target, String value) {
		logic("interact", target, value, null, null);
	}

	/**
	 * Stops tracking
	 */
	public void stopTracking() {
		tracking = false;
		flush();
	}

	/**
	 * Force to send out the traces stored in the local cache
	 */
	public void flush() {
		if (!isReady()) {
			connect();
		}
		traces.flush();
	}

	/**
	 * @return Returns true whether there are traces pending to be sent to the
	 *         server
	 */
	public boolean isDone() {
		return traces.isDone();
	}

	// Handlers for start tracking
	@Override
	public void success(TrackData trackData) {
		connecting = false;
		this.trackData = trackData;
		this.traces.setTrackData(trackData);
		//this.traces.setUrl(this.serverUri + RestAPI.TRACK);
		this.traces.setUrl("http://localhost:8080/xAPI/statements");
		errors = 0;
		for (ConnectionListener connectionListener : connectionListeners) {
			connectionListener.connected(trackData);
		}
	}

	@Override
	public void error(Throwable errorMessage) {
		connecting = false;
		errors++;
		retry = RETRIES * errors;
	}

	// Handlers for queues requests

	@Override
	public void success(Request request, Response response) {
		if (response.getStatusCode() == Response.OK
				|| response.getStatusCode() == Response.NO_CONTENT) {
			errors = 0;
		} else {
			errors++;
		}
	}

	@Override
	public void error(Request request, Throwable throwable) {
		errors++;
	}

	public void setMaxTracesPerQueue(int maxTraces) {
		traces.setMaxSize(maxTraces);
	}

	public interface ConnectionListener {

		void connected(TrackData trackData);

	}

}
