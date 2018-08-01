package net.es.maddash.checks;

import java.util.Comparator;
import java.util.TreeMap;

public class TemplateVariableMap extends TreeMap<String,String>{

    public TemplateVariableMap(){
        super(
                new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        if (s1.length() > s2.length()) {
                            return -1;
                        } else if (s1.length() < s2.length()) {
                            return 1;
                        } else {
                            return s1.compareTo(s2);
                        }
                    }
                }
                );
    }

}
