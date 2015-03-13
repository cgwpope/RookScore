package ca.cgwpope.jsonobjectwrapper;

public interface JSONFieldNamingStrategy {
    public String toJSONFieldName(String javaMethodName) throws UnresolvableFieldNameException;



    public static class UnresolvableFieldNameException extends Exception {
        private final  String mJavaMethodName;

        public UnresolvableFieldNameException(String javaMethodName){
            mJavaMethodName = javaMethodName;
        }
    }
}
