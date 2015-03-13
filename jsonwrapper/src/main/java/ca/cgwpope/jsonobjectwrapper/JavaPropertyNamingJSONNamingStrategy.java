package ca.cgwpope.jsonobjectwrapper;

/**
 * Created by t890428 on 2015-02-08.
 */
public class JavaPropertyNamingJSONNamingStrategy implements JSONFieldNamingStrategy {

    private final boolean mInitialFieldIsLowerCase;

    public JavaPropertyNamingJSONNamingStrategy(boolean isInitialCharacterLowerCase) {
        mInitialFieldIsLowerCase = isInitialCharacterLowerCase;
    }

    @Override
    public String toJSONFieldName(String javaMethodName) throws UnresolvableFieldNameException {

        String name = resolveJavaPropertyName(javaMethodName);
        if (name.length() == 0) {
            throw new UnresolvableFieldNameException(javaMethodName);
        } else if (mInitialFieldIsLowerCase) {
            StringBuilder toReturn = new StringBuilder();
            toReturn.append(Character.toLowerCase(name.charAt(0)));
            if (name.length() > 1) {
                toReturn.append(name.substring(1));
            }
            return toReturn.toString();
        } else {
            return name;
        }
    }

    protected String resolveJavaPropertyName(String javaMethodName) throws UnresolvableFieldNameException {
        if (javaMethodName.startsWith("get") || javaMethodName.startsWith("set")) {
            return javaMethodName.substring(3);
        } else {
            throw new UnresolvableFieldNameException(javaMethodName);
        }
    }
}
