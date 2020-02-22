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
package am.api.wrapper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.AMConnection;
import am.api.model.AMCredential;
import am.api.wrapper.local.AMConnectionLocal;

public class PoolableObjectFactory implements BasePoolableObjectFactory {

	private final static Logger logger = Logger.getLogger(PoolableObjectFactory.class);

	private ConnectionPool connectionPool = null;

	private int maxReuse;

	public PoolableObjectFactory() {
		super();
		maxReuse = 0;

	}

	@Override
	public void activateObject(AMCredential credential, PooledObject<AMBaseConnection> connection) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("activateObject credential: %s, connection: %s", credential, connection));
		}

	}

	@Override
	public void destroyObject(AMCredential credential, PooledObject<AMBaseConnection> connection) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("destroyObject credential: %s, connection: %s", credential, connection));
		}

		AMBaseConnection connImpl = (AMBaseConnection) connection;

		connImpl.shutdown();

		logger.debug("destroyed {}", connection);

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
		PoolableObjectFactory rhs = (PoolableObjectFactory) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(maxReuse, rhs.maxReuse).isEquals();
	}

	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}

	public int getMaxReuse() {
		return maxReuse;
	}

	public int hashCode() {
		return new HashCodeBuilder(101, 103).appendSuper(super.hashCode()).append(maxReuse).toHashCode();
	}

	public void init() {
		Assert.isTrue(maxReuse >= 0, "maxReuse must be greater than or equal to 0");
	}

	@Override
	public PooledObject<AMBaseConnection> makeObject(AMCredential credential) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("makeObject credential: %s", credential));
		}

		AMBaseConnection conn = new AMConnectionLocal(connectionPool, credential);

		conn.openConnection(credential.getDatabase(), credential.getUserName(), credential.getPassword());

		return new DefaultPooledObject<>(conn);
	}

	@Override
	public void passivateObject(AMCredential credential, PooledObject<AMBaseConnection> connection) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("passivateObject credential: %s, connection: %s", credential, connection));
		}

		connection.getObject().cleanup();

	}

	@Override
	public PoolableObjectFactory setConnectionPool(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
		return this;
	}

	public void setMaxReuse(int maxReuse) {
		this.maxReuse = maxReuse;
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("maxReuse", maxReuse).toString();
	}

	@Override
	public boolean validateObject(AMCredential credential, PooledObject<AMBaseConnection> connection) {

		AMBaseConnection base = connection.getObject();

		long isConnected = base.isConnected();
		int reuseCount = base.getReuseCount();
		boolean noReuse = base.isProcessingFlagSet(AMConnection.FLAG_NO_REUSE);

		logger.debug(String.format("isConnected = [%1$s], reuseCount = [%2$d], noReuse = [%3$s]", isConnected,
				reuseCount, Boolean.toString(noReuse)));

		if (false == noReuse) {

			if (maxReuse == 0 || maxReuse > 0 && reuseCount <= maxReuse) {
				if (1L == isConnected) {
					return true;
				}
			} else {
				logger.debug("reuse limit reached");
			}

		} else {
			logger.debug("reuse not allowed");
		}

		return false;

	}

}
