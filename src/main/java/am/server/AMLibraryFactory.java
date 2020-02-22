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
package am.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sun.jna.Native;

public class AMLibraryFactory {

	private static AMLibrary INSTANCE;

	public static synchronized AMLibrary getInstance() {

		if (INSTANCE == null) {

			InputStream configStream = null;

			String configFilePath = "/am/server/AMLibrary.properties";

			try {
				Properties properties = new Properties();

				configStream = AMLibraryFactory.class
						.getResourceAsStream(configFilePath);

				if (configStream != null) {
					properties.load(configStream);

					String apiLib = properties.getProperty("AMAPI_LIB");

					if (false == properties.isEmpty()) {

						System.loadLibrary(apiLib);

						INSTANCE = (AMLibrary) Native.loadLibrary(apiLib,
								AMLibrary.class);

						INSTANCE.AmStartup();

					} else {
						throw new IllegalStateException(
								String.format("properties file [%s] is empty",
										configFilePath));
					}
				} else {
					throw new IllegalStateException(String.format(
							"could not load properties file [%s]",
							configFilePath));
				}

			} catch (UnsatisfiedLinkError | IOException error) {
				throw new IllegalStateException(String.format(
						"Could not load Asset Manager native library: %s",
						error.getMessage()));
			}

		}

		return INSTANCE;

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
		return new EqualsBuilder().appendSuper(super.equals(obj)).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(101, 1).appendSuper(super.hashCode())
				.append(INSTANCE).toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString())
				.append("INSTANCE", INSTANCE).toString();
	}

}
