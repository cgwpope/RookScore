package ca.cgwpope.jsonobjectwrapper;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IJSONObjectWrapperFactory {
	public<T extends IJSONObjectWrapper> T wrap(Class<T> clazz, JSONObject delegate);
	public<T extends IJSONObjectWrapper> T[] wrap(Class<T> clazz, JSONArray delegate);
	public<T extends IJSONObjectWrapper> T newInstance(Class<T> clazz);


	
	public static final IJSONObjectWrapperFactory DEFAULT = new JSONObjectFactoryImpl(new JavaPropertyNamingJSONNamingStrategy(false));
}
