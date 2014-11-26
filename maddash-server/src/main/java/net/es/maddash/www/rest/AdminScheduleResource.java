package net.es.maddash.www.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.sf.json.JSONObject;

@Path("/maddash/admin/schedule")
public class AdminScheduleResource {
    Logger log = Logger.getLogger(AdminScheduleResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    @Context UriInfo uriInfo;
    final private String POST_EVENT = "maddash.www.rest.AdminSchedule.post";
    
    final public static String FIELD_NEXTCHECKTIME = "nextCheckTime";
    final public static String FIELD_CHECKFILTERS = "checkFilters";
    
    @Produces("application/json")
    @Consumes("application/json")
    @POST
    public Response post(String body, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(POST_EVENT));
        
        
        JSONObject response = null;
        try{
            JSONObject request = JSONObject.fromObject(body);
            response = MaDDashGlobals.getInstance().getResourceManager().updateSchedule(request);
        }catch(Exception e){
            this.netLogger.error(netLog.error(POST_EVENT, e.getMessage()));
            return Response.serverError().entity(e.getMessage()).build();
        }
        
        this.netLogger.info(netLog.end(POST_EVENT));
        return Response.ok().entity(response.toString()).build();
    }
}
