package com.alibaba.ide.plugin.eclipse.springext.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ConfigurationPoints;
import com.alibaba.citrus.springext.impl.ConfigurationPointsImpl;

public class SpringExtConfigUtil {
	public static ConfigurationPoint[] getAllConfigurationPoints(IProject project) {
		if (project == null) {
			return null;
		}
		try {
			String[] classPaths = JavaRuntime.computeDefaultRuntimeClassPath(JavaCore.create(project));
			if (classPaths == null) {
				return null;
			}
			ConfigurationPoints points = new ConfigurationPointsImpl(new URLClassLoader(trans2URL(classPaths)));
			Collection<ConfigurationPoint> pointCollection = points.getConfigurationPoints();
			return pointCollection.toArray(new ConfigurationPoint[] {});
		} catch (CoreException e) {
			return null;
		}
	}

	private static URL[] trans2URL(String[] classPaths) {
		List<URL> list = new ArrayList<URL>();
		for (String s : classPaths) {
			try {
				list.add(new File(s).toURI().toURL());
			} catch (MalformedURLException e) {
			}
		}
		return list.toArray(new URL[] {});
	}

}
