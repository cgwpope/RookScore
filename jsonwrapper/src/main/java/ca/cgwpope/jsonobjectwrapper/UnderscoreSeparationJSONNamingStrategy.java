package ca.cgwpope.jsonobjectwrapper;

public class UnderscoreSeparationJSONNamingStrategy implements JSONFieldNamingStrategy {
    private final boolean mFirstLetterOfWordsIsUppercase;

    public UnderscoreSeparationJSONNamingStrategy(boolean isFirstLetterOfWordsIsUppercase) {
        mFirstLetterOfWordsIsUppercase = isFirstLetterOfWordsIsUppercase;
    }

    @Override
    public String toJSONFieldName(String javaMethodName) throws JSONFieldNamingStrategy.UnresolvableFieldNameException {

        String name = resolveJavaPropertyName(javaMethodName);
        if (name.length() == 0) {
            throw new JSONFieldNamingStrategy.UnresolvableFieldNameException(javaMethodName);
        } else {
            //for each case switch, inject a '_'

            StringBuilder output = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);

                if(i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(name.charAt(i - 1))){
                    output.append('_');

                    if(mFirstLetterOfWordsIsUppercase){
                        c = Character.toUpperCase(c);
                    } else {
                        c = Character.toLowerCase(c);
                    }
                } else {
                    c = Character.toLowerCase(c);
                }

                output.append(c);

            }

            return output.toString();
        }
    }

    protected String resolveJavaPropertyName(String javaMethodName) throws JSONFieldNamingStrategy.UnresolvableFieldNameException {
        if (javaMethodName.startsWith("get") || javaMethodName.startsWith("set")) {
            return javaMethodName.substring(3);
        } else {
            throw new JSONFieldNamingStrategy.UnresolvableFieldNameException(javaMethodName);
        }
    }
}
