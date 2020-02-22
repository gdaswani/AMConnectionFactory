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
package am.server.tasks;

import java.util.concurrent.Callable;

import am.api.AMHandle;
import am.api.wrapper.AMConnectionDelegate;

public final class InsertRecord implements Callable<Long> {

	private final AMConnectionDelegate delegate;
	private final AMHandle recHandle;

	public InsertRecord(AMConnectionDelegate delegate, AMHandle recHandle) {
		super();
		this.delegate = delegate;
		this.recHandle = recHandle;
	}

	@Override
	public Long call() throws Exception {

		return delegate.insertRecord(recHandle);

	}
}
