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

import javax.transaction.xa.XAResource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import am.api.AMConnection;
import am.api.XAAMConnection;
import am.api.XAAMConnectionFactory;
import am.api.model.AMCredential;
import am.api.wrapper.ConnectionPool;
import am.api.wrapper.LocalXAResource;
import am.api.wrapper.XAAMConnectionImpl;

public class XAPooledAMConnectionFactory extends ConnectionPool implements XAAMConnectionFactory {

	public XAPooledAMConnectionFactory() {
		super();
	}

	private XAResource createXAResource(AMConnection connection) {
		return new LocalXAResource(connection);
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
		return new EqualsBuilder().appendSuper(super.equals(obj)).isEquals();
	}

	@Override
	public AMConnection getConnection() {
		return getConnection(null);
	}

	public XAAMConnection getXAConnection() {

		AMConnection connection = getConnection();

		return new XAAMConnectionImpl(connection, createXAResource(connection));
	}

	public XAAMConnection getXAConnection(AMCredential credential) {
		AMConnection connection = getConnection(credential);

		return new XAAMConnectionImpl(connection, createXAResource(connection));
	}

	public int hashCode() {
		return new HashCodeBuilder(91, 5).appendSuper(super.hashCode()).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).toString();
	}

}
