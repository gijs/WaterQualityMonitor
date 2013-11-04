/*
 * Water Quality Monitor Java Basestation
 * Copyright (C) 2013  nigelb
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package wqm.web.server;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import wqm.constants.ResourceMarkers;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Date: 10/21/13
 * Time: 2:10 PM
 *
 * @author NigelB
 */
public class HTTPServer {

    private Server jetty;
    private ResourceCollection base;


    public HTTPServer(WQMConfig config) throws IOException, URISyntaxException, NamingException {

        base =  new ResourceCollection(
                Resource.newResource(new URL(HTTPServer.class.getResource("/"+ ResourceMarkers.CONFIG_LOCATOR).toExternalForm().replace(ResourceMarkers.CONFIG_LOCATOR, "wqm-war/"))),
                Resource.newResource(new File(config.getDataConfig().getDataOutputDirectory()))
        );

        jetty = new Server();
        jetty.setThreadPool(new ExecutorThreadPool());
        jetty.addConnector(createConnector(config));
        jetty.setHandler(createHandler(config));

    }


    private Handler createHandler(WQMConfig config) throws IOException, URISyntaxException, NamingException {
        WebAppContext ctx = new WebAppContext("WQM", "/");
        ctx.setBaseResource(base);
        org.eclipse.jetty.plus.jndi.Resource r = new org.eclipse.jetty.plus.jndi.Resource(WQMConfig.class.getCanonicalName(), config);
        return ctx;

    }

    private Connector createConnector(WQMConfig config) {
        SelectChannelConnector c = new SelectChannelConnector();
        c.setHost(config.getAddress());
        c.setPort(config.getPort());
        return c;
    }

    public void start() throws Exception {
        jetty.start();
    }

    public void stop() throws Exception {
        jetty.stop();
    }

}
