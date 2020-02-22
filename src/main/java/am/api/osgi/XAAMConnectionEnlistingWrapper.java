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
package am.api.osgi;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.AMConnection;
import am.api.AMConnectionFactory;
import am.api.XAAMConnection;
import am.api.XAAMConnectionFactory;
import am.api.exception.AMConnectionException;
import am.api.model.AMCredential;
import am.api.util.NLS;
import am.api.wrapper.AMConnectionKey;
import am.api.wrapper.AMConnectionWrapper;
import am.api.wrapper.AMNTConnectionKey;

public class XAAMConnectionEnlistingWrapper implements AMConnectionFactory, Serializable {

	private class TransactionListener implements Synchronization {

		private final Object key;

		public TransactionListener(Object key) {
			this.key = key;
		}

		public void afterCompletion(int status) {

			AMConnection connection = connectionMap.remove(key);
			if (connection != null) {
				connection.close();
			}
		}

		public void beforeCompletion() {

		}

	}

	private final static Logger logger = Logger.getLogger(XAAMConnectionEnlistingWrapper.class);

	private final static long serialVersionUID = 1L;

	private transient Map<Object, AMConnection> connectionMap = new ConcurrentHashMap<Object, AMConnection>();

	private transient TransactionManager transactionManager;

	private XAAMConnectionFactory wrappedCF;

	public XAAMConnectionEnlistingWrapper() {
		super();
	}

	private void enlist(Transaction transaction, XAResource xaResource, Object key) throws AMConnectionException {
		try {
			transaction.enlistResource(xaResource);
			transaction.registerSynchronization(new TransactionListener(key));
		} catch (Exception e) {
			try {
				transactionManager.setRollbackOnly();
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (SystemException e1) {
				e1.printStackTrace();
			}
		}
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
		XAAMConnectionEnlistingWrapper rhs = (XAAMConnectionEnlistingWrapper) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(wrappedCF, rhs.wrappedCF)
				.append(transactionManager, rhs.transactionManager).isEquals();
	}

	@Override
	public AMConnection getConnection() throws AMConnectionException {
		return getConnection(null);
	}

	@Override
	public AMConnection getConnection(AMCredential credential) throws AMConnectionException {

		Transaction transaction = getTransaction();

		synchronized (connectionMap) {

			if (transaction != null) {

				Object key = new AMConnectionKey(credential, transaction);

				logger.debug(String.format("key = [%s]", key));

				AMConnection connection = connectionMap.get(key);

				logger.debug(String.format("existing connection = [%s]", connection));

				if (connection == null) {
					XAAMConnection xaConnection = wrappedCF.getXAConnection(credential);
					connection = xaConnection.getConnection();
					enlist(transaction, xaConnection.getXAResource(), key);
					connectionMap.put(key, connection);
				}

				return getEnlistedConnection(connection, true, key);

			} else {

				AMConnection xaConnection = wrappedCF.getXAConnection(credential).getConnection();

				Object key = new AMNTConnectionKey(credential, xaConnection.hashCode());

				connectionMap.put(key, xaConnection);

				return getEnlistedConnection(xaConnection, false, key);

			}

		}
	}

	private AMConnection getEnlistedConnection(AMConnection connection, boolean enlisted, Object key)
			throws AMConnectionException {

		AMConnectionWrapper wrapper = new AMConnectionWrapper(connection, enlisted, key, this);

		logger.debug(String.format("wrapper = [%s]", wrapper));

		return wrapper;
	}

	private Transaction getTransaction() throws AMConnectionException {
		try {
			return (transactionManager.getStatus() == Status.STATUS_ACTIVE) ? transactionManager.getTransaction()
					: null;
		} catch (SystemException e) {
			AMConnectionException amE = new AMConnectionException(NLS.ERRORS.getString("transaction.cannot.retrieve"),
					e);
			throw amE;
		}
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public XAAMConnectionFactory getWrappedCF() {
		return wrappedCF;
	}

	public int hashCode() {
		return new HashCodeBuilder(91, 1).appendSuper(super.hashCode()).append(wrappedCF).append(transactionManager)
				.toHashCode();
	}

	public void init() {
		Assert.notNull(transactionManager, "transactionManager is required.");
		Assert.notNull(wrappedCF, "wrappedCF is required.");
	}

	public void performCleanup() {
		logger.debug("connectionMap.size() = {}", connectionMap.size());
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setWrappedCF(XAAMConnectionFactory wrappedCF) {
		this.wrappedCF = wrappedCF;
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("wrappedCF", wrappedCF)
				.append("transactionManager", transactionManager).toString();
	}

	public void unregister(Object key) {

		synchronized (connectionMap) {
			connectionMap.remove(key);
		}

	}

}
