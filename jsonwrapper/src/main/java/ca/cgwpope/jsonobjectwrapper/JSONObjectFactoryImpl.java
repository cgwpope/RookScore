package ca.cgwpope.jsonobjectwrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectFactoryImpl implements IJSONObjectWrapperFactory {

    private final JSONFieldNamingStrategy mNamingStrategy;

    public JSONObjectFactoryImpl(JSONFieldNamingStrategy namingStrategy){
        mNamingStrategy = namingStrategy;
    }


	@Override
	public <T extends IJSONObjectWrapper> T wrap(Class<T> clazz, JSONObject delegate) {
		if(IJSONObjectWrapper.class.equals(clazz)){
			throw new IllegalArgumentException("Wrapped Object must be a sub-interface of " + IJSONObjectWrapper.class);
		}
		
		return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new JSONObjectWrapperInvocationHandler(this, delegate));
	}

	@Override
	public <T extends IJSONObjectWrapper> T newInstance(Class<T> clazz) {
		JSONObject object = new JSONObject();
		return wrap(clazz, object);
	}

	@Override
	public <T extends IJSONObjectWrapper> T[] wrap(Class<T> arrayComponentType, JSONArray delegate)   {
		Object arrayToReturn = Array.newInstance(arrayComponentType, delegate.length());
		for(int i = 0; i < delegate.length(); i++){
			//TODO: Handle nested JSON array
			try {
				Array.set(arrayToReturn, i, wrap(arrayComponentType, delegate.getJSONObject(i)));
			} catch (JSONException e) {
				throw new RuntimeException("Array element is not a JSONObject");
			}
		}
		
		return (T[])arrayToReturn;
	}

    public JSONFieldNamingStrategy getNamingStrategy() {
        return mNamingStrategy;
    }
}
