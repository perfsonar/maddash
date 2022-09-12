package net.es.maddash.notifications;

import java.util.Comparator;

public class NotifyProblemComparator implements Comparator<NotifyProblem>{

    @Override
    public int compare(NotifyProblem p1, NotifyProblem p2) {
        int result = this.compareName(p1.getGridName(), p2.getGridName());
        if(result == 0){
            result = this.compareName(p1.getSiteName(), p1.getSiteName());
        }
            
        return result;
    }
    
    private int compareName(String s1, String s2){
        if(s1 == null && s2 == null){
            return 0;
        }else if(s1 == null){
            return -1;
        }else if(s2 == null){
            return 1;
        }

        return s1.compareToIgnoreCase(s2);
    }

}
