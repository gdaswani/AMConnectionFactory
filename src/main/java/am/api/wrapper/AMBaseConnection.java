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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.ToStringBuilder;

import am.api.AMConnection;

public abstract class AMBaseConnection implements AMConnection {

	private AtomicInteger reuseCount;
	private int processingFlags = 0;

	protected AMBaseConnection() {
		super();
		reuseCount = new AtomicInteger();
	}

	public abstract void cleanup();

	public int getReuseCount() {
		return reuseCount.get();
	}

	public void incrementReuse() {
		reuseCount.incrementAndGet();
	}

	public abstract void shutdown();

	public String toString() {
		return new ToStringBuilder(this).append("reuseCount", reuseCount).append("processingFlags", processingFlags)
				.toString();
	}

	public void setProcessingFlag(int flag) {
		processingFlags |= flag;
	}

	public boolean isProcessingFlagSet(int flag) {
		return (processingFlags & flag) == flag;
	}

}
