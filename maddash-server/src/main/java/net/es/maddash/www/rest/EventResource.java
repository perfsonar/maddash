package net.es.maddash.www.rest;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;

import org.apache.log4j.Logger;

@Path("/maddash/events/{eventId}")
public class EventResource {
    Logger log = Logger.getLogger(EventResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    @Context UriInfo uriInfo;
    
    final private String GET_EVENT = "maddash.www.rest.EventResource.get";
    
    @Produces("application/json")
    @GET
    public Response get(@PathParam("eventId") int eventId, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(GET_EVENT));
        
        JsonObject json = null;
        try{
            json = MaDDashGlobals.getInstance().getResourceManager().getEvent(eventId, uriInfo);
        }catch(Exception e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            return Response.serverError().entity(e.getMessage()).build();
        }
        //detect if not found
        if(json == null){
            this.netLogger.error(netLog.error(GET_EVENT, "Event resource not found"));
            return Response.status(Status.NOT_FOUND).entity("Event resource not found").build();
        }
        
        this.netLogger.info(netLog.end(GET_EVENT));
        return Response.ok().entity(json.toString()).build();
    }

}
