
package com.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * A variant of the URLClassLoader that first loads from the URLs and only after that from the
 * parent.
 *
 * <p>
 * {@link #getResourceAsStream(String)} uses {@link #getResource(String)} internally so we don't
 * override that.
 */
public final class ChildFirstClassLoader extends URLClassLoader {

/**
   * The classes that should always go through the parent ClassLoader. This is relevant for Flink
   * classes, for example, to avoid loading Flink classes that cross the user-code/system-code
   * barrier in the user-code ClassLoader.
   */
  private final String[] alwaysParentFirstPatterns;

  public ChildFirstClassLoader(URL[] urls, ClassLoader parent, String[] alwaysParentFirstPatterns) {
   // super(urls, parent);
	  super(urls);
    this.alwaysParentFirstPatterns = alwaysParentFirstPatterns;
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {

    // First, check if the class has already been loaded
    @SuppressWarnings("rawtypes")
	Class c = findLoadedClass(name);

    if (c == null) {
      // check whether the class should go parent-first
     if(alwaysParentFirstPatterns != null)
      {	  
	      for (String alwaysParentFirstPattern : alwaysParentFirstPatterns) {
	        if (name.startsWith(alwaysParentFirstPattern)) {
	          return super.loadClass(name, resolve);
	        }
	      }
      }

      try {
        // check the URLs
        c = findClass(name);
      } catch (ClassNotFoundException e) {
        // let URLClassLoader do it, which will eventually call the parent
        c = super.loadClass(name, resolve);
      }
    }

    if (resolve) {
      resolveClass(c);
    }

    return c;
  }

  @Override
  public URL getResource(String name) {
    // first, try and find it via the URLClassloader
    URL urlClassLoaderResource = findResource(name);

    if (urlClassLoaderResource != null) {
      return urlClassLoaderResource;
    }

    // delegate to super
    return super.getResource(name);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    // first get resources from URLClassloader
    Enumeration<URL> urlClassLoaderResources = findResources(name);
    
    final List<URL> result = new ArrayList<URL>();
    
    while (urlClassLoaderResources.hasMoreElements()) {
      result.add(urlClassLoaderResources.nextElement());
    }

    // get parent urls
    Enumeration<URL> parentResources = getParent().getResources(name);

    while (parentResources.hasMoreElements()) {
      result.add(parentResources.nextElement());
    }

    return new Enumeration<URL>() {
      Iterator<URL> iter = result.iterator();

      public boolean hasMoreElements() {
        return iter.hasNext();
      }

      public URL nextElement() {
        return iter.next();
      }
    };
  }
}

