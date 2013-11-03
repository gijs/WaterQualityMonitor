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

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

public class PluginManager {
    private static final int BLOCK_SIZE = 64;
    private static Logger logger = Logger.getLogger(PluginManager.class);
    private static ClassLoader ldr = PluginManager.class.getClassLoader();

    public static final Object lock = new Object();

    private static final Hashtable<Class, Hashtable<String, ArrayList<Pair<Long, Class>>>> plugins = new Hashtable<Class, Hashtable<String, ArrayList<Pair<Long, Class>>>>();
    private static final String default_namespace = "default_namespace";

    public static <E> List<E> getPlugins(Class _interface, String namespace, Object[] constructorArgs) {
        List<E> toRet = new ArrayList<E>();
        Class[] cArgs = null;
        if (constructorArgs != null) {
            cArgs = new Class[constructorArgs.length];
            for (int i = 0, constructorArgsLength = constructorArgs.length; i < constructorArgsLength; i++) {
                cArgs[i] = constructorArgs[i].getClass();
            }
        }
        if (!plugins.containsKey(_interface)) {
            loadPlugins(_interface);
        }
        ArrayList<Pair<Long, Class>> found_plugins = plugins.get(_interface).get(namespace);
        if (found_plugins == null) {
            plugins.get(_interface).put(namespace, found_plugins = new ArrayList<Pair<Long, Class>>());
        }
        Collections.sort(found_plugins, new PluginComparator());
        synchronized (PluginManager.class) {
            for (Pair<Long, Class> found_plugin : found_plugins) {
                try {
                    Constructor cc = found_plugin.getB().getConstructor(cArgs);
                    toRet.add((E) cc.newInstance(constructorArgs));
                } catch (Throwable e) {
                    logger.warn("Plugin: " + found_plugin.getB().getCanonicalName() + " could not be created: " + e.getMessage(), e);
                }
            }
        }

        return toRet;
    }

    public static <E> List<E> getPlugins(Class _interface, Object[] constructorArgs) {
        return getPlugins(_interface, default_namespace, constructorArgs);
    }

    private static synchronized void loadPlugins(Class _interface) {


        if (!plugins.containsKey(_interface)) {
            plugins.put(_interface, new Hashtable<String, ArrayList<Pair<Long, Class>>>());
        }
        Hashtable<String, ArrayList<Pair<Long, Class>>> interface_plugins = plugins.get(_interface);
        InputStream is;
        try {
            Enumeration<URL> pluginList = ldr.getResources(String.format("META-INF/wqm/%s", _interface.getName()));
            while (pluginList.hasMoreElements()) {
                URL url = pluginList.nextElement();
                for (String plugin : new String(readStream(is = url.openStream())).split("\r\n|\n|\r")) {
                    if (plugin.trim().length() > 0) {
                        try {
                            String[] nsplug = plugin.split(";");
                            String namespace = default_namespace;
                            long priority = Long.MAX_VALUE;
                            String classname = nsplug[0];
                            if (nsplug.length >= 2) {
                                namespace = nsplug[0];
                                classname = nsplug[1];
                            }
                            if (nsplug.length == 3) {
                                priority = Long.parseLong(nsplug[2]);
                            }
                            if (!interface_plugins.containsKey(namespace)) {
                                interface_plugins.put(namespace, new ArrayList<Pair<Long, Class>>());
                            }
                            interface_plugins.get(namespace).add(new Pair<Long, Class>(priority, ldr.loadClass(classname)));
                        } catch (Throwable e) {
                            logger.warn("Plugin: " + plugin + " could not be created: " + e.getMessage(), e);
                        }
                    }
                }
                is.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(BLOCK_SIZE);
        byte[] buffer = new byte[BLOCK_SIZE];
        int len = is.read(buffer);
        while (len > 0) {
            bos.write(buffer, 0, len);
            len = is.read(buffer);
        }
        return bos.toByteArray();
    }


    private static class PluginComparator implements Comparator<Pair<Long, Class>> {

        public int compare(Pair<Long, Class> o1, Pair<Long, Class> o2) {
            int toRet = o1.getA().compareTo(o2.getA());
            if (toRet != 0) {
                return toRet;
            }
            return o1.getB().getCanonicalName().compareTo(o2.getB().getCanonicalName());
        }
    }

}
