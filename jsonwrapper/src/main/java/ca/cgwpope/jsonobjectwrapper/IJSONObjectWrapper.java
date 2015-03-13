package ca.cgwpope.jsonobjectwrapper;

import org.json.JSONObject;

public interface IJSONObjectWrapper {
	
	public JSONObject getDelegate();
	public boolean has(String fieldName);

}
