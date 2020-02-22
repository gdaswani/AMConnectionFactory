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

import javax.transaction.xa.XAResource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import am.api.AMConnection;
import am.api.XAAMConnection;

public class XAAMConnectionImpl implements XAAMConnection {

	private final AMConnection connection;
	private final XAResource resource;

	public XAAMConnectionImpl(AMConnection connection, XAResource resource) {
		super();
		this.connection = connection;
		this.resource = resource;
	}

	@Override
	public void close() {
		connection.close();
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
		XAAMConnectionImpl rhs = (XAAMConnectionImpl) obj;
		return new EqualsBuilder().append(connection, rhs.connection).append(resource, rhs.resource).isEquals();
	}

	@Override
	public AMConnection getConnection() {
		return connection;
	}

	public String toString() {
		return new ToStringBuilder(this).append("connection", connection).append("resource", resource).toString();
	}

	@Override
	public XAResource getXAResource() {
		return resource;
	}

	public int hashCode() {
		return new HashCodeBuilder(103, 105).append(connection).append(resource).toHashCode();
	}

}
