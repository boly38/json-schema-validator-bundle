package com.github.jsonschemavalidator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;

public class BundleClassLoader extends ClassLoader {
    /**
     * Logger
     */
    private Log LOG = LogFactory.getLog(BundleClassLoader.class.getName());

    Bundle b = null;

    public BundleClassLoader() {
        super(BundleClassLoader.class.getClassLoader());
    }

    public BundleClassLoader(ClassLoader parent) {
        super(parent);
    }

    public BundleClassLoader(ClassLoader parent, Bundle b) {
        super(parent);
        this.b = b;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }// loadClass

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {
        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    @Override
    protected Package getPackage(String name) {
        return super.getPackage(name);
    }

    @Override
    public URL getResource(String name) {
        if (name == null) {
            return null;
        }
        if (b != null) {
            LOG.debug("load resource from bundle :" + name);
            return b.getResource(name);
        }
        return super.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }
}
