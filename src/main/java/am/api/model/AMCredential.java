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
package am.api.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AMCredential implements Serializable {

	private final static long serialVersionUID = 1L;

	private final String database;

	private final String password;

	private final String userName;

	public AMCredential(String database, String userName, String password) {
		super();
		this.database = database;
		this.userName = userName;
		this.password = password;
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
		AMCredential rhs = (AMCredential) obj;
		return new EqualsBuilder().append(database, rhs.database)
				.append(userName, rhs.userName).append(password, rhs.password)
				.isEquals();
	}

	public String getDatabase() {
		return database;
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}

	public int hashCode() {
		return new HashCodeBuilder(1, 3).append(database).append(userName)
				.append(password).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).append("database", database)
				.append("userName", userName).append("password", "*******")
				.toString();
	}
}
