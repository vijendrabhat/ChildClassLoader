package com.loader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class TestMain {

	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		
		URL[] urls = new URL[1];

		urls[0] = new java.net.URL("file:/Users/Vijendra.Bhat/.m2/repository/com/quaero/TestjarClassLoad/0.0.1-SNAPSHOT/TestjarClassLoad-0.0.1-SNAPSHOT.jar");
		
		ChildFirstClassLoader loader = new ChildFirstClassLoader(urls, null, null);
		
		Class<?> testClass = loader.loadClass("com.quaero.Test.Service.ClassCheck", true);
		
		Object classCheck = testClass.newInstance();
		Class<?> testClass2 = loader.loadClass("com.quaero.Test.Pojo.User", true);
		
		Constructor<?> cons= testClass2.getConstructor(String.class, String.class);
		Object user = cons.newInstance("vijendra","bhat");
		
		Method method = testClass.getMethod("test", new Class[]{testClass2});
		
		System.out.println(method.invoke(classCheck,user));
	}
}
