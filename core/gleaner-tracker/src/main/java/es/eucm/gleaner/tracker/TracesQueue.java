package es.eucm.gleaner.tracker;

import es.eucm.gleaner.tracker.model.TrackData;
import es.eucm.gleaner.tracker.model.traces.Events;
import es.eucm.gleaner.tracker.model.traces.InputTrace;
import es.eucm.gleaner.tracker.model.traces.LogicTrace;
import es.eucm.gleaner.tracker.model.traces.Trace;
import es.eucm.gleaner.network.Header;
import es.eucm.gleaner.network.requests.Request;
import es.eucm.gleaner.network.requests.RequestCallback;
import es.eucm.gleaner.network.requests.RequestHelper;
import es.eucm.gleaner.network.requests.Response;
import es.eucm.gleaner.tracker.model.traces.xapi.*;

import java.lang.Object;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TracesQueue implements RequestCallback {

    private final RequestHelper requestHelper;

    /**
     * A request callback interested on the request results of the queue
     */
    private final RequestCallback requestCallback;

    /**
     * Track data
     */
    private TrackData trackData;

    /**
     * Url where to send the traces
     */
    private String url;

    /**
     * Stored traces. Some might be sent to the server, others might not
     */
    private List<Trace> storedTraces;

    /**
     * Aux list to add the traces to send in the next batch
     */
    private List<Trace> tracesToSend;

    /**
     * Index of the last trace sent in storedTraces list
     */
    private int lastTraceSentIndex;

    /**
     * Index of the last trace sent to the server that has been positively
     * acknowledged by the server
     */
    private int lastTraceReceivedIndex;

    /**
     * This flag is true when we're waiting for a server's response
     */
    private boolean sending;

    /**
     * Max size
     */
    private int maxSize;

    /**
     * Current max size
     */
    private int currentMaxSize;

    public TracesQueue(RequestHelper requestHelper,
                       RequestCallback requestCallback) {
        this.requestHelper = requestHelper;
        this.requestCallback = requestCallback;
        this.storedTraces = new ArrayList<Trace>();
        this.tracesToSend = new ArrayList<Trace>();
        this.lastTraceSentIndex = -1;
        this.lastTraceReceivedIndex = -1;
        this.maxSize = -1;
        this.currentMaxSize = maxSize;
        this.sending = false;
    }

    /**
     * @return true of the tracker has started a session in the server
     */
    public boolean isConnected() {
        return trackData != null;
    }

    /**
     * Sets the number of traces that must be stored without sending them to the
     * server. When the number of traces exceeds this number, a flush is
     * automatically triggered. If maxSize == -1, then traces are not sent
     * automatically
     *
     * @param maxSize max size
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        this.currentMaxSize = maxSize;
    }

    /**
     * Sets the url to post the traces
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Add trace
     *
     * @param trace the trace to add
     */
    public synchronized void add(Trace trace) {
        storedTraces.add(trace);
        if (currentMaxSize >= 0
                && storedTraces.size() - lastTraceReceivedIndex > currentMaxSize) {
            flush();
        }
    }

    /**
     * Clears the traces, without sending them to the server
     */
    public void clear() {
        storedTraces.clear();
        tracesToSend.clear();
        lastTraceSentIndex = -1;
        lastTraceReceivedIndex = -1;
        currentMaxSize = maxSize;
    }

    /**
     * Clears the traces, sending them to the server
     */
    public void flush() {
        if (storedTraces.size() > 0
                && lastTraceReceivedIndex < storedTraces.size()
                && isConnected() && !sending) {
            sending = true;
            tracesToSend.clear();
            for (int i = lastTraceReceivedIndex + 1; i < storedTraces.size(); i++) {
                tracesToSend.add(storedTraces.get(i));
            }
            lastTraceSentIndex = storedTraces.size() - 1;
            requestHelper.url(url)
                    .header("X-Experience-API-Version", "1.0.1").header("Authorization", "Basic b3BlbmxyczpvcGVubHJz")
                    .post(toXAPI(tracesToSend), this);
        }
    }

    private Object toXAPI(Object element) {
        if (element instanceof List) {
            List<Object> traces = (List) element;
            List<Object> res = new ArrayList<Object>();

            for (Object elem : traces) {
                if (elem instanceof Trace) {
                    Trace trace = (Trace) elem;

                    String event = trace.getEvent();
                    String verbString = Events.toVerb.get(event);

                    if (verbString == null) {
                        continue;
                    }

                    String verbId = verbString,
                            objectId = null,
                            definitionType = null;
                    Map<String, Object> extensions = new HashMap<String, Object>();

                    objectId = verbId;
                    definitionType = verbId;
                    if(trace instanceof LogicTrace) {
                        LogicTrace logicTrace = (LogicTrace) trace;

                        extensions.put("value", logicTrace.getValue());
                        extensions.put("value2", logicTrace.getValue2());
                        extensions.put("value3", logicTrace.getValue3());
                    } else  if(trace instanceof InputTrace) {
                        InputTrace inputTrace = (InputTrace) trace;
                        extensions.put("value1", inputTrace.getValue1());
                        extensions.put("value2", inputTrace.getValue2());
                        extensions.put("value3", inputTrace.getValue3());
                    }

                    // Actor
                    Actor actor = new Actor(new Account(trackData.getPlayerName()), trackData.getPlayerName());

                    // Verb
                    Map<String, String> display = new HashMap<String, String>();
                    display.put("en-US", verbId);
                    display.put("es-ES", verbId);
                    Verb verb = new Verb("http://example.com/" + verbId, display);

                    // Object
                    Definition definition;
                    es.eucm.gleaner.tracker.model.traces.xapi.Object object;
                    if (extensions.isEmpty()) {
                        definition = new Definition("http://example.com/objects/" + definitionType);
                    } else {
                        definition = new Definition("http://example.com/objects/" + definitionType, extensions);
                    }
                    object = new es.eucm.gleaner.tracker.model.traces.xapi.Object("http://example.com/games/lostinspace/" + objectId, definition);

                    res.add(new XAPIObject(actor, verb, object));
                }
            }

            return res;
        }
        return element;
    }

    public void setTrackData(TrackData trackData) {
        this.trackData = trackData;
    }

    @Override
    public void error(Request request, Throwable throwable) {
        sending = false;
        if (currentMaxSize > 0) {
            // Increase the windows' size, to avoid too much attempts
            currentMaxSize += maxSize;
        }
        requestCallback.error(request, throwable);
    }

    @Override
    public void success(Request request, Response response) {
        sending = false;
        if (response.getStatusCode() == Response.OK
                || response.getStatusCode() == Response.NO_CONTENT) {
            currentMaxSize = maxSize;
            lastTraceReceivedIndex = lastTraceSentIndex;
        }
        requestCallback.success(request, response);
    }

    public int size() {
        return storedTraces.size() - (lastTraceReceivedIndex + 1);
    }

    public boolean isDone() {
        return lastTraceReceivedIndex == storedTraces.size() - 1;
    }
}
