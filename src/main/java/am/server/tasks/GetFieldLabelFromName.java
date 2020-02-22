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

import am.api.model.AMString;
import am.api.wrapper.AMConnectionDelegate;
import am.server.client.ReturnWithString;

public final class GetFieldLabelFromName implements Callable<ReturnWithString> {

	private final AMConnectionDelegate delegate;
	private final String tableName;
	private final String fieldName;
	private final AMString fieldLabel;

	public GetFieldLabelFromName(AMConnectionDelegate delegate, String tableName, String fieldName,
			AMString fieldLabel) {
		super();
		this.delegate = delegate;
		this.tableName = tableName;
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
	}

	@Override
	public ReturnWithString call() throws Exception {

		return new ReturnWithString(delegate.getFieldLabelFromName(tableName, fieldName, fieldLabel), fieldLabel);

	}
}
