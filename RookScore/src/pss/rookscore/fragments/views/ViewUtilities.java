package pss.rookscore.fragments.views;

import java.util.List;

public class ViewUtilities {
    static String shorterName(List<String> allNames, String name) {
        
        //go with initials
        //if conflict with another set of initials, add next letter from first of each name section until no match
        String nameParts[] = name.split("\\s");
        StringBuilder initials = new StringBuilder();
        for (String string : nameParts) {
            initials.append(string.charAt(0));
        }
        
        return initials.toString();
    }
}
