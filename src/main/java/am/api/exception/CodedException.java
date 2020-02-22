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
package am.api.exception;

public class CodedException extends RuntimeException {

	private final static long serialVersionUID = 1L;

	private final String code;

	public CodedException() {
		super();
		this.code = null;
	}

	protected CodedException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return String.format("[%1$s] %2$s", code, super.getMessage());
	}
}
