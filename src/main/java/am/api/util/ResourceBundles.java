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
package am.api.util;

public enum ResourceBundles {

	ERRORS("am.api.resources.Errors"), MESSAGES("am.api.resources.Messages");

	private final String bundleName;

	private ResourceBundles(String bundleName) {
		this.bundleName = bundleName;
	}

	@Override
	public String toString() {
		return bundleName;
	}

}
