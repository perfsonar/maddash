package net.es.maddash.www.rest;

import java.util.List;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;

import org.apache.log4j.Logger;

@Path("/maddash/events")
public class EventsResource {
    Logger log = Logger.getLogger(EventsResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    @Context UriInfo uriInfo;
    final private String GET_EVENT = "maddash.www.rest.Events.get";
    
    @Produces("application/json")
    @GET
    public Response get(@QueryParam(value = "gridName") List<String> gridName, 
            @QueryParam(value = "rowName") List<String> rowName,
            @QueryParam(value = "columnName") List<String> colName,
            @QueryParam(value = "checkName") List<String> checkName,
            @QueryParam(value = "dimensionName") List<String> dimensionName,
            @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(GET_EVENT));
        
        JsonObject json = null;
        try{
            json = MaDDashGlobals.getInstance().getResourceManager().getEvents(gridName, rowName, colName, checkName, dimensionName, uriInfo);
        }catch(Exception e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            return Response.serverError().entity(e.getMessage()).build();
        }
        
        this.netLogger.info(netLog.end(GET_EVENT));
        return Response.ok().entity(json.toString()).build();
    }
}
