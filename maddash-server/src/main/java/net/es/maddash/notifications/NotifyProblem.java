package net.es.maddash.notifications;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.es.maddash.madalert.Problem;

public class NotifyProblem {
    protected String gridName;
    protected String siteName;
    protected Problem problem;
    protected boolean isGlobal;
    
    public NotifyProblem(String gridName, String siteName, Problem problem){
        this.gridName = gridName;
        this.siteName = siteName;
        this.problem = problem;
        this.isGlobal = false;
    }
    
    public NotifyProblem(String gridName, Problem problem){
        this.gridName = gridName;
        this.siteName = null;
        this.problem = problem;
        this.isGlobal = true;
    }
    
    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
    
    public String checksum(){
        String str = this.gridName + "::";
        if(this.siteName != null){
            str += this.siteName + "::";
        }
        str += this.problem.getCategory() + "::";
        str += this.problem.getName() + "::";
        str += this.problem.getSeverity() + "::";
        if(this.problem.getSolutions() != null){
            for(String s : this.problem.getSolutions()){
                str += s + "::";
            }
        }
        
        String checksum = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            checksum =  new BigInteger(1, md.digest(str.getBytes())).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to calculate md5 checksum because your java does not seem to support it");
        }
        
        return checksum;
    }
}
