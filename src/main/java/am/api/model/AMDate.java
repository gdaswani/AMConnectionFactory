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
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement
public class AMDate implements Serializable {

	public final static long NULL_DATE_EQUIV = -413405696L;

	public final static long serialVersionUID = 1L;

	public final static AMDate create(Date date) {

		if (date == null) {
			throw new IllegalArgumentException("invalid date argument");
		}

		// java dates in time value are UTC / GMT already but
		// milliseconds, not seconds

		long value = date.getTime() / 1000l;

		if (((long) Integer.MAX_VALUE) < value) {
			throw new IllegalStateException();
		}

		return new AMDate(value);

	}

	public final static AMDate fromUNIXInt(long unixUTC) {

		return unixUTC > AMDate.NULL_DATE_EQUIV ? new AMDate(unixUTC) : null;

	}

	public final static Date toDate(long unixUTC) {

		return unixUTC > AMDate.NULL_DATE_EQUIV ? new Date(unixUTC * 1000l) : null;

	}

	private long value;

	public AMDate() {
		super();
	}

	private AMDate(long value) {
		this.value = value;
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
		AMDate rhs = (AMDate) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(value, rhs.value).isEquals();
	}

	public long getValue() {
		return value;
	}

	public int hashCode() {
		return new HashCodeBuilder(57, 1).append(value).toHashCode();
	}

	public void setValue(long value) {
		this.value = value;
	}

	public final Date toDate() {
		return new Date(value * 1000l);
	}

	public String toString() {
		return new ToStringBuilder(this).append("value", value).toString();
	}
}
