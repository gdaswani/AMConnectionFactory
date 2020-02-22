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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import am.server.AMAPIProcess;

public class ProcessStatusReader implements Runnable {

	private final static Logger logger = Logger
			.getLogger(ProcessStatusReader.class);

	private InputStream inputStream = null;
	private ProcessInfoWorker processInfoWorker = null;

	public ProcessStatusReader(ProcessInfoWorker processInfoWorker,
			InputStream inputStream) {
		super();
		this.processInfoWorker = processInfoWorker;
		this.inputStream = inputStream;
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
		ProcessStatusReader rhs = (ProcessStatusReader) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj))
				.append(processInfoWorker, rhs.processInfoWorker)
				.append(inputStream, rhs.inputStream).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(98, 7).appendSuper(super.hashCode())
				.append(processInfoWorker).append(inputStream).toHashCode();
	}
	
	public void run() {

		String line = null;

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				inputStream));

		try {

			logger.debug("Trying to read status");

			while ((line = stdIn.readLine()) != null) {

				logger.debug("readLine()={}", line);

				if (line.startsWith(AMAPIProcess.STATUS_READY)) {

					int processId = Integer.parseInt(line.substring(line
							.indexOf(':') + 1));

					logger.debug("processId [{}] retrieved for key [{}]",
							processId, processInfoWorker.getKey());

					processInfoWorker.setProcessId(processId);

					processInfoWorker.signalReady();
				} else if (line.startsWith(AMAPIProcess.STATUS_FAILED)) {
					processInfoWorker.signalFailure();

				}

			}
		} catch (IOException e) {
			logger.debug(e);
		}

	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString())
				.append("processInfoWorker", processInfoWorker)
				.append("inputStream", inputStream).toString();
	}
}
