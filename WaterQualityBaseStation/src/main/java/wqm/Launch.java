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

package wqm;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import wqm.web.server.HTTPServer;
import wqm.web.server.WQMConfig;

/**
 * Date: 10/21/13
 * Time: 2:05 PM
 *
 * @author NigelB
 */
public class Launch {
    private static Logger logger = Logger.getLogger(Launch.class);
    static volatile boolean running = true;
    public static void main(String[] args) throws Exception {
        CommandLine cmd = CmdLine.parse("wqm", CmdLine.getOptions(), args);
        WQMConfig config = CmdLine.createServerConfig(cmd);
        HTTPServer server = new HTTPServer(config);
        logger.info("A");
        config.addShutdownCallback(new Runnable() {
            public void run() {
                running = false;
                logger.error("Server Shutdown Called.");
            }
        });
        logger.info("B");
        server.start();
        logger.info("C");
        while(running)
        {
            Thread.sleep(500);
        }
        server.stop();
    }
}
