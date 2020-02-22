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

import am.api.wrapper.AMConnectionDelegate;

public final class DbGetPk implements Callable<Long> {

	private final AMConnectionDelegate delegate;
	private final String tableName;
	private final String whereClause;

	public DbGetPk(AMConnectionDelegate delegate, String tableName, String whereClause) {
		super();
		this.delegate = delegate;
		this.tableName = tableName;
		this.whereClause = whereClause;
	}

	@Override
	public Long call() throws Exception {

		return delegate.dbGetPk(tableName, whereClause);

	}
}
