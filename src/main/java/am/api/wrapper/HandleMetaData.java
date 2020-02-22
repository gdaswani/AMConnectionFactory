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

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sun.jna.Pointer;

public class HandleMetaData {
	private final Pointer pointer;
	private Date registerDate;

	public HandleMetaData(Pointer pointer) {
		super();
		registerDate = new Date();
		this.pointer = pointer;
	}

	public Pointer getPointer() {
		return pointer;
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public String toString() {
		return new ToStringBuilder(this).append("registerDate", registerDate).append("pointer", pointer).toString();
	}

	public int hashCode() {
		return new HashCodeBuilder(91, 7).append(registerDate).append(pointer).toHashCode();
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
		HandleMetaData rhs = (HandleMetaData) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(registerDate, rhs.registerDate)
				.append(pointer, rhs.pointer).isEquals();
	}
}
