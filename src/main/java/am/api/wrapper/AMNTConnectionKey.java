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

import am.api.model.AMCredential;

public class AMNTConnectionKey {

	private final AMCredential credential;
	private final long hashCode;

	public AMNTConnectionKey(AMCredential credential, long hashCode) {
		super();
		this.credential = credential;
		this.hashCode = hashCode;
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
		AMNTConnectionKey rhs = (AMNTConnectionKey) obj;
		return new EqualsBuilder().append(credential, rhs.credential).append(hashCode, rhs.hashCode).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(15, 5).append(credential).append(hashCode).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).append("credential", credential).append("hashCode", hashCode).toString();
	}
}
