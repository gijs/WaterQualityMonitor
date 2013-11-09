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

import org.apache.commons.cli.*;
import wqm.constants.Defaults;
import wqm.web.exceptions.InvalidRadioException;
import wqm.web.server.WQMConfig;

import javax.xml.bind.JAXBException;

/**
 * Date: 11/1/13
 * Time: 10:38 AM
 *
 * @author NigelB
 */
public class CmdLine {
    public static final Option help;
    public static final Option config_dir;
    public static final Option listen;
    public static final Option port;

    static {

        help = OptionBuilder.withLongOpt("help")
                .withDescription("Display this help message.")
                .create('h');

        config_dir = OptionBuilder.withArgName("config_dir")
                .hasArg()
                .withLongOpt("config-dir")
                .withDescription(String.format("The location of the config directory, default: %s", Defaults.ConfigDir.getDefault()))
                .create('c');

        listen = OptionBuilder.withArgName("listen")
                .hasArg()
                .withLongOpt("listen")
                .withDescription("The address to listen on.")
                .create('l');

        port = OptionBuilder.withArgName("port")
                .hasArg()
                .withLongOpt("port")
                .withDescription(String.format("The port to listen on."))
                .create('p');

    }


    public static Options getOptions()
    {
        Options options = new Options();
        options.addOption(help);
        options.addOption(config_dir);
        options.addOption(listen);
        options.addOption(port);
        return options;
    }

    public static WQMConfig createServerConfig(CommandLine cl) throws JAXBException, InvalidRadioException {
        WQMConfig toRet = new WQMConfig();


        if(cl.hasOption(listen.getOpt()))
        {
            toRet.setAddress(get(cl, listen));
        }else
        {
            toRet.setAddress(Defaults.ListenAddress.getDefault());
        }


        if(cl.hasOption(port.getOpt()))
        {
            toRet.setPort(getInt(cl, port));
        }else
        {
            toRet.setPort(Integer.parseInt(Defaults.ListenPort.getDefault()));
        }

        if(cl.hasOption(config_dir.getOpt()))
        {
            toRet.setConfigDir(get(cl, config_dir));
        }else
        {
            toRet.setConfigDir(Defaults.ConfigDir.getDefault());
        }
        return toRet;
    }

    public static CommandLine parse(String programName, Options o, String[] args) throws ParseException {
        CommandLineParser p = new GnuParser();
        CommandLine toRet = p.parse(o, args);

        if (toRet.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(programName, o);
            System.exit(0);
        }

        return toRet;
    }
    public static String get(CommandLine cLine, Option option) {
        return cLine.getOptionValue(option.getOpt());
    }

    public static int getInt(CommandLine cLine, Option option) {
        return Integer.parseInt(cLine.getOptionValue(option.getOpt()));
    }

}
