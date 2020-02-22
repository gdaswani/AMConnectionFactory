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

import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.AMConnection;
import am.api.exception.AMConnectionException;
import am.api.model.AMCredential;
import am.api.util.NLS;

public class ConnectionPool {

	private final static Logger logger = Logger.getLogger(ConnectionPool.class);

	private AMCredential defaultCredential;
	private int maxActive = 10;
	private int maxIdle = 5;
	private int maxTotal = 20;
	private long maxWait = 5000L;
	private long minEvictableIdleTimeMs = 300000L;
	private int minIdle = 0;
	private int numTestsPerEvictionRun = 5;
	private GenericKeyedObjectPool<AMCredential, AMBaseConnection> pool;
	private BasePoolableObjectFactory poolableObjectFactory = null;
	private long timeBetweenEvictionRunsMs = 600000L;

	public ConnectionPool() {
		super();
	}

	public void close() {
		try {
			pool.close();
		} catch (Exception error) {
			throw new IllegalStateException(error);
		}
	}

	public void destroy() {

		if (pool != null) {
			close();
		}

		pool = null;
		defaultCredential = null;
		maxActive = 10;
		maxIdle = 1;
		minIdle = 0;
		maxWait = 5000L;

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
		ConnectionPool rhs = (ConnectionPool) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(pool, rhs.pool)
				.append(defaultCredential, rhs.defaultCredential)
				.append(poolableObjectFactory, rhs.poolableObjectFactory).append(maxActive, maxActive)
				.append(maxIdle, rhs.maxIdle).append(maxWait, rhs.maxWait).append(minIdle, rhs.minIdle)
				.append(minEvictableIdleTimeMs, rhs.minEvictableIdleTimeMs)
				.append(numTestsPerEvictionRun, rhs.numTestsPerEvictionRun)
				.append(timeBetweenEvictionRunsMs, rhs.timeBetweenEvictionRunsMs).isEquals();
	}

	public AMConnection getConnection(AMCredential credential) throws AMConnectionException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("credential: [%s]", credential));
		}

		AMCredential credentialToLookup = (credential != null) ? credential : defaultCredential;

		AMBaseConnection pConnection = null;

		try {

			if (logger.isDebugEnabled()) {
				logger.debug(String.format(
						"Pool globalNumActive=[%d], globalNumIdle=[%d], keyNumActive=[%d], keyNumIdle=[%d]",
						pool.getNumActive(), pool.getNumIdle(), pool.getNumActive(credentialToLookup),
						pool.getNumIdle(credentialToLookup)));
			}

			pConnection = pool.borrowObject(credentialToLookup);

			if (pConnection != null) {
				pConnection.incrementReuse();
			}

		} catch (NoSuchElementException e) {
			logger.warn(String.format("Pool Exhausted, message=[%s]", e.getMessage()));
			AMConnectionException connE = new AMConnectionException(
					NLS.ERRORS.getString("connection.cannot.borrow.exhausted"), e);
			throw connE;
		} catch (Exception e) {

			if (e instanceof AMConnectionException) {

				throw (AMConnectionException) e;

			} else {

				AMConnectionException connE = new AMConnectionException(
						NLS.ERRORS.getString("connection.cannot.borrow.exception"), e);

				throw connE;

			}

		}

		return pConnection;
	}

	public AMCredential getDefaultCredential() {
		return defaultCredential;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public long getMaxWait() {
		return maxWait;
	}

	public long getMinEvictableIdleTimeMs() {
		return minEvictableIdleTimeMs;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public BasePoolableObjectFactory getPoolableObjectFactory() {
		return poolableObjectFactory;
	}

	public long getTimeBetweenEvictionRunsMs() {
		return timeBetweenEvictionRunsMs;
	}

	public int hashCode() {
		return new HashCodeBuilder(91, 5).append(pool).append(defaultCredential).append(poolableObjectFactory)
				.append(maxActive).append(maxIdle).append(maxWait).append(minIdle).append(minEvictableIdleTimeMs)
				.append(numTestsPerEvictionRun).append(timeBetweenEvictionRunsMs).toHashCode();
	}

	public void init() {

		Assert.notNull(defaultCredential, "defaultCredential is required.");
		Assert.notNull(poolableObjectFactory, "poolableObjectFactory is required.");
		Assert.isTrue(maxActive > 0, "maxActive needs to be greater than 0.");
		Assert.isTrue(maxIdle > 0, "maxIdle needs to be greater than 0.");
		Assert.isTrue(maxActive > maxIdle, "maxActive needs to be higher than maxIdle.");
		Assert.isTrue(maxIdle > minIdle, "maxIdle needs to be higher than minIdle");

		if (minIdle > 0) {

			Assert.isTrue(minEvictableIdleTimeMs > 0,
					"minEvictableIdleTimeMs needs to be greater than 0 when minIdle is greater than 0");
			Assert.isTrue(timeBetweenEvictionRunsMs > 0,
					"timeBetweenEvictionRunsMs needs to be greater than 0 when minIdle is greater than 0");

		}

		GenericKeyedObjectPoolConfig<AMBaseConnection> config = new GenericKeyedObjectPoolConfig<>();

		config.setMaxWaitMillis(maxWait);
		config.setMaxIdlePerKey(maxIdle);
		config.setMaxTotal(maxTotal);
		config.setMinIdlePerKey(minIdle);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMs);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMs);

		pool = new GenericKeyedObjectPool<>(poolableObjectFactory, config);

	}

	void invalidateObject(AMCredential credential, AMBaseConnection connection) {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("credential: %s, connection: %s", credential, connection));
		}

		try {
			pool.invalidateObject(credential, connection);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void performCleanup() {

	}

	public void returnObject(AMCredential credential, AMBaseConnection connection) {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("credential: %s, connection: %s", credential, connection));
		}

		try {
			pool.returnObject(credential, connection);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void setDefaultCredential(AMCredential defaultCredential) {
		this.defaultCredential = defaultCredential;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	public void setMinEvictableIdleTimeMs(long minEvictableIdleTimeMs) {
		this.minEvictableIdleTimeMs = minEvictableIdleTimeMs;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public void setPoolableObjectFactory(BasePoolableObjectFactory poolableObjectFactory) {
		this.poolableObjectFactory = poolableObjectFactory;
		poolableObjectFactory.setConnectionPool(this);
	}

	public void setTimeBetweenEvictionRunsMs(long timeBetweenEvictionRunsMs) {
		this.timeBetweenEvictionRunsMs = timeBetweenEvictionRunsMs;
	}

	public String toString() {
		return new ToStringBuilder(this).append("pool", pool).append("defaultCredential", defaultCredential)
				.append("poolableObjectFactory", poolableObjectFactory).append("maxActive", maxActive)
				.append("maxIdle", maxIdle).append("maxWait", maxWait).append("minIdle", minIdle)
				.append("minEvictableIdleTimeMs", minEvictableIdleTimeMs)
				.append("numTestsPerEvictionRun", numTestsPerEvictionRun)
				.append("timeBetweenEvictionRunsMs", timeBetweenEvictionRunsMs).toString();
	}
}
