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
package am.server.client;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import am.api.model.AMString;

public class ReturnWithString implements Serializable {

	private final static long serialVersionUID = 1L;

	private long returnValue;
	private AMString stringVal;

	public ReturnWithString() {
		super();
	}

	public ReturnWithString(long returnValue, AMString aStringVal) {
		super();
		this.returnValue = returnValue;
		this.stringVal = aStringVal;
	}

	public long getReturnValue() {
		return returnValue;
	}

	public ReturnWithString setReturnValue(long returnValue) {
		this.returnValue = returnValue;
		return this;
	}

	public AMString getStringVal() {
		return stringVal;
	}

	public ReturnWithString setStringVal(AMString aStringVal) {
		this.stringVal = aStringVal;
		return this;
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
		ReturnWithString rhs = (ReturnWithString) obj;
		return new EqualsBuilder().append(returnValue, rhs.returnValue).append(stringVal, rhs.stringVal).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(19, 53).append(returnValue).append(stringVal).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).append("returnValue", returnValue).append("stringVal", stringVal).toString();
	}
}
