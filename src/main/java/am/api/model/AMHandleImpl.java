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

import am.api.AMHandle;
import am.api.AMHandleType;

public final class AMHandleImpl implements AMHandle, Serializable {

	private final static long serialVersionUID = 1L;

	private final String createdFrom;

	private final AMHandleType handleType;

	private final String id;

	public AMHandleImpl(String id, AMHandleType handleType, String createdFrom) {
		super();

		this.id = id;
		this.handleType = handleType;
		this.createdFrom = createdFrom;
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
		AMHandleImpl rhs = (AMHandleImpl) obj;
		return new EqualsBuilder().append(id, rhs.id)
				.append(handleType, rhs.handleType)
				.append(createdFrom, rhs.createdFrom).isEquals();
	}

	public String getCreatedFrom() {
		return createdFrom;
	}

	public AMHandleType getHandleType() {
		return handleType;
	}

	public String getId() {
		return id;
	}

	public int hashCode() {
		return new HashCodeBuilder(9, 5).append(id).append(handleType)
				.append(createdFrom).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).append("id", id)
				.append("handleType", handleType)
				.append("createdFrom", createdFrom).toString();
	}
}
