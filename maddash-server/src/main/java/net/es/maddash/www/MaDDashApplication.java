package net.es.maddash.www;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import net.es.maddash.www.rest.AdminEventResource;
import net.es.maddash.www.rest.AdminEventsResource;
import net.es.maddash.www.rest.AdminScheduleResource;
import net.es.maddash.www.rest.CellResource;
import net.es.maddash.www.rest.CheckResource;
import net.es.maddash.www.rest.ChecksResource;
import net.es.maddash.www.rest.ColumnsResource;
import net.es.maddash.www.rest.DashboardsResource;
import net.es.maddash.www.rest.EventResource;
import net.es.maddash.www.rest.EventsResource;
import net.es.maddash.www.rest.GridResource;
import net.es.maddash.www.rest.GridsResource;
import net.es.maddash.www.rest.MaDAlertDiffResource;
import net.es.maddash.www.rest.MaDAlertIndexResource;
import net.es.maddash.www.rest.MaDAlertReportResource;
import net.es.maddash.www.rest.RowResource;
import net.es.maddash.www.rest.RowsResource;

@javax.ws.rs.ApplicationPath("/maddash")
public class MaDDashApplication extends Application{

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> services = new HashSet<Class<?>>();
        services.add(DashboardsResource.class);
        services.add(GridsResource.class);
        services.add(GridResource.class);
        services.add(RowResource.class);
        services.add(RowsResource.class);
        services.add(CellResource.class);
        services.add(CheckResource.class);
        services.add(ChecksResource.class);
        services.add(ColumnsResource.class);
        services.add(AdminScheduleResource.class);
        services.add(AdminEventsResource.class);
        services.add(AdminEventResource.class);
        services.add(EventsResource.class);
        services.add(EventResource.class);
        //madalert
        services.add(MaDAlertDiffResource.class);
        services.add(MaDAlertIndexResource.class);
        services.add(MaDAlertReportResource.class);
        return services;
    }
}
