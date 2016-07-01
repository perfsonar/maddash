package net.es.maddash.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

import net.es.maddash.NetLogger;

public class EmailNotification implements Notification{
    private Logger log = Logger.getLogger(EmailNotification.class);
    private Logger netlogger = Logger.getLogger("netlogger");
    
    private String name;
    private String format;
    private String dashboardUrl;
    
    private String serverAddress;
    private int serverPort;
    private String serverUsername;
    private String serverPassword;
    private boolean serverSSL;
    
    private String subjectPrefix;
    private String fromAddress;
    private List<String> replyTo;
    private List<String> toAddresses;
    private List<String> ccAddresses;
    private List<String> bccAddresses;
    
    final public static String PROP_MAILSERVER = "mailServer";
    final public static String PROP_MAILSERVER_ADDR = "address";
    final public static String PROP_MAILSERVER_PORT = "port";
    final public static String PROP_MAILSERVER_USER = "username";
    final public static String PROP_MAILSERVER_PASS = "password";
    final public static String PROP_MAILSERVER_SSL = "useSSL";
    final public static String PROP_FROM = "from";
    final public static String PROP_REPLYTO = "replyTo";
    final public static String PROP_TO = "to";
    final public static String PROP_CC = "cc";
    final public static String PROP_BCC = "bcc";
    final public static String PROP_SUBJECTPREFIX = "subjectPrefix";
    final public static String PROP_FORMAT = "format";
    final public static String PROP_FORMAT_HTML = "html";
    final public static String PROP_FORMAT_TEXT = "text";
    final public static String PROP_DASHBOARDURL = "dashboardUrl";
    
    final public static String DEFAULT_MAILSERVER_ADDR = "127.0.0.1";
    final public static int DEFAULT_MAILSERVER_PORT = 25;
    final public static String DEFAULT_MAILSERVER_USER = null;
    final public static String DEFAULT_MAILSERVER_PASS = null;
    final public static boolean DEFAULT_MAILSERVER_SSL = false;
    final public static String DEFAULT_FORMAT = "html";
    
    @Override
    public void init(String name, JsonObject params) {
        this.name = name;
        
        //setup mail server
        if(params.containsKey(PROP_MAILSERVER)){
            JsonObject mailServerParams = params.getJsonObject(PROP_MAILSERVER);
            if(mailServerParams.containsKey(PROP_MAILSERVER_ADDR) && !mailServerParams.isNull(PROP_MAILSERVER_ADDR) ){
                this.serverAddress = mailServerParams.getString(PROP_MAILSERVER_ADDR);
            }else{
                this.serverAddress = DEFAULT_MAILSERVER_ADDR;
            }
            if(mailServerParams.containsKey(PROP_MAILSERVER_PORT) && !mailServerParams.isNull(PROP_MAILSERVER_PORT) ){
                this.serverPort = mailServerParams.getInt(PROP_MAILSERVER_PORT);
            }else{
                this.serverPort = DEFAULT_MAILSERVER_PORT;
            }
            if(mailServerParams.containsKey(PROP_MAILSERVER_USER) && !mailServerParams.isNull(PROP_MAILSERVER_USER) ){
                this.serverUsername = mailServerParams.getString(PROP_MAILSERVER_USER);
            }else{
                this.serverUsername = DEFAULT_MAILSERVER_USER;
            }
            if(mailServerParams.containsKey(PROP_MAILSERVER_PASS) && !mailServerParams.isNull(PROP_MAILSERVER_PASS) ){
                this.serverPassword = mailServerParams.getString(PROP_MAILSERVER_PASS);
            }else{
                this.serverPassword = DEFAULT_MAILSERVER_PASS;
            }
            
            if(mailServerParams.containsKey(PROP_MAILSERVER_SSL) && !mailServerParams.isNull(PROP_MAILSERVER_SSL) ){
                this.serverSSL = mailServerParams.getInt(PROP_MAILSERVER_SSL) > 0;
            }else{
                this.serverSSL = DEFAULT_MAILSERVER_SSL;
            }
        }
        
        //setup addresses
        if(params.containsKey(PROP_FROM) && !params.isNull(PROP_FROM) ){
            this.fromAddress = params.getString(PROP_FROM);
        }
        this.replyTo = new ArrayList<String>();
        if(params.containsKey(PROP_REPLYTO) && !params.isNull(PROP_REPLYTO) ){
            JsonArray json = params.getJsonArray(PROP_REPLYTO);
            for(int i = 0; i < json.size(); i++){
                this.replyTo.add(json.getString(i));
            }
        }
        this.toAddresses = new ArrayList<String>();
        if(params.containsKey(PROP_TO) && !params.isNull(PROP_TO) ){
            JsonArray json = params.getJsonArray(PROP_TO);
            for(int i = 0; i < json.size(); i++){
                this.toAddresses.add(json.getString(i));
            }
        }else{
            throw new RuntimeException("The property " + PROP_FROM + " must be specified");
        }
        this.ccAddresses = new ArrayList<String>();
        if(params.containsKey(PROP_CC) && !params.isNull(PROP_CC) ){
            JsonArray json = params.getJsonArray(PROP_CC);
            for(int i = 0; i < json.size(); i++){
                this.ccAddresses.add(json.getString(i));
            }
        }
        this.bccAddresses = new ArrayList<String>();
        if(params.containsKey(PROP_BCC) && !params.isNull(PROP_BCC) ){
            JsonArray json = params.getJsonArray(PROP_BCC);
            for(int i = 0; i < json.size(); i++){
                this.bccAddresses.add(json.getString(i));
            }
        }
        
        //setup subject
        this.subjectPrefix = "";
        if(params.containsKey(PROP_SUBJECTPREFIX) && !params.isNull(PROP_SUBJECTPREFIX) ){
            this.subjectPrefix = params.getString(PROP_SUBJECTPREFIX);
        }
        
      //setup dashboard url
        this.dashboardUrl = null;
        if(params.containsKey(PROP_DASHBOARDURL) && !params.isNull(PROP_DASHBOARDURL) ){
            this.dashboardUrl = params.getString(PROP_DASHBOARDURL);
            if(!this.dashboardUrl.endsWith("/")){
                this.dashboardUrl += "/";
            }
        }
        
        //setup format
        if(params.containsKey(PROP_FORMAT) && !params.isNull(PROP_FORMAT) ){
            this.format = params.getString(PROP_FORMAT);
            if(!PROP_FORMAT_HTML.equals(this.format) && !PROP_FORMAT_TEXT.equals(this.format)){
                throw new RuntimeException("Unsupported email format. Email notifications must have " + 
                        PROP_FORMAT + " set to " + PROP_FORMAT_TEXT + " or " + PROP_FORMAT_HTML);
            }
        }else{
            this.format = DEFAULT_FORMAT;
        }

    }

    @Override
    public void send(List<NotifyProblem> problems) {
        NetLogger netLog = NetLogger.getTlogger();
        netlogger.info(netLog.start("maddash.EmailNotification.send"));
        //don't send anything if no problems
        if(problems.isEmpty()){
            netlogger.info(netLog.end("maddash.EmailNotification.send", "No problems found so no email sent"));
            return;
        }
        
        try {
            Email email = null;
            if(this.format == PROP_FORMAT_HTML){
                email = new HtmlEmail();
                ((HtmlEmail)email).setHtmlMsg(this.generateHtmlMessage(problems));
            }else{
                email = new SimpleEmail();
                email.setMsg(this.generateTextMessage(problems));
            }
            email.setHostName(this.serverAddress);
            email.setSmtpPort(this.serverPort);
            if(this.serverUsername != null && this.serverPassword != null){
                email.setAuthenticator(new DefaultAuthenticator(this.serverUsername, this.serverPassword));
            }
            email.setSSLOnConnect(this.serverSSL);
            email.setStartTLSRequired(this.serverSSL);
            
            if(this.fromAddress != null){
                email.setFrom(this.fromAddress);
            }
            for(String addr : this.replyTo){
                email.addReplyTo(addr);
            }
            for(String addr : this.toAddresses){
                email.addTo(addr);
            }
            for(String addr : this.ccAddresses){
                email.addCc(addr);
            }
            for(String addr : this.bccAddresses){
                email.addBcc(addr);
            }
            email.setSubject(this.subjectPrefix + " " + this.name);
            
            email.send();
        } catch (Exception e) {
            netlogger.info(netLog.error("maddash.EmailNotification.send", e.getMessage()));
            e.printStackTrace();
        }
        
        netlogger.info(netLog.end("maddash.EmailNotification.send", "Email sent"));
    }

    private String generateTextMessage(List<NotifyProblem> problems) {
        StringBuffer msg = new StringBuffer();
        for(NotifyProblem p: problems){
            msg.append("Grid: " + p.getGridName() + "\n");
            msg.append("Site: " + (p.getSiteName() != null ? p.getSiteName() : "Global") + "\n");
            msg.append("Description: " + p.getProblem().getName() + "\n");
            msg.append("Category: " + p.getProblem().getCategory() + "\n");
            msg.append("Severity: " + p.getProblem().getSeverity() + "\n");
            msg.append("Solutions:\n");
            for(String s : p.getProblem().getSolutions()){
                msg.append("    - " + s.trim() + "\n");
            }
            msg.append("\n");
        }
        return msg.toString();
    }
    
    private String generateHtmlMessage(List<NotifyProblem> problems) {
        StringBuffer msg = new StringBuffer();
        msg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        msg.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        msg.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        msg.append("<head>");
// Most email clients don't support <style></style> tag
//        msg.append("<style type=\"text/css\">");
//        msg.append(".maddashReportScopeTitle {font-size: 16px;color:#000000;margin-top: 10px;margin: 10px 5px 5px 0px;text-decoration: underline;font-weight: bold;}");
//        msg.append(".maddashReportReport {margin: 5px 5px 10px 0px;}");
//        msg.append(".report-container {width: 97%;margin: 10px 10px 20px 10px;border-bottom: 1px solid  #d0d1e6;padding-bottom: 20px}");
//        msg.append(".maddashReportGridName {font-size: 18px;font-weight: bold;color:#000000;margin: 25px 5px 5px 0px;}");
//        msg.append(".maddashReportRow {margin: 5px 0px 5px 0px;}");
//        msg.append(".maddashReportLabel {font-size: 14px;font-weight: bold;margin-right: 5px;}");
//        msg.append(".maddashReportProblems {margin-left: 0px;}");
//        msg.append(".maddashReportStatusRowImg {vertical-align: middle;height: 24px;width: 24px;}");
//        msg.append(".maddashReportStatusRow {font-size:14px;display: inline-block;vertical-align: middle;}");
//        msg.append(".maddashReportSolutionsList {margin: 0px 0px 0px 0px;font-size: 14px;}");
//        msg.append(".maddashSummaryReportsDiv {margin: 0px 0px 0px 10px;width: 95%;}");
//        msg.append(".maddashReportStatusRowCritical {font-size: 18px; font-weight: bold;}");
//        msg.append(".maddashReportStatusRowWarn {font-size: 18px; font-weight: bold;}");
//        msg.append(".maddashReportStatusRowOk {font-size: 18px; font-weight: bold;}");
//        msg.append(".maddashReportProblemCritical {border: 1px solid #DB2929;background-color:#FFE4E1;margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;}");
//        msg.append(".maddashReportProblemOk {border: 1px solid #9ACD32;background-color:#DFFFA5;margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;}");
//        msg.append(".maddashReportProblemWarn {border: 1px solid #FFE600;background-color:#FFFCCF;margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;}");
//        msg.append("</style>");
        msg.append("<title></title>");
        msg.append("</head>");
        msg.append("<body marginheight=\"0\" marginwidth=\"0\">");
        msg.append("<div class=\"reportMainTitle\" style=\"color: #FFFFFF;background-color: #0066CC;font-size: 24px; font-weight: bolder; margin: 0px 0px 0px 0px; padding: 20px 20px 20px 5px;\">MaDDash: Test Email Report</div>");
        msg.append("<div style=\"margin: 0px 0px 0px 0px; border: 1px solid #b5bcc7; background-color: #efefef; text-align: left; padding: 6px 10px 6px 10px;\"><a href=\""+ (this.dashboardUrl == null ? "#" : this.dashboardUrl) + "\" style=\"color: #000000; size: 16px; text-decoration: none;\">&#8689; View Dashboard</a></div>");
        String prevGrid = "";
        String prevSite = "";
        boolean isFirst = true;
        for(NotifyProblem p: problems){
            if(!Objects.equals(p.getGridName(), prevGrid)){
                if(!isFirst){
                    msg.append("</div>");
                    msg.append("</div>");
                    msg.append("</div>");
                }
                msg.append("<div class=\"report-container\" style=\"width: 97%;margin: 10px 10px 20px 10px;border-bottom: 1px solid  #d0d1e6;padding-bottom: 20px\">");
                msg.append("<div class=\"maddashReportGridName\" style=\"font-size: 18px;font-weight: bold;color:#000000;margin: 25px 5px 5px 0px;\">");
                msg.append("<a href=\"" + (this.dashboardUrl == null ? "#" : this.dashboardUrl + "?grid=" + p.getGridName()) + "\" style=\"font-size: 18px;font-weight: bold;color:#000000; text-decoration: none\">&#8689; ");
                msg.append(p.getGridName() + "</a></div>");
                msg.append("<div>");
                msg.append("<div class=\"maddashReportReport\" style=\"margin: 5px 5px 10px 0px;\">");
                prevSite = "";
            }
            if(!Objects.equals(p.getSiteName(), prevSite)){
                if(!isFirst){
                    msg.append("</div>");
                }
                msg.append("<div class=\"maddashReportScopeTitle\" style=\"font-size: 16px;color:#000000;margin-top: 10px;margin: 10px 5px 5px 0px;text-decoration: underline;font-weight: bold;\">");
                msg.append("<span class=\"maddashReportScopeTitleSpan\">" + (p.getSiteName() == null ? "Global" : p.getSiteName()) + "</span>");
                msg.append("</div>");
                msg.append("<div class=\"maddashReportProblems\" style=\"margin-left: 0px;\">");
            }
            if(p.getProblem().getSeverity() == 0){
                msg.append("<div class=\"maddashReportProblemOk\" style=\"border: 1px solid #9ACD32;background-color:#DFFFA5;margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;\">");
            }else if(p.getProblem().getSeverity() == 1){
                msg.append("<div class=\"maddashReportProblemWarn\" style=\"border: 1px solid #FFE600;background-color:#FFFCCF; color: black; margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;\">");
            }else{
                msg.append("<div class=\"maddashReportProblemCritical\" style=\"border: 1px solid #DB2929;background-color:#FFE4E1;margin: 5px 0px 5px 0px;padding: 5px 5px 5px 5px;\">");
            }
            msg.append("<div class=\"maddashReportStatusRow\">");
            //msg.append("<img class=\"maddashReportStatusRowImg\" src=\"images/error.png\">");
            if(p.getProblem().getSeverity() == 0){
                msg.append("<span class=\"maddashReportStatusRowOk\" style=\"font-size: 18px; font-weight: bold;\" > &#10003; ");
            }else if(p.getProblem().getSeverity() == 1){
                msg.append("<span class=\"maddashReportStatusRowWarn\" style=\"font-size: 18px; font-weight: bold;\"> &#9888; ");
            }else{
                msg.append("<span class=\"maddashReportStatusRowCritical\" style=\"font-size: 18px; font-weight: bold;\">&#10008; ");
            }
            msg.append(p.getProblem().getName() + "</span>");
            msg.append("</div>");
            msg.append("<div class=\"maddashReportRow\" style=\"margin: 5px 0px 5px 0px;\">");
            msg.append("<span class=\"maddashReportLabel\" style=\"font-size: 14px;font-weight: bold;margin-right: 5px;\">Category:</span>");
            msg.append("<span class=\"maddashReportCategory\">" + p.getProblem().getCategory() + "</span>");
            msg.append("</div>");
            if(!p.getProblem().getSolutions().isEmpty()){
                msg.append("<div class=\"maddashReportRow\" style=\"margin: 5px 0px 5px 0px;\">");
                msg.append("<span class=\"maddashReportLabel\" style=\"font-size: 14px;font-weight: bold;margin-right: 5px;\">Potential Solutions:</span>");
                msg.append("</div>");
                msg.append("<div class=\"maddashReportSolutions\">");
                msg.append("<ul class=\"maddashReportSolutionsList\" style=\"margin: 0px 0px 0px 0px;font-size: 14px;\">");
                for(String s : p.getProblem().getSolutions()){
                    msg.append("<li class=\"maddashReportSolutionsListItem\">" + s + "</li>");
                }
                msg.append("</ul>");
                msg.append("</div>");
            }
            msg.append("</div>");
            prevGrid = p.getGridName();
            prevSite = p.getSiteName();
            isFirst = false;
        }
        //close off all divs
        msg.append("</div>");
        msg.append("</div>");
        msg.append("</div>");
        msg.append("</div>");
        
        msg.append("</body>");
        msg.append("</html>");

        return msg.toString();
    }

}
