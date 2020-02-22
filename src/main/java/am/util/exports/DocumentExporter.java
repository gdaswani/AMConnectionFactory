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

package am.util.exports;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import am.api.AMConnection;
import am.api.AMHandle;
import am.api.model.AMCredential;
import am.api.model.AMString;
import am.api.osgi.XAPooledAMConnectionFactory;
import am.api.wrapper.PoolableObjectFactory;

public class DocumentExporter {

	private final static Logger logger = Logger.getLogger(DocumentExporter.class);

	private void run() {

		AMCredential credential = new AMCredential("DEV", "SOMEUSER", "SOMEPASSWORD");

		XAPooledAMConnectionFactory pFactory = new XAPooledAMConnectionFactory();
		pFactory.setDefaultCredential(credential);
		pFactory.setPoolableObjectFactory(new PoolableObjectFactory());
		pFactory.init();

		try (AMConnection amConnection = pFactory.getConnection()) {

			AMHandle queryHandle = amConnection.queryCreate();

			Assert.isTrue(0L == amConnection.queryExec(queryHandle, "SELECT lDocId, FileName FROM amDocument"),
					"Could not perform query, or result is empty");

			do {

				long lDocId = amConnection.getFieldLongValue(queryHandle, 0);

				logger.info("documentId = {}", lDocId);

				AMString fileName = AMString.create(255);

				amConnection.getFieldStrValue(queryHandle, 1, fileName);

				if (!"".equals(fileName.toString())) {

					String targetFileName = FilenameUtils.getName(fileName.toString());

					logger.info("exporting Document to = {}", targetFileName);

					if (0L != amConnection.exportDocument(lDocId,
							String.format("export\\documents\\%1$s", targetFileName))) {

						logger.error(String.format("Could not export document - %1$s", targetFileName));
					}

				} else {
					logger.warn("documentId = {} does not have a filename", lDocId);
				}

			} while (0L == amConnection.queryNext(queryHandle));

			Assert.isTrue(0L == amConnection.releaseHandle(queryHandle), "Could not release qryHandle");

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static void main(String[] args) {
		new DocumentExporter().run();
	}

}
