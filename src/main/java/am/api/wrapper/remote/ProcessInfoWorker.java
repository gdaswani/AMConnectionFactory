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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import am.server.AMAPIProcess;

public class ProcessInfoWorker implements Runnable {

	private final static Logger logger = Logger.getLogger(ProcessInfoWorker.class);

	private final String classPath;
	private final Integer key;
	private final String logPath;
	private Process process = null;
	private volatile int processId;
	private AMProcessManager amProcessManager;

	private final BlockingQueue<Boolean> statusQ;

	public ProcessInfoWorker(Integer key, String classPath, String logPath, BlockingQueue<Boolean> statusQ,
			AMProcessManager amProcessManager) {
		super();
		this.key = key;
		this.classPath = classPath;
		this.logPath = logPath;
		this.statusQ = statusQ;
		this.amProcessManager = amProcessManager;
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
		ProcessInfoWorker rhs = (ProcessInfoWorker) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(key, rhs.key).append(processId, rhs.processId)
				.append(process, rhs.process).isEquals();
	}

	public synchronized void forciblyDestroy() {
		try {
			process.waitFor(1, TimeUnit.MINUTES);
		} catch (InterruptedException interrupted) {
			logger.warn(interrupted);
			Thread.currentThread().interrupt();
			throw new IllegalStateException(interrupted);
		} finally {
			if (process.isAlive()) {
				logger.info("Destroying with force [{}]", process);
				process.destroyForcibly();
			}
		}
	}

	public Integer getKey() {
		return key;
	}

	public Process getProcess() {
		return process;
	}

	public int getProcessId() {
		return processId;
	}

	public int hashCode() {
		return new HashCodeBuilder(99, 5).appendSuper(super.hashCode()).append(key).append(processId).append(process)
				.toHashCode();
	}

	@Override
	public void run() {

		ProcessBuilder processBuilder = new ProcessBuilder("java.exe",
				String.format("-D%1$s=%2$d", AMAPIProcess.PARAM_RMI_SERVER_PORT, key),
				String.format("-D%1$s=%2$s", AMAPIProcess.PARAM_LOG_PATH, logPath), "-Xmx64M", "-Xrs", "-classpath",
				classPath, AMAPIProcess.class.getName());

		try {

			processBuilder.redirectError(new File(String.format("%1$sAMAPIProcess_%2$d.log", logPath, key)));

			process = processBuilder.start();

			logger.debug("process={}, this={}", process, this);

			new Thread(new ProcessStatusReader(this, process.getInputStream())).start();

			if (process.isAlive()) {
				try {
					process.waitFor(1, TimeUnit.DAYS);
				} catch (InterruptedException ignore) {
					logger.warn(ignore);
					Thread.currentThread().interrupt();
					throw new IllegalStateException(ignore);
				} finally {
					if (process.isAlive()) {
						forciblyDestroy();
					}
					amProcessManager.unregister(key);
				}
			} else {
				try {
					statusQ.put(Boolean.FALSE);
				} catch (Exception e) {
					logger.warn(e);
				}
			}

		} catch (IOException e) {

			logger.fatal("Could not start AMAPIProcess with key [{}], e = [{}]", key, e);

			signalFailure();

		}
	}

	public void setProcessId(int processId) {
		this.processId = processId;

	}

	public void signalFailure() {
		statusQ.add(Boolean.FALSE);
	}

	public void signalReady() {
		statusQ.add(Boolean.TRUE);
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("key", key).append("processId", processId)
				.append("process", process).toString();
	}
}
