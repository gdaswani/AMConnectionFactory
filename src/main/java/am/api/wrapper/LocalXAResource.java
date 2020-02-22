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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import am.api.AMConnection;
import am.api.model.AMString;
import am.api.util.NLS;

public class LocalXAResource implements XAResource {

	private final static Logger logger = Logger
			.getLogger(LocalXAResource.class);

	private final AMConnection connection;
	private Xid currentXid;

	public LocalXAResource(AMConnection connection) {
		this.connection = connection;
	}

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Xid: [%s], onePhase = [%s]", xid,
					onePhase));
		}

		if (xid == null)
			throw new NullPointerException("xid is null");

		if (!this.currentXid.equals(xid))
			throw new XAException("Invalid Xid: expected " + this.currentXid
					+ ", but was " + xid);

		try {

			long status = connection.commit();

			if (status != 0L) {

				AMString errorMsg = AMString.create(1024);
				connection.lastErrorMsg(errorMsg);
				connection.clearLastError();
				throw new IllegalStateException(String.format(
						NLS.ERRORS.getString("transaction.cannot.commit"),
						status, errorMsg.toString()));

			}

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("connection: [%s]", connection));
			}

		} catch (Exception e) {
			throw (XAException) new XAException().initCause(e);
		} finally {
			this.currentXid = null;
		}
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Xid: [%s], flags = [%d]", xid, flags));
		}

		if (xid == null)
			throw new NullPointerException("xid is null");

		if (!this.currentXid.equals(xid))
			throw new XAException("Invalid Xid: expected " + this.currentXid
					+ ", but was " + xid);
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
		LocalXAResource rhs = (LocalXAResource) obj;
		return new EqualsBuilder().append(currentXid, rhs.currentXid)
				.append(connection, rhs.connection).isEquals();
	}

	@Override
	public void forget(Xid xid) throws XAException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Xid: [%s]", xid));
		}

		if (xid != null && xid.equals(currentXid)) {
			this.currentXid = null;
		}
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	public synchronized Xid getXid() {
		return currentXid;
	}

	public int hashCode() {
		return new HashCodeBuilder(91, 1).append(currentXid).append(connection)
				.toHashCode();
	}

	@Override
	public boolean isSameRM(XAResource xaResource) throws XAException {
		return this == xaResource;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		return XAResource.XA_OK;
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		return new Xid[0];
	}

	@Override
	public void rollback(Xid xid) throws XAException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Xid: [%s]", xid));
		}

		if (xid == null)
			throw new NullPointerException("xid is null");

		if (!this.currentXid.equals(xid))
			throw new XAException("Invalid Xid: expected " + this.currentXid
					+ ", but was " + xid);

		try {
			connection.rollBack();
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("rollback - connection: [%s]",
						connection));
			}

		} catch (Exception e) {
			throw (XAException) new XAException().initCause(e);
		} finally {
			this.currentXid = null;
		}
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return false;
	}

	public synchronized void start(Xid xid, int flag) throws XAException {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Xid: [%s], flag = [%d]", xid, flag));
		}

		switch (flag) {

		case XAResource.TMNOFLAGS:

			if (this.currentXid != null) {
				throw new XAException(
						"Already enlisted in another transaction with xid "
								+ xid);
			}

			try {
				connection.startTransaction();
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("start - connection: [%s]",
							connection));
				}
			} catch (Exception e) {
				throw (XAException) new XAException(
						"Count not turn start transaction for a XA transaction")
						.initCause(e);
			} finally {
				this.currentXid = xid;
			}

			break;

		case XAResource.TMRESUME:

			if (xid != this.currentXid) {
				throw new XAException(
						"Attempting to resume in different transaction: expected "
								+ this.currentXid + ", but was " + xid);
			}
			break;

		default:
			throw new XAException("Unknown start flag " + flag);

		}
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString())
				.append("currentXid", currentXid)
				.append("connection", connection).toString();
	}

}
