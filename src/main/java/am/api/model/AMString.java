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

import com.sun.jna.WString;

public class AMString implements Serializable {

	private final static long serialVersionUID = 1L;

	public final static AMString create(int bufferLen) {
		return new AMString(bufferLen);
	}

	public final static AMString create(long bufferlen) {
		if (bufferlen <= Integer.MAX_VALUE && bufferlen >= Integer.MIN_VALUE) {
			return new AMString((int) bufferlen);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public final static AMString create(byte[] buffer) {
		AMString amString = new AMString(buffer.length);
		amString.setBuffer(buffer);
		return amString;
	}

	private byte[] buffer;

	private AMString(int bufferLen) {
		super();
		buffer = new byte[bufferLen];
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
		AMString rhs = (AMString) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(buffer, rhs.buffer).isEquals();
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getBufferLength() {
		return buffer.length;
	}

	public int hashCode() {
		return new HashCodeBuilder(61, 27).append(buffer).toHashCode();
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void copyFrom(WString aString) {
		setBuffer(aString.toString().getBytes());
	}

	public String toString() {
		return new String(buffer);
	}

	public void fromString(String aString) {
		try {
			buffer = aString.getBytes();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
