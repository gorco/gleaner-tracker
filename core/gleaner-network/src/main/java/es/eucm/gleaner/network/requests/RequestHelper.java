package es.eucm.gleaner.network.requests;

import es.eucm.gleaner.network.CharSet;
import es.eucm.gleaner.network.ContentType;
import es.eucm.gleaner.network.Header;
import es.eucm.gleaner.network.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract class that handles all the platform-dependent details of http
 * requests
 */
public abstract class RequestHelper {

	public RequestHelper() {

	}

	public RequestHelper.Builder url(String url) {
		return new Builder(url);
	}

	/**
	 * Sends an standard request
	 * 
	 * @param request
	 *            the request
	 * @param uriWithParameters
	 *            the target uri (with parameters already encoded in it)
	 * @param callback
	 *            the request callback
	 */
	public abstract void send(Request request, String uriWithParameters,
			RequestCallback callback);

	/**
	 * Performs a request over a resources
	 * 
	 * @param request
	 *            the request
	 * @param uriWithParameters
	 *            the target uri (with parameters already encoded in it)
	 * @param callback
	 *            resource callback
	 * @param clazz
	 *            class of the result
	 * @param isCollection
	 *            if the resource is a collection
	 * @param <S>
	 *            class of the resource (after process the response).
	 * @param <T>
	 *            class of the resource (as returned by the server). Could be
	 *            the same as S or not. S could be Object, and T could be
	 *            List<Object>
	 */
	public abstract <S, T> void send(Request request, String uriWithParameters,
			ResourceCallback<T> callback, Class<S> clazz, boolean isCollection);

	/**
	 * Encodes a string in the given charset
	 * 
	 * @param string
	 *            the string to encode
	 * @param charset
	 *            the charset
	 * @return the string encoded
	 */
	public abstract String encode(String string, String charset);

	/**
	 * Converts the given element into a JSON string representing it
	 * 
	 * @param element
	 *            the element
	 * @return the JSON string
	 */
	public abstract String getJsonData(Object element);

	public class Builder {

		protected Request request;

		public Builder(String url) {
			request = new Request();
			request.setUri(url);
		}

		public Builder method(String method) {
			request.setMethod(method);
			return this;
		}

		public Builder contentType(String contentType) {
			request.setHeader(Header.CONTENT_TYPE, contentType);
			return this;
		}

		public Builder header(String name, String value) {
			request.setHeader(name, value);
			return this;
		}

		public Builder data(Object entity) {
			request.setEntity(getJsonData(entity));
			return this;
		}

		public Builder setParameter(String name, String value) {
			request.setParameter(name, value);
			return this;
		}

		public void send(RequestCallback callback) {
			RequestHelper.this.send(request, getUriWithParamaters(request),
					callback);
		}

		public String getUriWithParamaters(Request request) {
			String parameters = null;
			if (request.getParameters() != null) {
				int i = request.getParameters().size();
				if (i > 0) {
					parameters = "";
					for (Entry<String, String> entry : request.getParameters()
							.entrySet()) {
						i--;
						parameters += encode(entry.getKey(), CharSet.UTF8)
								+ "=" + encode(entry.getValue(), CharSet.UTF8)
								+ (i != 0 ? "&" : "");
					}
				}
			}
			return request.getUri()
					+ (parameters == null ? "" : "?" + parameters);
		}

		public <S, T> void post(ResourceCallback<T> callback, Class<S> clazz,
				boolean isCollection) {
			method(Method.POST);
			RequestHelper.this.send(request, getUriWithParamaters(request),
					callback, clazz, isCollection);
		}

		public void post(Object element) {
			post(element, null);
		}

		public void post(Object element, RequestCallback callback) {
			method(Method.POST);
			contentType(ContentType.APPLICATION_JSON);
			data(element);
			send(callback);
		}

		public Builder parameters(Map<String, String> parameters) {
			request.setParameters(parameters);
			return this;
		}
	}
}
