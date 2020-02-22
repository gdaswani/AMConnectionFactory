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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import am.api.AMConnection;
import am.api.AMHandle;
import am.api.exception.AMConnectionException;
import am.api.model.AMDate;
import am.api.model.AMString;
import am.api.osgi.XAAMConnectionEnlistingWrapper;
import am.api.util.NLS;

public final class AMConnectionWrapper implements AMConnection {

	private boolean closed;

	private AMConnection connection;

	private final XAAMConnectionEnlistingWrapper connectionFactory;

	private final boolean enlisted;

	private final Object key;

	public AMConnectionWrapper(AMConnection connection, boolean enlisted, Object key,
			XAAMConnectionEnlistingWrapper connectionFactory) {
		super();
		this.enlisted = enlisted;
		this.connection = connection;
		this.key = key;
		this.connectionFactory = connectionFactory;
	}

	public long clearLastError() {
		return connection.clearLastError();
	}

	public void close() {
		if (!closed) {
			try {
				if (!enlisted) {
					connectionFactory.unregister(key);
					connection.close();
				}
			} finally {
				closed = true;
			}

		}
	}

	public long commit() {
		if (enlisted) {
			throw new IllegalStateException(NLS.MESSAGES.getString("connection.enlisted.commit"));
		}
		return connection.commit();
	}

	public long connectionName(AMString connectionName) {
		return connection.connectionName(connectionName);
	}

	public long convertDateBasicToUnix(long tmTime) {
		return connection.convertDateBasicToUnix(tmTime);
	}

	public long convertDateIntlToUnix(String dateAsString) {
		return connection.convertDateIntlToUnix(dateAsString);
	}

	public long convertDateStringToUnix(String dateAsString) {
		return connection.convertDateStringToUnix(dateAsString);
	}

	public long convertDateUnixToBasic(long dateAsUnix) {
		return connection.convertDateUnixToBasic(dateAsUnix);
	}

	public long convertDateUnixToIntl(long UnixDate, AMString dateAsIntlStr) {
		return connection.convertDateUnixToIntl(UnixDate, dateAsIntlStr);
	}

	public long convertDateUnixToString(long dateAsUnix, AMString dateAsStr) {
		return connection.convertDateUnixToString(dateAsUnix, dateAsStr);
	}

	public long convertDoubleToString(double dSrc, AMString dblAsString) {
		return connection.convertDoubleToString(dSrc, dblAsString);
	}

	public long convertMonetaryToString(double dMonetarySrc, AMString dblAsString) {
		return connection.convertMonetaryToString(dMonetarySrc, dblAsString);
	}

	public double convertStringToDouble(String dblAsString) {
		return connection.convertStringToDouble(dblAsString);
	}

	public double convertStringToMonetary(String monetaryAsString) {
		return connection.convertStringToMonetary(monetaryAsString);
	}

	public long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {
		return connection.createLink(srcRecHandle, linkName, srcDstHandle);
	}

	public AMHandle createRecord(String tblName) {
		return connection.createRecord(tblName);
	}

	public long currentDate() {
		return connection.currentDate();
	}

	public long currentServerDate() {
		return connection.currentServerDate();
	}

	public long dateAdd(long startAsUnixDate, long duration) {
		return connection.dateAdd(startAsUnixDate, duration);
	}

	public long dateAddLogical(long startAsUnixDate, long duration) {
		return connection.dateAddLogical(startAsUnixDate, duration);
	}

	public long dateDiff(long endAsUnixDate, long startAsUnixDate) {
		return connection.dateDiff(endAsUnixDate, startAsUnixDate);
	}

	public long dbExecAql(String aqlQuery) {
		return connection.dbExecAql(aqlQuery);
	}

	public long dbGetDate(String aqlQuery) {
		return connection.dbGetDate(aqlQuery);
	}

	public double dbGetDouble(String aqlQuery) {
		return connection.dbGetDouble(aqlQuery);
	}

	public long dbGetLimitedList(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator, long maxSize, long errorType) {
		return connection.dbGetLimitedList(aqlQuery, result, colSeparator, lineSeparator, idSeperator, maxSize,
				errorType);
	}

	public long dbGetList(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator) {
		return connection.dbGetList(aqlQuery, result, colSeparator, lineSeparator, idSeperator);
	}

	public long dbGetListEx(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator) {
		return connection.dbGetListEx(aqlQuery, result, colSeparator, lineSeparator, idSeperator);
	}

	public long dbGetLong(String aqlQuery) {
		return connection.dbGetLong(aqlQuery);
	}

	public long dbGetPk(String tableName, String whereClause) {
		return connection.dbGetPk(tableName, whereClause);
	}

	public long dbGetString(String query, AMString result, String colSeparator, String lineSeparator) {
		return connection.dbGetString(query, result, colSeparator, lineSeparator);
	}

	public long dbGetStringEx(String query, AMString result, String colSeparator, String lineSeparator) {
		return connection.dbGetStringEx(query, result, colSeparator, lineSeparator);
	}

	public long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {
		return connection.deleteLink(srcRecHandle, linkName, srcDstHandle);
	}

	public long deleteRecord(AMHandle recordHandle) {
		return connection.deleteRecord(recordHandle);
	}

	public long duplicateRecord(AMHandle recordHandle, long insert) {
		return connection.duplicateRecord(recordHandle, insert);
	}

	public long enumValList(String enumName, AMString value, long caseSensitive, String lineSeparator) {
		return connection.enumValList(enumName, value, caseSensitive, lineSeparator);
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
		AMConnectionWrapper rhs = (AMConnectionWrapper) obj;
		return new EqualsBuilder().append(connection, rhs.connection).append(closed, rhs.closed)
				.append(enlisted, rhs.enlisted).append(key, rhs.key).isEquals();
	}

	public AMHandle executeActionById(long actionId, String tableName, long recordId) {
		return connection.executeActionById(actionId, tableName, recordId);
	}

	public AMHandle executeActionByName(String sqlName, String tableName, long recordId) {
		return connection.executeActionByName(sqlName, tableName, recordId);
	}

	public long exportDocument(long documentId, String fileName) {
		return connection.exportDocument(documentId, fileName);
	}

	public long flushTransaction() {
		return connection.flushTransaction();
	}

	public long formatCurrency(double amount, String currency, AMString result) {
		return connection.formatCurrency(amount, currency, result);
	}

	public long formatLong(long number, String format, AMString result) {
		return connection.formatLong(number, format, result);
	}

	public long getComputeString(String tableName, long recordId, String template, AMString result) {
		return connection.getComputeString(tableName, recordId, template, result);
	}

	public AMHandle getField(AMHandle objHandle, long position) {
		return connection.getField(objHandle, position);
	}

	public long getFieldCount(AMHandle objHandle) {
		return connection.getFieldCount(objHandle);
	}

	public long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos) {
		return connection.getFieldDateOnlyValue(recHandle, fieldPos);
	}

	public long getFieldDateValue(AMHandle recHandle, long fieldPos) {
		return connection.getFieldDateValue(recHandle, fieldPos);
	}

	public long getFieldDescription(AMHandle fieldHandle, AMString target) {
		return connection.getFieldDescription(fieldHandle, target);
	}

	public double getFieldDoubleValue(AMHandle objHandle, long fieldPos) {
		return connection.getFieldDoubleValue(objHandle, fieldPos);
	}

	public long getFieldFormat(AMHandle fldHandle, AMString target) {
		return connection.getFieldFormat(fldHandle, target);
	}

	public long getFieldFormatFromName(String tableName, String fieldName, AMString result) {
		return connection.getFieldFormatFromName(tableName, fieldName, result);
	}

	public AMHandle getFieldFromName(AMHandle objHandle, String fielddName) {
		return connection.getFieldFromName(objHandle, fielddName);
	}

	public long getFieldLabel(AMHandle fldHandle, AMString result) {
		return connection.getFieldLabel(fldHandle, result);
	}

	public long getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel) {
		return connection.getFieldLabelFromName(tableName, fieldName, fieldLabel);
	}

	public long getFieldLongValue(AMHandle objHandle, long fieldPosition) {
		return connection.getFieldLongValue(objHandle, fieldPosition);
	}

	public long getFieldName(AMHandle objHandle, long fieldPositon, AMString fieldName) {
		return connection.getFieldName(objHandle, fieldPositon, fieldName);
	}

	public long getFieldSize(AMHandle fldHandle) {
		return connection.getFieldSize(fldHandle);
	}

	public long getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName) {
		return connection.getFieldSqlName(fldHandle, fieldSQLName);
	}

	public long getFieldStrValue(AMHandle qryHandle, long position, AMString target) {
		return connection.getFieldStrValue(qryHandle, position, target);
	}

	public long getFieldType(AMHandle fldHandle) {
		return connection.getFieldType(fldHandle);
	}

	public long getFieldUserType(AMHandle fldHandle) {
		return connection.getFieldUserType(fldHandle);
	}

	public Object getKey() {
		return key;
	}

	public AMHandle getRecordFromMainId(String tableName, long lId) {
		return connection.getRecordFromMainId(tableName, lId);
	}

	public AMHandle getRecordHandle(AMHandle qryHandle) {
		return connection.getRecordHandle(qryHandle);
	}

	public long getRecordId(AMHandle recHandle) {
		return connection.getRecordId(recHandle);
	}

	public AMHandle getRelDstField(AMHandle fldHandle) {
		return connection.getRelDstField(fldHandle);
	}

	public AMHandle getRelSrcField(AMHandle fldHandle) {
		return connection.getRelSrcField(fldHandle);
	}

	public AMHandle getRelTable(AMHandle fldHandle) {
		return connection.getRelTable(fldHandle);
	}

	public AMHandle getReverseLink(AMHandle fldHandle) {
		return connection.getReverseLink(fldHandle);
	}

	public long getSelfFromMainId(String tableName, long recordId, AMString recordDescription) {
		return connection.getSelfFromMainId(tableName, recordId, recordDescription);
	}

	public long getVersion(AMString amVersion) {
		return connection.getVersion(amVersion);
	}

	public int hashCode() {
		return new HashCodeBuilder(93, 3).append(connection).append(closed).append(enlisted).append(key).toHashCode();
	}

	public long importDocument(long docId, String tableName, String fileName, String category, String designation) {
		return connection.importDocument(docId, tableName, fileName, category, designation);
	}

	public long insertRecord(AMHandle recHandle) {
		return connection.insertRecord(recHandle);
	}

	public long isConnected() {
		return connection.isConnected();
	}

	public long lastError() {
		return connection.lastError();
	}

	public long lastErrorMsg(AMString errorMessage) {
		return connection.lastErrorMsg(errorMessage);
	}

	public long listToString(AMString target, String source, String colSep, String lineSep, String idSep) {
		return connection.listToString(target, source, colSep, lineSep, idSep);
	}

	public long loginId() {
		return connection.loginId();
	}

	public long loginName(AMString loginName) {
		return connection.loginName(loginName);
	}

	public AMHandle openConnection(String database, String username, String password) throws AMConnectionException {
		return connection.openConnection(database, username, password);
	}

	public long purgeRecord(AMHandle recHandle) {
		return connection.purgeRecord(recHandle);
	}

	public AMHandle queryCreate() {
		return connection.queryCreate();
	}

	public long queryExec(AMHandle queryHandle, String aqlQuery) {
		return connection.queryExec(queryHandle, aqlQuery);
	}

	public long queryGet(AMHandle qryHandle, String aqlQuery) {
		return connection.queryGet(qryHandle, aqlQuery);
	}

	public long queryNext(AMHandle queryHandle) {
		return connection.queryNext(queryHandle);
	}

	public long querySetAddMainField(AMHandle qryHandle, long addMainField) {
		return connection.querySetAddMainField(qryHandle, addMainField);
	}

	public long querySetFullMemo(AMHandle qryHandle, long fullMemo) {
		return connection.querySetFullMemo(qryHandle, fullMemo);
	}

	public AMHandle queryStartTable(AMHandle qryHandle) {
		return connection.queryStartTable(qryHandle);
	}

	public long queryStop(AMHandle qryHandle) {
		return connection.queryStop(qryHandle);
	}

	public long refreshAllCaches() {
		return connection.refreshAllCaches();
	}

	public long releaseHandle(AMHandle objHandle) {
		return connection.releaseHandle(objHandle);
	}

	@Override
	public void resetCallTimeOut() {
		connection.resetCallTimeOut();
	}

	public long rollBack() {
		if (enlisted) {
			throw new IllegalStateException(NLS.MESSAGES.getString("connection.enlisted.rollback"));
		}
		return connection.rollBack();
	}

	@Override
	public void setCallTimeOutInMs(long timeOutInMs) {
		connection.setCallTimeOutInMs(timeOutInMs);
	}

	public long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue) {
		return connection.setFieldDateOnlyValue(recHandle, fieldName, dateOnlyValue);
	}

	public long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue) {
		return connection.setFieldDateValue(recHandle, fieldName, dateTimeValue);
	}

	public long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value) {
		return connection.setFieldDoubleValue(recHandle, fieldName, value);
	}

	public long setFieldLongValue(AMHandle recHandle, String fieldName, long value) {
		return connection.setFieldLongValue(recHandle, fieldName, value);
	}

	public long setFieldStrValue(AMHandle recHandle, String fieldName, String value) {
		return connection.setFieldStrValue(recHandle, fieldName, value);
	}

	@Override
	public void setProcessingFlag(int flag) {
		connection.setProcessingFlag(flag);
	}

	public long sqlTextConst(String aqlQuery, AMString target) {
		return connection.sqlTextConst(aqlQuery, target);
	}

	public long startTransaction() {
		return connection.startTransaction();
	}

	public String toString() {
		return new ToStringBuilder(this).append("connection", connection).append("closed", closed)
				.append("enlisted", enlisted).append("key", key).toString();
	}

	public long updateRecord(AMHandle recHandle) {
		return connection.updateRecord(recHandle);
	}

}
