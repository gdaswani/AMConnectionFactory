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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.exception.AMConnectionException;
import am.api.model.AMCredential;
import am.api.util.NLS;
import am.api.wrapper.AMBaseConnection;

public class RemotePoolableObjectFactory extends am.api.wrapper.PoolableObjectFactory {

	private final static Logger logger = Logger.getLogger(RemotePoolableObjectFactory.class);

	private AMProcessManager processManager = null;

	private long defaultCallTimeOutInMs;

	public RemotePoolableObjectFactory() {
		super();
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
		RemotePoolableObjectFactory rhs = (RemotePoolableObjectFactory) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(processManager, rhs.processManager).isEquals();
	}

	public AMProcessManager getProcessManager() {
		return processManager;
	}

	public int hashCode() {
		return new HashCodeBuilder(101, 103).appendSuper(super.hashCode()).append(processManager).toHashCode();
	}

	public void init() {
		super.init();
		Assert.notNull(processManager, "processManager is required");
		Assert.isTrue(defaultCallTimeOutInMs >= 0L, "defaultCallTimeOutInMs needs to be 0 or greater");
	}

	@Override
	public PooledObject<AMBaseConnection> makeObject(AMCredential credential) throws Exception {

		AMConnectionRemote conn = null;

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("makeObject credential: %s", credential));
		}

		int serverPort = processManager.instantiate();

		logger.debug("serverPort = {}", serverPort);

		if (serverPort > 0) {

			conn = new AMConnectionRemote(getConnectionPool(), credential, serverPort, defaultCallTimeOutInMs);

			try {

				conn.openConnection(credential.getDatabase(), credential.getUserName(), credential.getPassword());

			} catch (Exception failure) {

				try {
					conn.shutdown();
				} catch (Exception e) {
					logger.warn(
							String.format("tried to proactively shutdown connection but failed, e=[%1$s]", failure));
				}

				if (failure instanceof AMConnectionException) {
					throw failure;
				} else {
					throw new AMConnectionException(failure);
				}
			}

		} else {
			throw new AMConnectionException(NLS.ERRORS.getString("connection.cannot.open"));
		}

		return new DefaultPooledObject<>(conn);
	}

	public void setProcessManager(AMProcessManager processManager) {
		this.processManager = processManager;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("processManager", processManager)
				.toString();
	}

	public long getDefaultCallTimeOutInMs() {
		return defaultCallTimeOutInMs;
	}

	public void setDefaultCallTimeOutInMs(long defaultCallTimeOutInMs) {
		this.defaultCallTimeOutInMs = defaultCallTimeOutInMs;
	}

}
