package es.eucm.gleaner.tracker.conversors;

import com.google.gwt.json.client.JSONObject;

import es.eucm.gleaner.gwt.network.converters.AbstractJsonConverter;
import es.eucm.gleaner.model.traces.InputTrace;
import es.eucm.gleaner.tracker.data.ActionTraceJS;

public class ActionTraceConversor extends
		AbstractJsonConverter<InputTrace, ActionTraceJS> {

	@Override
	public String getJson(InputTrace object) {
		ActionTraceJS trace = (ActionTraceJS) ActionTraceJS.createObject();
		trace.set(object);
		return new JSONObject(trace).toString();
	}

	@Override
	public InputTrace getObject(ActionTraceJS jsObject) {
		return null;
	}

}
