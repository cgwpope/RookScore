package pss.rookscore.fragments.views;

import java.util.List;

public class ViewUtilities {
	
	static final int TEXT_SIZE = 16;
	static final int ROUND_SUMMARY_WIDTH = 150;

	
    static String shorterName(List<String> allNames, String name) {
        
        //go with initials
        //if conflict with another set of initials, add next letter from first of each name section until no match
        String nameParts[] = name.split("\\s");
        StringBuilder initials = new StringBuilder();
        
        if(nameParts.length == 0){
        	initials.append(name);
        } else if(nameParts.length == 1){
        	initials.append(nameParts[0].substring(0, Math.max(3, nameParts[0].length())));
        } else {
            for (String string : nameParts) {
                initials.append(string.charAt(0));
            }
        }
        
        
        return initials.toString();
    }

	static float computeCentredStringStart(float leftmost, float fullWidth, float textWidth) {
		return (leftmost + fullWidth/2) - textWidth/2;
	}
}
