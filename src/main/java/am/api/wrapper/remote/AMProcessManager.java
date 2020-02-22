/*
    This file is part of AMConnectionFactory.

    AMConnectionFactory is free software: you can redistribute it and/or modify
    it under the terms of the GNU LESSER General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AMConnectionFactory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AMConnectionFactory.  If not, see <https://www.gnu.org/licenses/>.
 */
package am.api.wrapper.remote;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.util.NLS;

public class AMProcessManager {

	private final static Logger logger = Logger.getLogger(AMProcessManager.class);

	private String classPath;
	private AtomicInteger currentPortNumber;
	private String logPath;
	private int maxPoolSize = 0;
	private ExecutorService processExecutor;
	private Map<Integer, ProcessInfoWorker> processMap;
	private String reaperScriptPath;
	private String shellPath;
	private int startingPortNumber = 0;

	public AMProcessManager() {
		super();
	}

	public void cleanup() {

		logger.debug("Trying to cleanup");

		Set<Integer> keys = new HashSet<>(processMap.keySet());

		for (Integer key : keys) {
			unregister(key);
		}

		processExecutor.shutdownNow();

	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		AMProcessManager rhs = (AMProcessManager) obj;

		return new EqualsBuilder().appendSuper(super.equals(obj)).append(maxPoolSize, rhs.maxPoolSize)
				.append(startingPortNumber, rhs.startingPortNumber).append(shellPath, rhs.shellPath)
				.append("reaperScriptPath", rhs.reaperScriptPath).append(logPath, rhs.logPath)
				.append(classPath, rhs.classPath).isEquals();
	}

	private synchronized ProcessInfoWorker getAvailable(BlockingQueue<Boolean> statusQ) {

		do {

			if (processMap.size() < maxPoolSize) {

				Integer key = currentPortNumber.accumulateAndGet(1, (index, inc) -> {
					return ++index >= (startingPortNumber + maxPoolSize) ? startingPortNumber : index;
				});

				if (!processMap.containsKey(key)) {

					if (hasProcessLock(key)) {
						killUnregisteredProcess(key);
					}

					ProcessInfoWorker worker = new ProcessInfoWorker(key, classPath, logPath, statusQ, this);

					processMap.put(key, worker);

					return worker;

				}

			} else {
				return null;
			}

		} while (true);
	}

	public String getClassPath() {
		return classPath;
	}

	private String getLockFileName(final Integer key) {

		String lockFileName = String.format("%1$sAMAPIProcess_%2$d.lck", logPath, key);

		logger.debug("lockFileName = {}", lockFileName);

		return lockFileName;
	}

	public String getLogPath() {
		return logPath;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public String getReaperScriptPath() {
		return reaperScriptPath;
	}

	public String getShellPath() {
		return shellPath;
	}

	public int getStartingPortNumber() {
		return startingPortNumber;
	}

	public int hashCode() {

		return new HashCodeBuilder(99, 3).append(classPath).append(logPath).append(maxPoolSize).append(processExecutor)
				.append(processMap).append(reaperScriptPath).append(shellPath).append(startingPortNumber).toHashCode();

	}

	private boolean hasProcessLock(final Integer key) {

		boolean hasLock = false;

		try (FileChannel channel = new RandomAccessFile(new File(getLockFileName(key)), "rw").getChannel()) {

			FileLock lock = channel.tryLock(0, 1024, false);

			if (lock == null) {
				// could not get lock
				hasLock = true;
			}

		} catch (IOException e) {
			logger.error("caught exception", e);
			hasLock = true;
		}

		return hasLock;
	}

	public void init() {

		logger.debug("startingPortNumber = {}", startingPortNumber);
		logger.debug("maxPoolSize = {}", maxPoolSize);
		logger.debug("classPath = {}", classPath);
		logger.debug("logPath = {}", logPath);

		Assert.isTrue(startingPortNumber != 0, "startingPortNumber must not be 0");
		Assert.isTrue(maxPoolSize > 0, "maxPoolSize must be great than 0");
		Assert.notNull(classPath, "classPath is required");
		Assert.isTrue(logPath != null && new File(logPath).isDirectory(), "logPath required and must be a directory");
		Assert.notNull(shellPath, "shellPath is required");
		Assert.notNull(reaperScriptPath, "reaperScriptPath is required");

		currentPortNumber = new AtomicInteger(startingPortNumber);

		processMap = new ConcurrentHashMap<>();

		processExecutor = Executors.newFixedThreadPool(maxPoolSize, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}
		});

	}

	public int instantiate() {

		logger.debug("Trying to instantiate new worker, currentPortNumber = {}", currentPortNumber);

		final BlockingQueue<Boolean> statusQ = new ArrayBlockingQueue<Boolean>(1);

		ProcessInfoWorker worker;

		do {

			worker = getAvailable(statusQ);

			if (worker == null) {
				logger.warn(NLS.MESSAGES.getString("processmanager.instantiate.wait"));
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					logger.warn(e);
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e);
				}
			}

		} while (worker == null);

		processExecutor.execute(worker);

		logger.debug("Submitted worker {}, waiting for it to get ready", worker);

		Boolean isReady = Boolean.FALSE;

		try {

			isReady = statusQ.poll(30L, TimeUnit.SECONDS);

			if (isReady != null && isReady) {
				return worker.getKey();
			} else {
				unregister(worker.getKey());
			}
		} catch (InterruptedException e) {
			logger.warn(e);
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

		return 0;

	}

	private void killUnregisteredProcess(final Integer key) {

		logger.warn("Killing unregistered process with key of {}", key);

		try {
			Process process = new ProcessBuilder(shellPath, "-ExecutionPolicy", "Bypass", "-File", reaperScriptPath,
					"-amServerPort", String.format("%1$d", key)).start();

			logger.info("Waiting for process = {} to complete", process);

			process.waitFor(1L, TimeUnit.MINUTES);

			logger.info("Process = {} completed", process);

		} catch (InterruptedException e) {
			logger.warn("Caught exception while trying to kill process", e);
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		} catch (IOException e) {
			logger.warn("Caught exception while trying to kill process", e);
			throw new IllegalStateException(e);
		}
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath.endsWith("\\") ? logPath : logPath + "\\";
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public void setReaperScriptPath(String reaperScriptPath) {
		this.reaperScriptPath = reaperScriptPath;
	}

	public void setShellPath(String shellPath) {
		this.shellPath = shellPath;
	}

	public void setStartingPortNumber(int startingPortNumber) {
		this.startingPortNumber = startingPortNumber;
	}

	public String toString() {
		return new ToStringBuilder(this).append("classPath", classPath).append("logPath", logPath)
				.append("maxPoolSize", maxPoolSize).append("reaperScriptPath", reaperScriptPath)
				.append("shellPath", shellPath).append("startingPortNumber", startingPortNumber).toString();
	}

	public synchronized void unregister(final Integer key) {

		if (key != null) {

			ProcessInfoWorker worker = processMap.remove(key);

			if (worker != null) {

				logger.debug("Trying to unregister worker {}", worker);

				if (worker.getProcess() != null && worker.getProcess().isAlive()) {
					worker.forciblyDestroy();
				}

			} else {
				logger.warn("Tried to unregister worker with key = {} but it did not exist in the processMap, why?",
						key);
			}

		}

	}

}
