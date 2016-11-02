package com.rcpcompany.ide.jobnotifier.internal;

import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private IJobChangeListener myJobListener;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.rcpcompany.ide.jobnotifier"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		myJobListener = new JobListener();

		Job.getJobManager().addJobChangeListener(myJobListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (myJobListener != null) {
			Job.getJobManager().removeJobChangeListener(myJobListener);
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
