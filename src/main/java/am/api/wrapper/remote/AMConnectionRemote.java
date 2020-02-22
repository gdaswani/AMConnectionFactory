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
package am.api.wrapper.remote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import am.api.AMConnection;
import am.api.AMHandle;
import am.api.exception.AMConnectionException;
import am.api.exception.CallTimeOutException;
import am.api.model.AMCredential;
import am.api.model.AMDate;
import am.api.model.AMString;
import am.api.wrapper.AMBaseConnection;
import am.api.wrapper.ConnectionPool;
import am.server.AMAPIProcess;
import am.server.client.AMLibraryRemote;
import am.server.client.ReturnWithString;

final class AMConnectionRemote extends AMBaseConnection {

	private final static Logger logger = Logger.getLogger(AMConnectionRemote.class);

	private final AMCredential credential;

	private final long defaultCallTimeOutInMs;

	private final ConnectionPool pool;

	private AMLibraryRemote remoteLibrary;

	private final int serverPort;

	public AMConnectionRemote(ConnectionPool pool, AMCredential credential, int serverPort,
			long defaultCallTimeOutInMs) {

		super();

		this.pool = pool;
		this.credential = credential;
		this.serverPort = serverPort;

		if (defaultCallTimeOutInMs <= 0L) {
			this.defaultCallTimeOutInMs = Long.MAX_VALUE;
		} else {
			this.defaultCallTimeOutInMs = defaultCallTimeOutInMs;
		}

		String rmiPath = String.format("rmi://localhost:%1$d/%2$s", serverPort, AMAPIProcess.class.getName());

		logger.info("rmiPath={}", rmiPath);

		try {
			remoteLibrary = (AMLibraryRemote) Naming.lookup(rmiPath);
			logger.debug("remoteLibrary={}", remoteLibrary);
			remoteLibrary.setCallTimeOutInMs(this.defaultCallTimeOutInMs);
			logger.debug("setting callTimeOutInMs to default value {}", this.defaultCallTimeOutInMs);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void cleanup() {
		try {
			remoteLibrary.cleanup();
			resetCallTimeOut();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long clearLastError() {
		try {
			return remoteLibrary.clearLastError();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() {

		if (logger.isDebugEnabled()) {
			logger.debug("Current Thread = {}", Thread.currentThread().toString());
		}

		if (pool != null) {

			logger.debug(String.format("Returning connection [%s] to [%s]", this, pool));

			pool.returnObject(credential, this);

		} else {

			shutdown();

		}

	}

	@Override
	public long commit() {
		try {
			return remoteLibrary.commit();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long connectionName(AMString connectionName) {
		try {
			ReturnWithString returnValue = remoteLibrary.connectionName(connectionName);

			connectionName.setBuffer(returnValue.getStringVal().getBuffer());

			return returnValue.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateBasicToUnix(long tmTime) {
		try {
			return remoteLibrary.convertDateBasicToUnix(tmTime);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateIntlToUnix(String dateAsString) {
		try {
			return remoteLibrary.convertDateIntlToUnix(dateAsString);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateStringToUnix(String dateAsString) {
		try {
			return remoteLibrary.convertDateStringToUnix(dateAsString);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateUnixToBasic(long dateAsUnix) {
		try {
			return remoteLibrary.convertDateUnixToBasic(dateAsUnix);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateUnixToIntl(long UnixDate, AMString dateAsIntlStr) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.convertDateUnixToIntl(UnixDate, dateAsIntlStr);

			dateAsIntlStr.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDateUnixToString(long dateAsUnix, AMString dateAsStr) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.convertDateUnixToString(dateAsUnix, dateAsStr);

			dateAsStr.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertDoubleToString(double dSrc, AMString dblAsString) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.convertDoubleToString(dSrc, dblAsString);

			dblAsString.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long convertMonetaryToString(double dMonetarySrc, AMString dblAsString) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.convertMonetaryToString(dMonetarySrc, dblAsString);

			dblAsString.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double convertStringToDouble(String dblAsString) {
		try {
			return remoteLibrary.convertStringToDouble(dblAsString);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double convertStringToMonetary(String monetaryAsString) {
		try {
			return remoteLibrary.convertStringToMonetary(monetaryAsString);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {
		try {
			return remoteLibrary.createLink(srcRecHandle, linkName, srcDstHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle createRecord(String tblName) {
		try {
			return remoteLibrary.createRecord(tblName);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long currentDate() {
		try {
			return remoteLibrary.currentDate();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long currentServerDate() {
		try {
			return remoteLibrary.currentServerDate();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dateAdd(long startAsUnixDate, long duration) {
		try {
			return remoteLibrary.dateAdd(startAsUnixDate, duration);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dateAddLogical(long startAsUnixDate, long duration) {
		try {
			return remoteLibrary.dateAddLogical(startAsUnixDate, duration);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dateDiff(long endAsUnixDate, long startAsUnixDate) {
		try {
			return remoteLibrary.dateDiff(endAsUnixDate, startAsUnixDate);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbExecAql(String aqlQuery) {
		try {
			return remoteLibrary.dbExecAql(aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetDate(String aqlQuery) {
		try {
			return remoteLibrary.dbGetDate(aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double dbGetDouble(String aqlQuery) {
		try {
			return remoteLibrary.dbGetDouble(aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetLimitedList(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator, long maxSize, long errorType) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.dbGetLimitedList(aqlQuery, result, colSeparator,
					lineSeparator, idSeperator, maxSize, errorType);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetList(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.dbGetList(aqlQuery, result, colSeparator, lineSeparator,
					idSeperator);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetListEx(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.dbGetListEx(aqlQuery, result, colSeparator, lineSeparator,
					idSeperator);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetLong(String aqlQuery) {
		try {
			return remoteLibrary.dbGetLong(aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetPk(String tableName, String whereClause) {
		try {
			return remoteLibrary.dbGetPk(tableName, whereClause);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetString(String query, AMString result, String colSeparator, String lineSeparator) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.dbGetString(query, result, colSeparator, lineSeparator);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dbGetStringEx(String query, AMString result, String colSeparator, String lineSeparator) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.dbGetStringEx(query, result, colSeparator, lineSeparator);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {
		try {
			return remoteLibrary.deleteLink(srcRecHandle, linkName, srcDstHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long deleteRecord(AMHandle recordHandle) {
		try {
			return remoteLibrary.deleteRecord(recordHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long duplicateRecord(AMHandle recordHandle, long insert) {
		try {
			return remoteLibrary.duplicateRecord(recordHandle, insert);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long enumValList(String enumName, AMString value, long caseSensitive, String lineSeparator) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.enumValList(enumName, value, caseSensitive, lineSeparator);

			value.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
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
		AMConnectionRemote rhs = (AMConnectionRemote) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(serverPort, rhs.serverPort)
				.append(credential, rhs.credential).append(remoteLibrary, rhs.remoteLibrary).isEquals();
	}

	@Override
	public AMHandle executeActionById(long actionId, String tableName, long recordId) {
		try {
			return remoteLibrary.executeActionById(actionId, tableName, recordId);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle executeActionByName(String sqlName, String tableName, long recordId) {
		try {
			return remoteLibrary.executeActionByName(sqlName, tableName, recordId);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long exportDocument(long documentId, String fileName) {
		try {
			return remoteLibrary.exportDocument(documentId, fileName);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long flushTransaction() {
		try {
			return remoteLibrary.flushTransaction();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long formatCurrency(double amount, String currency, AMString result) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.formatCurrency(amount, currency, result);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long formatLong(long number, String format, AMString result) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.formatLong(number, format, result);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getComputeString(String tableName, long recordId, String template, AMString result) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getComputeString(tableName, recordId, template, result);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getField(AMHandle objHandle, long position) {
		try {
			return remoteLibrary.getField(objHandle, position);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldCount(AMHandle objHandle) {
		try {
			return remoteLibrary.getFieldCount(objHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos) {
		try {
			return remoteLibrary.getFieldDateOnlyValue(recHandle, fieldPos);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldDateValue(AMHandle recHandle, long fieldPos) {
		try {
			return remoteLibrary.getFieldDateOnlyValue(recHandle, fieldPos);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldDescription(AMHandle fieldHandle, AMString target) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldDescription(fieldHandle, target);

			target.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double getFieldDoubleValue(AMHandle objHandle, long fieldPos) {
		try {
			return remoteLibrary.getFieldDoubleValue(objHandle, fieldPos);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldFormat(AMHandle fldHandle, AMString target) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldFormat(fldHandle, target);

			target.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldFormatFromName(String tableName, String fieldName, AMString result) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldFormatFromName(tableName, fieldName, result);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getFieldFromName(AMHandle objHandle, String fielddName) {
		try {
			return remoteLibrary.getFieldFromName(objHandle, fielddName);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldLabel(AMHandle fldHandle, AMString result) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldLabel(fldHandle, result);

			result.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldLabelFromName(tableName, fieldName, fieldLabel);

			fieldLabel.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldLongValue(AMHandle objHandle, long fieldPosition) {
		try {
			return remoteLibrary.getFieldLongValue(objHandle, fieldPosition);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldName(AMHandle objHandle, long fieldPositon, AMString fieldName) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldName(objHandle, fieldPositon, fieldName);

			fieldName.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldSize(AMHandle fldHandle) {
		try {
			return remoteLibrary.getFieldSize(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getFieldSqlName(fldHandle, fieldSQLName);

			fieldSQLName.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldStrValue(AMHandle qryHandle, long position, AMString target) {
		try {

			ReturnWithString returnValue = remoteLibrary.getFieldStrValue(qryHandle, position, target);

			target.setBuffer(returnValue.getStringVal().getBuffer());

			return returnValue.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldType(AMHandle fldHandle) {
		try {
			return remoteLibrary.getFieldType(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldUserType(AMHandle fldHandle) {
		try {
			return remoteLibrary.getFieldUserType(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRecordFromMainId(String tableName, long lId) {
		try {
			return remoteLibrary.getRecordFromMainId(tableName, lId);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRecordHandle(AMHandle qryHandle) {
		try {
			return remoteLibrary.getRecordHandle(qryHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getRecordId(AMHandle recHandle) {
		try {
			return remoteLibrary.getRecordId(recHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRelDstField(AMHandle fldHandle) {
		try {
			return remoteLibrary.getRelDstField(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRelSrcField(AMHandle fldHandle) {
		try {
			return remoteLibrary.getRelSrcField(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRelTable(AMHandle fldHandle) {
		try {
			return remoteLibrary.getRelTable(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getReverseLink(AMHandle fldHandle) {
		try {
			return remoteLibrary.getReverseLink(fldHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getSelfFromMainId(String tableName, long recordId, AMString recordDescription) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getSelfFromMainId(tableName, recordId, recordDescription);

			recordDescription.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	public int getServerPort() {
		return serverPort;
	}

	@Override
	public long getVersion(AMString amVersion) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.getVersion(amVersion);

			amVersion.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	public int hashCode() {
		return new HashCodeBuilder(99, 1).appendSuper(super.hashCode()).append(serverPort).append(credential)
				.append(remoteLibrary).toHashCode();
	}

	@Override
	public long importDocument(long docId, String tableName, String fileName, String category, String designation) {
		try {
			return remoteLibrary.importDocument(docId, tableName, fileName, category, designation);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long insertRecord(AMHandle recHandle) {
		try {
			return remoteLibrary.insertRecord(recHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long isConnected() {
		try {
			return remoteLibrary.isConnected();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long lastError() {
		try {
			return remoteLibrary.lastError();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long lastErrorMsg(AMString errorMessage) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.lastErrorMsg(errorMessage);

			errorMessage.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long listToString(AMString target, String source, String colSep, String lineSep, String idSep) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.listToString(target, source, colSep, lineSep, idSep);

			target.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long loginId() {
		try {
			return remoteLibrary.loginId();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long loginName(AMString loginName) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.loginName(loginName);

			loginName.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle openConnection(String database, String username, String password) {
		try {
			return remoteLibrary.openConnection(database, username, password);
		} catch (AMConnectionException connE) {

			setProcessingFlag(AMConnection.FLAG_NO_REUSE);

			throw connE;

		} catch (Exception e) {

			setProcessingFlag(AMConnection.FLAG_NO_REUSE);

			throw new AMConnectionException(e);
		}

	}

	@Override
	public long purgeRecord(AMHandle recHandle) {
		try {
			return remoteLibrary.purgeRecord(recHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle queryCreate() {
		try {
			return remoteLibrary.queryCreate();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long queryExec(AMHandle queryHandle, String aqlQuery) {
		try {
			return remoteLibrary.queryExec(queryHandle, aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long queryGet(AMHandle qryHandle, String aqlQuery) {
		try {
			return remoteLibrary.queryGet(qryHandle, aqlQuery);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long queryNext(AMHandle queryHandle) {
		try {
			return remoteLibrary.queryNext(queryHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long querySetAddMainField(AMHandle qryHandle, long addMainField) {
		try {
			return remoteLibrary.querySetAddMainField(qryHandle, addMainField);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long querySetFullMemo(AMHandle qryHandle, long fullMemo) {
		try {
			return remoteLibrary.querySetFullMemo(qryHandle, fullMemo);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle queryStartTable(AMHandle qryHandle) {
		try {
			return remoteLibrary.queryStartTable(qryHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long queryStop(AMHandle qryHandle) {
		try {
			return remoteLibrary.queryStop(qryHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long refreshAllCaches() {
		try {
			return remoteLibrary.refreshAllCaches();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long releaseHandle(AMHandle objHandle) {
		try {
			return remoteLibrary.releaseHandle(objHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void resetCallTimeOut() {

		try {
			logger.debug("resetting callTimeOutInMs to default value {}", defaultCallTimeOutInMs);
			remoteLibrary.setCallTimeOutInMs(this.defaultCallTimeOutInMs);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long rollBack() {
		try {
			return remoteLibrary.rollBack();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void setCallTimeOutInMs(long timeOutInMs) {

		try {
			logger.debug("callTimeOutInMs={}", timeOutInMs);
			remoteLibrary.setCallTimeOutInMs(timeOutInMs);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue) {
		try {
			return remoteLibrary.setFieldDateOnlyValue(recHandle, fieldName, dateOnlyValue);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue) {
		try {
			return remoteLibrary.setFieldDateValue(recHandle, fieldName, dateTimeValue);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value) {
		try {
			return remoteLibrary.setFieldDoubleValue(recHandle, fieldName, value);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long setFieldLongValue(AMHandle recHandle, String fieldName, long value) {
		try {
			return remoteLibrary.setFieldLongValue(recHandle, fieldName, value);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long setFieldStrValue(AMHandle recHandle, String fieldName, String value) {
		try {
			return remoteLibrary.setFieldStrValue(recHandle, fieldName, value);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void shutdown() {
		try {
			remoteLibrary.shutdown();
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long sqlTextConst(String aqlQuery, AMString target) {
		try {
			ReturnWithString returnWithStr = remoteLibrary.sqlTextConst(aqlQuery, target);

			target.setBuffer(returnWithStr.getStringVal().getBuffer());

			return returnWithStr.getReturnValue();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long startTransaction() {
		try {
			return remoteLibrary.startTransaction();
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("serverPort", serverPort)
				.append("credential", credential).append("remoteLibrary", remoteLibrary).toString();
	}

	@Override
	public long updateRecord(AMHandle recHandle) {
		try {
			return remoteLibrary.updateRecord(recHandle);
		} catch (CallTimeOutException t) {
			setProcessingFlag(AMConnection.FLAG_NO_REUSE);
			throw t;
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

}
