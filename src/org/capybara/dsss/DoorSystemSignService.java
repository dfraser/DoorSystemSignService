package org.capybara.dsss;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.BasicConfigurator;

/**
 * Implements daemon lifecycle for the Door System Sign Service, for use with commons-daemon.
 * 
 * @author dfraser
 *
 */
public class DoorSystemSignService implements Daemon {

	private ExecutorService executor;

	@Override
	public void destroy() {

	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
	}

	@Override
	public void start() throws Exception {
		BasicConfigurator.configure();

		MessageQueueListener mql = new MessageQueueListener();
		executor = Executors.newSingleThreadExecutor();
		executor.execute(mql);
	}

	@Override
	public void stop() throws Exception {
		if (executor != null) {
			executor.shutdown();
		}
	}
	
	public static void main(String args[]) throws Exception {
		DoorSystemSignService dsss = new DoorSystemSignService();
		dsss.start();
	}
}
