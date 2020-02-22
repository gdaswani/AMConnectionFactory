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

public final class ImportDocument implements Callable<Long> {

	private final AMConnectionDelegate delegate;
	private final long docId;
	private final String tableName;
	private final String fileName;
	private final String category;
	private final String designation;

	public ImportDocument(AMConnectionDelegate delegate, long docId, String tableName, String fileName, String category,
			String designation) {
		super();
		this.delegate = delegate;
		this.docId = docId;
		this.tableName = tableName;
		this.fileName = fileName;
		this.category = category;
		this.designation = designation;
	}

	@Override
	public Long call() throws Exception {

		return delegate.importDocument(docId, tableName, fileName, category, designation);

	}
}
