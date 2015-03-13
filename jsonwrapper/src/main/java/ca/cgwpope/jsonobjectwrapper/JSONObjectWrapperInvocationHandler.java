package ca.cgwpope.jsonobjectwrapper;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class JSONObjectWrapperInvocationHandler implements InvocationHandler {

	private JSONObject mDelegate;
	private JSONObjectFactoryImpl mFactory;

	public JSONObjectWrapperInvocationHandler(JSONObjectFactoryImpl factory, JSONObject delegate) {
		mDelegate = delegate;
		mFactory = factory;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//Proxy == invocation handler instance

		if(!method.getName().startsWith("get") && !method.getName().startsWith("set") && !method.getName().equals("has")){
			throw new IllegalArgumentException("Unsupported method: " + method.getName());
		}
		
		
		if(method.getName().equals("getDelegate")){
			if(args == null ||args.length == 0){
				return mDelegate;
			} else {
				throw new IllegalArgumentException("getDeleagte() must be called with no arguments");

			}
		}
		
		
		if(method.getName().equals("has")){
			if(args.length == 1 && args[0] instanceof String){
				return mDelegate.has((String)args[0]);
			} else {
				throw new IllegalArgumentException("has() must be called with a single String argument to test existing of field in JSONObject");
			}
		}
		
		

		if(method.getName().startsWith("set")){
			handleProperySet(method, args);
			return null;
		} else if(method.getName().startsWith("get")){
			return handlePropertyGet(method);
		} else {
			//can't get here
			throw new IllegalStateException("Invalid method invocation");
		}
	}

	private Object handlePropertyGet(Method method) throws JSONException {
		
		String fieldName = resolveFieldName(method.getName());
		
		if(method.getReturnType().isInterface() && IJSONObjectWrapper.class.isAssignableFrom(method.getReturnType()) ){
			Class<? extends IJSONObjectWrapper> returnValueType = (Class<? extends IJSONObjectWrapper>)method.getReturnType();
			return mFactory.wrap(returnValueType, mDelegate.getJSONObject(fieldName));
		} else if(method.getReturnType().isArray()){
			JSONArray array = mDelegate.getJSONArray(fieldName);
			if(Boolean.class.equals(method.getReturnType().getComponentType())){
				Boolean toReturn[] = new Boolean[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getBoolean(i);
				}
				
				return toReturn;
				
			} else if(boolean.class.equals(method.getReturnType().getComponentType())){
				boolean toReturn[] = new boolean[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getBoolean(i);
				}
				
				return toReturn;				
			}
			
			else if(double.class.equals(method.getReturnType().getComponentType())){
				double toReturn[] = new double[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getDouble(i);
				}
				
				return toReturn;
			} else if(Double.class.equals(method.getReturnType().getComponentType())) {
				Double toReturn[] = new Double[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getDouble(i);
				}
				
				return toReturn;
				
			} else if(Integer.class.equals(method.getReturnType().getComponentType())){
				Integer toReturn[] = new Integer[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getInt(i);
				}
				
				return toReturn;
			} else if(int.class.equals(method.getReturnType().getComponentType())){
				int toReturn[] = new int[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getInt(i);
				}
				
				return toReturn;
			} else if(Long.class.equals(method.getReturnType().getComponentType())){
				Long toReturn[] = new Long[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getLong(i);
				}
				
				return toReturn;	
			} else if(long.class.equals(method.getReturnType().getComponentType())){
				long toReturn[] = new long[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getLong(i);
				}
				
				return toReturn;	
			} else if(String.class.equals(method.getReturnType().getComponentType())){
				String toReturn[] = new String[array.length()];
				
				for(int i= 0; i < toReturn.length; i++){
					toReturn[i] = array.getString(i);
				}
				
				return toReturn;
				
			} else if(method.getReturnType().getComponentType().isInterface() && IJSONObjectWrapper.class.isAssignableFrom(method.getReturnType().getComponentType())){
				
				Object arrayToReturn = Array.newInstance(method.getReturnType().getComponentType(), array.length());
				
				for(int i= 0; i < array.length(); i++){
					Array.set(arrayToReturn, i, mFactory.wrap((Class<? extends IJSONObjectWrapper>)method.getReturnType().getComponentType(), array.getJSONObject(i)));
				}
				
				return arrayToReturn;
			} else {
				throw new IllegalArgumentException("Unsupported return type:" + method.getReturnType().getComponentType());
			}

		} else {
			/*switch on return value for all supported types
			mDelegate.getBoolean(key)
			mDelegate.getDouble(key)
			mDelegate.getInt(key)
			mDelegate.getLong(key)
			mDelegate.getString(key)
			*/
			if(Boolean.class.equals(method.getReturnType()) || boolean.class.equals(method.getReturnType())){
				return mDelegate.getBoolean(fieldName);
			} else if(Double.class.equals(method.getReturnType()) || double.class.equals(method.getReturnType())){
				return mDelegate.getDouble(fieldName);
			} else if(Integer.class.equals(method.getReturnType()) || int.class.equals(method.getReturnType())){
				return mDelegate.getInt(fieldName);
			} else if(Long.class.equals(method.getReturnType()) || long.class.equals(method.getReturnType())){
				return mDelegate.getLong(fieldName);
			} else if(String.class.equals(method.getReturnType())){
				return mDelegate.getString(fieldName);
			} else {
				throw new IllegalArgumentException("Unsupported return type:" + method.getReturnType());
			}
		}
	}

	private void handleProperySet(Method method, Object[] args) throws JSONException {
		String fieldName = resolveFieldName(method.getName());

		if(args.length  != 1){
			throw new IllegalArgumentException("Setter must have single argument" + method.getName());
		}
		
		if(args[0] == null){
			mDelegate.put(fieldName, JSONObject.NULL);
		}
		
		if(IJSONObjectWrapper.class.isAssignableFrom(args[0].getClass())){
			IJSONObjectWrapper wrapper = (IJSONObjectWrapper)args[0];
			mDelegate.put(fieldName, wrapper.getDelegate());
		} else if(args[0].getClass().isArray()){
			
			JSONArray toSet = new JSONArray();
			if(args[0].getClass().getComponentType().isAssignableFrom(IJSONObjectWrapper.class)){
				//each element in the array is a IJSONObjectWrapper - unwrap and store
				IJSONObjectWrapper values[] = (IJSONObjectWrapper[])args[0];
				for (IJSONObjectWrapper value : values) {
					toSet.put(value.getDelegate());
				}
			} else 	if(args[0].getClass().getComponentType().equals(Boolean.class)){
				Boolean values[] = (Boolean[])args[0];
				for (Boolean value : values) {
					toSet.put(value);
				}
			} else if( args[0].getClass().getComponentType().equals(boolean.class)){
				boolean values[] = (boolean[])args[0];
				for (boolean value : values) {
					toSet.put(value);
				}				
			}
			else if(args[0].getClass().getComponentType().equals(Double.class)){
				Double values[] = (Double[])args[0];
				for (Double value : values) {
					toSet.put(value);
				}			
			} else if(args[0].getClass().getComponentType().equals(double.class)) {
				double values[] = (double[])args[0];
				for (double value : values) {
					toSet.put(value);
				}							
			}else if(args[0].getClass().getComponentType().equals(Integer.class)){
				Integer values[] = (Integer[])args[0];
				for (Integer value : values) {
					toSet.put(value);
				}			
			} else if(args[0].getClass().getComponentType().equals(int.class)){
				int values[] = (int[])args[0];
				for (int value : values) {
					toSet.put(value);
				}			
				
			}else if(args[0].getClass().getComponentType().equals(Long.class)){
				Long values[] = (Long[])args[0];
				for (Long value : values) {
					toSet.put(value);
				}			
			} else if(args[0].getClass().getComponentType().equals(long.class)){
				long values[] = (long[])args[0];
				for (long value : values) {
					toSet.put(value);
				}							
			}else if(args[0].getClass().getComponentType().equals(String.class)){
				String values[] = (String[])args[0];
				for (String value : values) {
					toSet.put(value);
				}					
			} else {
				throw new IllegalArgumentException("Invalid type to set array property:" + args[0].getClass().getComponentType());
			}
			
			mDelegate.put(fieldName, toSet);
			
		} else {
			if(args[0].getClass().equals(Boolean.class) || args[0].getClass().equals(boolean.class)){
				mDelegate.put(fieldName, (Boolean)args[0]);
			} else if(args[0].getClass().equals(Double.class) || args[0].getClass().equals(double.class)){
				mDelegate.put(fieldName, (Double)args[0]);
			} else if(args[0].getClass().equals(Integer.class) || args[0].getClass().equals(int.class)){
				mDelegate.put(fieldName, (Integer)args[0]);
			} else if(args[0].getClass().equals(Long.class) || args[0].getClass().equals(long.class)){
				mDelegate.put(fieldName, (Long)args[0]);
			} else if(args[0].getClass().equals(String.class)){
				mDelegate.put(fieldName, (String)args[0]);
			} else {
				throw new IllegalArgumentException("Invalid type to set property:" + args[0].getClass());
			}
		}
	}

	private String resolveFieldName(String name)  {
        try {
            return mFactory.getNamingStrategy().toJSONFieldName(name);
        } catch (JSONFieldNamingStrategy.UnresolvableFieldNameException e){
            throw new RuntimeException(e);
        }
	}

}
