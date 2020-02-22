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
package am.api;

import am.api.exception.AMConnectionException;
import am.api.model.AMDate;
import am.api.model.AMString;

public interface AMConnection extends AutoCloseable {

	/**
	 * Connection should not be reused, usually flagged after a non-recoverable
	 * error due to API instability
	 */
	final static int FLAG_NO_REUSE = 1;

	long clearLastError();

	void close();

	/**
	 * This function commits all the modifications made to the database associated
	 * with the current connection.
	 * 
	 * @return 0 for Normal execution, else error code.
	 */

	long commit();

	/**
	 * This function returns the current database connection name.
	 * 
	 * @param connectionName an output parameter
	 * 
	 * @return status
	 */

	long connectionName(AMString connectionName);

	/**
	 * This function converts a Basic format date ("Date" type) to a Unix format
	 * date ("Long" type). This function does not work from external tools because
	 * the two types are equivalent.
	 * 
	 * @param tmTime Unix time to convert
	 * 
	 * @return status
	 */

	long convertDateBasicToUnix(long tmTime);

	/**
	 * This function converts an international format date ("Date" type) to a Unix
	 * format date ("Long" type).
	 * 
	 * @param dateAsString
	 * 
	 * @return Unix date
	 */

	long convertDateIntlToUnix(String dateAsString);

	/**
	 * Converts a date to a string (as displayed in the Windows Control Panel) to a
	 * Unix "Long".
	 * 
	 * @param dateAsString format (yyyy-mm-dd hh:mm:ss).
	 * 
	 * @return Unix date
	 */

	long convertDateStringToUnix(String dateAsString);

	/**
	 * This function converts a Unix format date ("Long" type) to a Basic format
	 * date ("Date" type). This function does not work from external tools because
	 * the two types are equivalent.
	 */

	long convertDateUnixToBasic(long dateAsUnix);

	/**
	 * This function converts a Unix format date ("Long" type) to an international
	 * format date (yyyy-mm-dd hh:mm:ss).
	 * 
	 * @param unixDate      Unix date
	 * @param dateAsIntlStr converted date in international string format
	 *                      (yyyy-mm-dd hh:mm:ss) (OUTPUT)
	 * 
	 * @return status
	 */

	long convertDateUnixToIntl(long unixDate, AMString dateAsIntlStr);

	/**
	 * Converts a "Long" Unix format date to a string format date (as displayed in
	 * the Windows Control Panel).
	 * 
	 * @param dateAsUnix Unix date
	 * 
	 * @param dateAsStr  converted date in in string format as displayed in the
	 *                   windows control panel (OUTPUT)
	 * 
	 * @return status
	 */

	long convertDateUnixToString(long dateAsUnix, AMString dateAsStr);

	/**
	 * This function converts a double to a string
	 * 
	 * @param dSrc   double to convert
	 * @param target string value object (Output)
	 * 
	 * @return status
	 */

	long convertDoubleToString(double dSrc, AMString dblAsString);

	/**
	 * This function converts a monetary value to a character string. The string is
	 * formatted according to the regional options (currency) defined in the Windows
	 * Control Panel.
	 * 
	 * @param dMonetarySrc double to convert
	 * @param target       string value object formatted to the regional options
	 *                     (currency) defined in the Windows Control Panel (Output)
	 * 
	 * @return status
	 */

	long convertMonetaryToString(double dMonetarySrc, AMString dblAsString);

	/**
	 * This function converts a character string (in a format corresponding to the
	 * one defined in the Windows Control Panel) to a double precision number.
	 * 
	 * @param dblAsString
	 * 
	 * @return double value of string
	 */

	double convertStringToDouble(String dblAsString);

	/**
	 * This function converts a character string (in a format corresponding to the
	 * one defined in the Windows Control Panel) to a monetary value.
	 * 
	 * @param monteraryAsString
	 * 
	 * @return double value of string
	 */

	double convertStringToMonetary(String monetaryAsString);

	/**
	 * This function modifies a link of a record and makes it point to a new record
	 * in the target table. It therefore creates a link between two records.
	 * 
	 * @param srcRecHandle This parameter contains the handle of the record
	 *                     containing the link to be modified.
	 * @param linkName     This parameter contains the SQL name of the link to be
	 *                     modified.
	 * @param srcDstHandle This parameter contains a handle of the target record of
	 *                     the link.
	 * 
	 * @return status 0: Normal execution.
	 */

	long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle);

	/**
	 * This function creates an empty record in a table taking the default values
	 * into account. This new record does not exist in the database until it has
	 * been inserted.
	 * 
	 * @param tblName contains the SQL name of the table in which you want to create
	 *                the record.
	 * 
	 * @return recordHandle
	 */

	AMHandle createRecord(String tblName);

	/**
	 * This function returns the current date on the client workstation.
	 * 
	 * Note: The value returned by this function is expressed as GMT+0 and does not
	 * take daylight savings into account
	 * 
	 * @return current date in Unix format
	 */

	long currentDate();

	/**
	 * This function returns the current date on the server.
	 * 
	 * @return current date in Unix format on the server
	 */

	long currentServerDate();

	/**
	 * This function calculates a new date according to a start date to which a real
	 * duration is added.
	 * 
	 * @param startAsUnixDate This parameter contains the date to which the duration
	 *                        is added
	 * 
	 * @param duration        This parameter contains the duration, expressed in
	 *                        seconds, to be added to the date
	 * 
	 * @return
	 */

	long dateAdd(long startAsUnixDate, long duration);

	/**
	 * This function calculates a new date according to a start date to which a
	 * logical duration is added (1 month contains 30 days).
	 * 
	 * @param startAsUnixDate This parameter contains the date to which the duration
	 *                        is added.
	 * 
	 * @param duration        This parameter contains the duration, expressed in
	 *                        seconds, to be added to the date
	 * 
	 * @return
	 */

	long dateAddLogical(long startAsUnixDate, long duration);

	/**
	 * This function calculates in the seconds the duration (or time span) between
	 * two dates.
	 * 
	 * @param endAsUnixDate   This parameter contains the end date of the period for
	 *                        which the calculation is carried out.
	 * 
	 * @param startAsUnixDate This parameter contains the start date of the period
	 *                        for which the calculation is carried out.
	 * 
	 * @return time span in seconds
	 */

	long dateDiff(long endAsUnixDate, long startAsUnixDate);

	/**
	 * This function enables you to execute an AQL query on the database
	 * 
	 * @param aqlQuery This parameter contains the AQL query to execute.
	 * 
	 * @return 0: Normal execution. Other than zero: Error code.
	 */

	long dbExecAql(String aqlQuery);

	/**
	 * This function returns the result, in date format, of the AQL query. If the
	 * query does not return a result, the value 0 is returned without triggering an
	 * error.
	 * 
	 * @param aqlQuery This parameter contains the full AQL query whose result you
	 *                 want to recover.
	 * 
	 * @return status 0 for normal execution, else retrieve error using lastError()
	 */

	long dbGetDate(String aqlQuery);

	/**
	 * This function returns the result (as a double-precision number), of the AQL
	 * query. If the query does not return a result, the value 0 is returned without
	 * triggering an error.
	 * 
	 * @param aqlQuery This parameter contains the full AQL query whose result you
	 *                 want to recover.
	 * 
	 * @return status 0 for normal execution, else retrieve error using lastError()
	 */

	double dbGetDouble(String aqlQuery);

	/**
	 * This function returns the result of an AQL query in a numbered list. Unlike
	 * the dbGetList and dbGetListEx functions, this function is used to define the
	 * maximum number of elements selected by the AQL query and indicates what
	 * should be done if data is truncated.
	 * 
	 * @param aqlQuery      This parameter contains the AQL query that is to be
	 *                      executed.
	 * 
	 * @param result        resulting string that needs to be parsed out
	 * 
	 * @param colSep        This parameter contains the character used as the column
	 *                      separator in the result returned by the function.
	 * 
	 * @param lineSeparator This parameter contains the character used as the line
	 *                      separator in the result returned by the function.F
	 * 
	 * @param idSeperator   This parameter contains the character used as the
	 *                      identifier separator in the result returned by the
	 *                      function.
	 * 
	 * @param maxSize       This parameter contains the maximum number of elements
	 *                      returned by the AQL query before truncation occurs.F
	 * 
	 * @param errorType     If 1 the function returns an error message. If the
	 *                      parameter is set to 2, the function returns a warning
	 *                      message. If the parameter is set to 4, the function
	 *                      returns an informational message.
	 * 
	 */

	long dbGetLimitedList(String aqlQuery, AMString result, String colSeparator, String lineSeparator,
			String idSeperator, long maxSize, long errorType);

	/**
	 * This function returns, as a list, the result of an AQL query. The number of
	 * elements selected by the AQL query is limited to 99.
	 * 
	 * @param aqlQuery      This parameter contains the AQL query that is to be
	 *                      executed.
	 * 
	 * @param result        resulting string that needs to be parsed out
	 * 
	 * @param colSep        This parameter contains the character used as the column
	 *                      separator in the result returned by the function.
	 * 
	 * @param lineSeparator This parameter contains the character used as the line
	 *                      separator in the result returned by the function.F
	 * 
	 * @param idSeperator   This parameter contains the character used as the
	 *                      identifier separator in the result returned by the
	 *                      function.
	 * 
	 */

	long dbGetList(String aqlQuery, AMString result, String colSeparator, String lineSeparator, String idSeperator);

	/**
	 * This function returns, as a list, the result of an AQL query. Unlike the
	 * dbGetList function, this function is not limited in the number of elements
	 * selected by the AQL query.
	 * 
	 * @param aqlQuery      This parameter contains the AQL query you want to
	 *                      execute.
	 * @param result
	 * @param colSeparator  This parameter contains the character used as the column
	 *                      separator in the result returned by the function.
	 * @param lineSeparator This parameter contains the character used as the line
	 *                      separator in the result returned by the function.
	 * @param idSeperator   This parameter contains the character used as the
	 *                      identifier separator in the result returned by the
	 *                      function.
	 * @return
	 */

	long dbGetListEx(String aqlQuery, AMString result, String colSeparator, String lineSeparator, String idSeperator);

	/**
	 * This function returns the result of an AQL query. If the query does not
	 * return a result, the value 0 is returned without triggering an error.
	 * 
	 * @param aqlQuery This parameter contains the full AQL query whose result you
	 *                 want to recover.
	 * @return status 0 for successful, else error, you must call the lastError() to
	 *         retrieve error
	 */

	long dbGetLong(String aqlQuery);

	/**
	 * This function returns the primary key of a table according to the WHERE
	 * clause in an AQL query. If the query does not return a result, the value 0 is
	 * returned without triggering an error
	 * 
	 * @param tableName
	 * @param whereClause
	 * @return
	 */

	long dbGetPk(String tableName, String whereClause);

	/**
	 * This function returns the result of an AQL query as a formatted string. The
	 * number of elements selected by the AQL query is limited to 99. It returns a
	 * maximum of 254 characters per field (if there are more, the string is
	 * truncated).
	 * 
	 * @param query
	 * @param result
	 * @param colSeparator
	 * @param lineSeparator
	 * @return status - 0 for success, else peruse the error by calling lastError()
	 */

	long dbGetString(String query, AMString result, String colSeparator, String lineSeparator);

	/**
	 * This function returns, as a character string , the result of an AQL query.
	 * The difference with the AmDbGetString function is that this function is not
	 * limited in the number of elements selected by the AQL query.
	 * 
	 * @param query
	 * @param result
	 * @param colSeparator
	 * @param lineSeparator
	 * @return status - 0 for success, else peruse the rror by calling lastError()
	 */

	long dbGetStringEx(String query, AMString result, String colSeparator, String lineSeparator);

	/**
	 * This function deletes a links of a record.
	 * 
	 * @param srcRecHandle This parameter contains the handle of the record
	 *                     containing the link to be deleted.
	 * @param linkName     This parameter contains the SQL name of the link to be
	 *                     deleted.
	 * @param srcDstHandle This parameter contains a handle of the target record of
	 *                     the link to be deleted.
	 * @return status - 0 for normal execution, other than 0, error.
	 */

	long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle);

	/**
	 * This function deletes a record in the database.
	 * 
	 * @param recordHandle This parameter contains a handle of the record you want
	 *                     to delete.
	 * @return status - 0 for normal execution, else error.
	 */

	long deleteRecord(AMHandle recordHandle);

	/**
	 * This function enables you to duplicate a record.
	 * 
	 * @param recordHandle This parameter contains the handle of the record to
	 *                     duplicate.
	 * @param insert       This parameter enables you to specify whether you want to
	 *                     insert the duplicated record immediately (=1) or not
	 *                     (=0).
	 * @return status 0 for normal execution, else error
	 */

	long duplicateRecord(AMHandle recordHandle, long insert);

	/**
	 * This function returns a string containing all the values of a custom itemized
	 * list. The different values are sorted alphabetically and are delimited by the
	 * separator indicated in the strLineSep parameter. If an itemized list value
	 * contains the character used as the separator or a "\", the "\" prefix is
	 * used.
	 * 
	 * @param enumName      This parameter contains the SQL name of the itemized
	 *                      list for which you want to recover the values.
	 * @param value
	 * @param caseSensitive This parameter enables you to specify whether the sort
	 *                      is case sensitive (=1) or not (=0).
	 * @param lineSeparator This parameter contains the character used to delimit
	 *                      the itemized-list values.
	 * @return status 0 for normal execution, else error
	 */

	long enumValList(String enumName, AMString value, long caseSensitive, String lineSeparator);

	/**
	 * This function executes an action as identified by its identifier.
	 * 
	 * @param actionId  This parameter contains the identifier of the action to be
	 *                  executed.
	 * @param tableName In the case of a contextual action, this parameter contains
	 *                  the SQL name of the table on which the action is executed.
	 *                  If this parameter is omitted, in the case of a contextual
	 *                  action, the function will fail. For non-contextual actions,
	 *                  this parameter is not interpreted and therefore optional.
	 * @param recordId  This parameter contains the identifier of a possible record
	 *                  concerned by the action. For non-contextual actions, this
	 *                  parameter is not interpreted and therefore optional.
	 * @return status - 0 for normal execution, else error
	 */

	AMHandle executeActionById(long actionId, String tableName, long recordId);

	/**
	 * 
	 * @param sqlName   This parameter contains the SQL name of the action to be
	 *                  executed.
	 * @param tableName In the case of a contextual action, this parameter contains
	 *                  the SQL name of the table on which the action is executed.
	 *                  If this parameter is omitted, in the case of a contextual
	 *                  action, the function will fail. For non-contextual actions,
	 *                  this parameter is not interpreted and therefore optional.
	 * @param recordId  This parameter contains the identifier of a possible record
	 *                  concerned by the action. For non-contextual actions, this
	 *                  parameter is not interpreted and therefore optional.
	 * @return status - 0 for normal execution, else error
	 */

	AMHandle executeActionByName(String sqlName, String tableName, long recordId);

	/**
	 * This function enables you to export a document attached to a record.
	 * 
	 * @param documentId This parameter contains the identifier of the document to
	 *                   export.
	 * @param fileName   This parameter contains the name of the document to export,
	 *                   as it is stored in the FileName field of the Documents
	 *                   table.
	 * @return status - 0 for normal execution, else error
	 */

	long exportDocument(long documentId, String fileName);

	/**
	 * This function purges the task lists of the agents (like after a database
	 * Commit operation).
	 */
	long flushTransaction();

	/**
	 * This function displays a monetary value in a given currency. The standard
	 * symbol of the currency is also displayed.
	 * 
	 * @param amount
	 * @param currency
	 * @param result
	 * @return
	 */

	long formatCurrency(double amount, String currency, AMString result);

	/**
	 * This function remplaces a token in a character string with the value
	 * contained in a Long type variable.
	 * 
	 * @param number
	 * @param format
	 * @param result
	 * @return
	 */

	long formatLong(long number, String format, AMString result);

	/**
	 * This function returns the description string of a given record according to a
	 * template.
	 * 
	 * @param tableName
	 * @param recordId
	 * @param template
	 * @param result
	 * @return
	 */
	long getComputeString(String tableName, long recordId, String template, AMString result);

	/**
	 * This function creates a field object from the handle of a query, a record or
	 * a table and returns the handle of the field object created.
	 * 
	 * @param objHandle This parameter contains a handle of a query, record, or
	 *                  table.
	 * @param position  This parameter contains the position of the field (its
	 *                  index) within the object.
	 * @return
	 */
	AMHandle getField(AMHandle objHandle, long position);

	/**
	 * This function returns the number of fields contained in the current object.
	 * 
	 * @param objHandle This parameter contains a handle of a query or record.
	 * @return status - 0 for success, else error
	 */
	long getFieldCount(AMHandle objHandle);

	/**
	 * This function returns the value of a field contained in the current object.
	 * This value is returned in the "Date" format (from an external tool, it is a
	 * Long). Unlike the AmGetFieldDateValue function, only the Date part is
	 * returned, the Time part is omitted.
	 * 
	 * @param objHandle This parameter contains a handle of a valid record, query or
	 *                  table.
	 * @param fieldPos  This parameter contains the number of the field within the
	 *                  current object.
	 * @return date only value in Unix format
	 */

	long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos);

	/**
	 * This function returns the value of a field contained in the current object.
	 * This value is returned in "Date" format (from external tools, it is a Long).
	 * 
	 * @param objHandle This parameter contains a handle of a valid record, query or
	 *                  table.
	 * @param fieldPos  This parameter contains the number of the field within the
	 *                  current object.
	 * @return date only value in Unix format
	 */

	long getFieldDateValue(AMHandle recHandle, long fieldPos);

	/**
	 * This function returns, as a character string ("String" format), the long
	 * description of a field identified by a handle.
	 * 
	 * @param fieldHandle This parameter contains a valid handle of the field whose
	 *                    long description you want to know.
	 * @param target      value
	 * @return status - 0 for success, else error
	 */

	long getFieldDescription(AMHandle fieldHandle, AMString target);

	/**
	 * This function returns the value of a field contained in the current object.
	 * This value is returned in "Double" format.
	 * 
	 * @param objHandle This parameter contains a handle of a query or record.
	 * @param fieldPos  This parameter contains the number of the field inside the
	 *                  current object.
	 * 
	 * @return status - 0 for success, else error
	 */

	double getFieldDoubleValue(AMHandle objHandle, long fieldPos);

	/**
	 * This function is useful when the value of the "UserType" of the field
	 * concerned (cf. "database.txt" file) is: System itemized list, Itemized list,
	 * Time span, Table, field name
	 * 
	 * @param fldHandle This parameter contains a valid handle of the field whose
	 *                  "UserType" you want to know.
	 * @param target
	 * @return status, 0 for success else error
	 */
	long getFieldFormat(AMHandle fldHandle, AMString target);

	/**
	 * This function returns the "UserType" format of a field, from its name.
	 * 
	 * @param tableName This parameter contains the SQL name of the table containing
	 *                  the field concerned by the operation.
	 * @param fieldName This parameter contains the SQL name of the field.
	 * @param result
	 * @return
	 */
	long getFieldFormatFromName(String tableName, String fieldName, AMString result);

	/**
	 * This function creates a field object based on its name and returns the handle
	 * of the field object created.
	 * 
	 * @param objHandle  This parameter contains a handle of a query, record, or
	 *                   table.
	 * @param fielddName This parameter contains the field name.
	 * @return handle of field object created
	 */

	AMHandle getFieldFromName(AMHandle objHandle, String fielddName);

	/**
	 * This function returns, as a character string ("String" format), the label of
	 * a field identified by a handle.
	 * 
	 * @param fldHandle This parameter contains a valid handle of the field whose
	 *                  label you want to know.
	 * @param result    value object
	 * 
	 * @return status, 0 for success else failure
	 */

	long getFieldLabel(AMHandle fldHandle, AMString result);

	/**
	 * This function returns the label of a field from its SQL name.
	 * 
	 * @param tableName  This parameter contains the SQL name of the table
	 *                   containing the field concerned by the operation.
	 * @param fieldName  This parameter contains the SQL name of the field.
	 * @param fieldLabel target value object
	 * @return status, 0 of success else failure
	 */
	long getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel);

	/**
	 * This function returns the value of a field contained in the current object.
	 * 
	 * @param objHandle     This parameter contains a handle of a query, record, or
	 *                      table.
	 * @param fieldPosition This parameter contains the number of the field within
	 *                      the current object. E.g., the value "0" indicates the
	 *                      first field.
	 * @return 0 if success, else failure
	 */
	long getFieldLongValue(AMHandle objHandle, long fieldPosition);

	/**
	 * This function returns the name of a field contained in the current object.
	 * 
	 * @param objHandle
	 * @param fieldPositon
	 * @param fieldName
	 * @return
	 */
	long getFieldName(AMHandle objHandle, long fieldPositon, AMString fieldName);

	/**
	 * This function returns the size of a field.
	 * 
	 * @param fldHandle
	 * @return This parameter contains a handle of the field whose size you want to
	 *         know.
	 */

	long getFieldSize(AMHandle fldHandle);

	/**
	 * This function returns, as a character string ("String" format), the SQL name
	 * of a field identified by a handle.
	 * 
	 * @param fldHandleThis parameter contains a valid handle of the field whose SQL
	 *                      name you want to know.
	 * @param fieldSQLName  target object
	 * @return 0 for success, else failure
	 */
	long getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName);

	/**
	 * This function returns the value of a field contained in the current object.
	 * This value is returned in string format.
	 * 
	 * @param qryHandle query handle
	 * @param position  position in the field list, starts at 0
	 * @param target    string value object (Output)
	 * 
	 * @return status, 0 for success else error
	 */

	long getFieldStrValue(AMHandle qryHandle, long position, AMString target);

	/**
	 * This function returns the type of a field.
	 * 
	 * @param fldHandle
	 * @return 0 for undefined, 1 for byte, 2 for short, 3 for long, 4 for float, 5
	 *         for double, and 6 for string
	 */
	long getFieldType(AMHandle fldHandle);

	/**
	 * This function returns the "UserType" of a field (cf. database.txt file)
	 * identified by a handle, in the form of a long integer. For a field, the valid
	 * return values are summarized below:
	 * 
	 * 0 Default, 1 Number, 2 Yes/No, 3 Money, 4 Date, 5 Date+Time, 7 System
	 * itemized list, 8 Custom itemized list, 10 Percentage, 11 Time span, 12 Table
	 * or field SQL name
	 * 
	 * for a link
	 * 
	 * 0 Normal, 1 Comment, 2 Image, 3 History, 4 Feature value
	 * 
	 * @param fldHandle
	 * @return
	 */
	long getFieldUserType(AMHandle fldHandle);

	/**
	 * This function returns the ID number of a record identified by a value of the
	 * primary key of the table containing this record.
	 * 
	 * @param tableName
	 * @param lId
	 * @return recordHandle
	 */

	AMHandle getRecordFromMainId(String tableName, long lId);

	/**
	 * This function returns the handle of a record that is the current result of a
	 * query identified by its handle. This record can be used to write in the
	 * database. This function only works if the query contains the primary key of
	 * the record.
	 * 
	 * @param qryHandle This parameter contains a valid handle of a query object.
	 * @return handle of record
	 */

	AMHandle getRecordHandle(AMHandle qryHandle);

	/**
	 * This function returns the ID number of a record identified by its handle. In
	 * the case of a record being inserted, this value is 0. Do not release the
	 * query handle that was used to instantiate this record handle, otherwise an
	 * invalid handle will be returned.
	 * 
	 * @param recHandle This parameter contains a valid handle of the record whose
	 *                  ID number you want to know.
	 * 
	 * @return
	 */
	long getRecordId(AMHandle recHandle);

	/**
	 * This function returns a handle on the target field of a link.
	 * 
	 * @param fldHandle his parameter contains a valid handle on the link concerned
	 *                  by the operation.
	 * @return
	 */
	AMHandle getRelDstField(AMHandle fldHandle);

	/**
	 * This function returns a handle on the source field of a link.
	 * 
	 * @param fldHandle This parameter contains a valid handle on the link concerned
	 *                  by the operation.
	 * @return
	 */
	AMHandle getRelSrcField(AMHandle fldHandle);

	/**
	 * This function returns a handle on the relation table of an N-N link.
	 * 
	 * @param fldHandle
	 * @return in case of error, this function returns a non-valid handle (zero)
	 */
	AMHandle getRelTable(AMHandle fldHandle);

	/**
	 * This function returns the handle of the reverse link specified by the handle
	 * contained in the fldHandle parameter.
	 * 
	 * @param fldHandle This parameter contains a handle of the link whose reverse
	 *                  link you want to know.
	 * @return
	 */
	AMHandle getReverseLink(AMHandle fldHandle);

	/**
	 * 
	 * @param tableName
	 * @param recordId
	 * @param recordDescription
	 * @return
	 */
	long getSelfFromMainId(String tableName, long recordId, AMString recordDescription);

	/**
	 * This function returns the build number of Asset Manager in the form of a
	 * character string.
	 * 
	 * @param amVersion
	 * @return
	 */
	long getVersion(AMString amVersion);

	/**
	 * This function creates and imports a document from a file.
	 * 
	 * @param docId       This parameter contains the value that will be stored in
	 *                    the lDocObjId field of the amDocument table.
	 * @param tableName   This parameter contains the value that will be stored in
	 *                    the DocObjTable field of the amDocument table. In
	 *                    practice, it is the SQL name of the table containing the
	 *                    record to which the document is attached.
	 * @param fileName    This parameter contains the name of the file to import.
	 * @param category    This parameter contains the category of the document, as
	 *                    is appears in AssetCenter.
	 * @param designation This parameter contains the name of the document, as it
	 *                    appears in AssetManager.
	 * @return
	 */
	long importDocument(long docId, String tableName, String fileName, String category, String designation);

	/**
	 * This function inserts a record previously created in the database. Only those
	 * records created using the createRecord function can be inserted in the
	 * database. Records accessed using a query cannot be inserted.
	 * 
	 * @param recHandle This parameter contains a handle of the record you want to
	 *                  insert in the database.
	 * @return 0 for success, else failure
	 */

	long insertRecord(AMHandle recHandle);

	/**
	 * This function tests whether the current connection is valid.
	 * 
	 * @param connHandle
	 * @return 0 if connected, else not connected
	 */
	long isConnected();

	/**
	 * This function returns the last error code generated by the last function
	 * executed in the context of the corresponding connection.
	 * 
	 * @return error code
	 */
	long lastError();

	/**
	 * This function returns the last error message occurred in the current
	 * connection.
	 * 
	 * @param errorMessage
	 * @return
	 */
	long lastErrorMsg(AMString errorMessage);

	/**
	 * This function converts the result of a character string obtained via the
	 * getList function to a character string that can be displayed in the same way
	 * as the getString function.
	 * 
	 * @param target
	 * @param source  This parameter contains the character string to be converted.
	 * @param colSep  This parameter contains the character used as column separator
	 *                in the string to be converted.
	 * @param lineSep This parameter contains the character used as line separator
	 *                in the string to be converted.
	 * @param idSep   This parameter contains the character used as identifier
	 *                separator in the string to be converted.
	 * @return
	 */
	long listToString(AMString target, String source, String colSep, String lineSep, String idSep);

	/**
	 * This function returns the identifier of the connected user.
	 * 
	 * @return
	 */
	long loginId();

	/**
	 * This function returns the login name of the connected user.
	 * 
	 * @param loginName
	 * @return
	 */
	long loginName(AMString loginName);

	AMHandle openConnection(String database, String username, String password) throws AMConnectionException;

	/**
	 * This function destroys a record.
	 * 
	 * @param recHandle
	 * @return
	 */
	long purgeRecord(AMHandle recHandle);

	/**
	 * This function creates a query object in the current connection. This object
	 * can then be used to send AQL statements to the database server.
	 * 
	 * @return
	 */
	AMHandle queryCreate();

	/**
	 * This function executes an AQL query. It returns the first result of the
	 * query. The next result can be obtained via the AmQueryNext function. When the
	 * query sent by this function returns a "Memo" type field the result is limited
	 * to 255 characters.
	 * 
	 * @param queryHandle This parameter contains a valid handle of the query object
	 *                    to which the AQL statements are sent.
	 * @param aqlQuery    This parameter contains the body of the AQL query as a
	 *                    string.
	 * @return 0 for success, else returns the error code
	 */

	long queryExec(AMHandle queryHandle, String aqlQuery);

	/**
	 * This function executes an AQL query without a cursor (one single result). It
	 * only returns one single line of results.
	 * 
	 * @param qryHandle This parameter contains a valid handle of the query object
	 *                  to which the AQL statements are sent.
	 * @param aqlQuery  This parameter contains the body of the AQL query as a
	 *                  string.
	 * @return
	 */

	long queryGet(AMHandle qryHandle, String aqlQuery);

	/**
	 * This function returns the result of a query executed beforehand using the
	 * queryExec function.
	 * 
	 * @param queryHandle
	 * @return
	 */

	long queryNext(AMHandle queryHandle);

	/**
	 * This function enables you to send a query in a mode where the main field of
	 * the table is automatically added to the list of fields to be returned. This
	 * type of query never returns a null identifier record.
	 * 
	 * @param qryHandle    This parameter contains a valid handle on a query object.
	 * @param addMainField (-1) main field added, (0) main field not added
	 * @return 0 for normal execution, else the error code
	 */

	long querySetAddMainField(AMHandle qryHandle, long addMainField);

	/**
	 * By default, when executing the queryExec function, the query truncates Memo
	 * type fields to 254 characters. This function sends the query in a mode where
	 * Memo fields are recovered in full.
	 * 
	 * @param qryHandle This parameter contains a valid handle on a query object.
	 * @param fullMemo  (-1) memo field in full, (0) memo field truncated to 254
	 *                  characters.
	 * @return 0 for normal execution, else error code
	 */
	long querySetFullMemo(AMHandle qryHandle, long fullMemo);

	/**
	 * This function returns a handle of the table concerned by a query identified
	 * by its handle.
	 * 
	 * @param qryHandle This parameter contains a valid handle of a query object.
	 * @return In case of error, this function returns a non-valid handle (0)
	 */
	AMHandle queryStartTable(AMHandle qryHandle);

	/**
	 * This function interrupts the execution of a query identified by its handle.
	 * This query must have been launched beforehand using the queryExec function.
	 * 
	 * @param qryHandle This parameter contains a valid handle of a query object. HP
	 * @return 0 for normal execution else error code.
	 */

	long queryStop(AMHandle qryHandle);

	/**
	 * This function refreshes the caches used in Asset Manager.
	 * 
	 * @return 0 for normal execute, else error code.
	 */
	long refreshAllCaches();

	/**
	 * This function frees the handle and sub-handles of an object.
	 * 
	 * @param objHandle This parameter contains a handle of the object concerned.
	 */
	long releaseHandle(AMHandle objHandle);

	/**
	 * Resets the call timeout to the default value as configured by the admins
	 */

	void resetCallTimeOut();

	/**
	 * This function cancels all modifications made before the declaration of the
	 * start of the transaction (performed via the startTransaction function).
	 * 
	 * @return 0 for normal execute, else error code.
	 */
	long rollBack();

	/**
	 * Sets the call timeout
	 */

	void setCallTimeOutInMs(long timeOutInMs);

	/**
	 * This function modifies a field in a record. This function does not update the
	 * database. The modification will be made when the record is updated or
	 * inserted, or when the transaction is committed.
	 * 
	 * @param recHandle     This parameter contains the handle of the record
	 *                      containing the field to be modified.
	 * @param fieldName     This parameter contains the SQL name of the field to be
	 *                      modified.
	 * @param dateOnlyValue This parameter contains the new value of the field in
	 *                      "Date" format only. Unlike the setFieldDateValue
	 *                      function, only the Date part is processed, the Time part
	 *                      is omitted.
	 * @return 0 for normal execute, else error code
	 */
	long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue);

	/**
	 * This function modifies a field in a record. This function does not update the
	 * database. The modification will be made when the record is updated or
	 * inserted, or when the transaction is committed.
	 * 
	 * @param recHandle     This parameter contains the handle of the record
	 *                      containing the field to be modified.
	 * @param fieldName     This parameter contains the SQL name of the field to be
	 *                      modified.
	 * @param dateTimeValue This parameter contains the new value of the field in
	 *                      "Date" format.
	 * @return 0 for normal execution, else error code
	 */
	long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue);

	/**
	 * This function modifies a field in a record. This function does not update the
	 * database.
	 * 
	 * @param recHandle This parameter contains the handle of the record containing
	 *                  the field to be modified.
	 * @param fieldName This parameter contains the SQL name of the field to be
	 *                  modified.
	 * @param value     This parameter contains the new value of the field in
	 *                  "Double" format.
	 * @return 0 for normal execution, else error code
	 */
	long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value);

	/**
	 * This function modifies a field in a record. This function does not update the
	 * database.To modify the value of a date, time or date+time date you must
	 * express the new value in terms of seconds elapsed since 01/01/1970 at
	 * 00:00:00.
	 * 
	 * @param recHandle This parameter contains the handle of the record containing
	 *                  the field to be modified.
	 * @param fieldName This parameter contains the SQL name of the field to be
	 *                  modified.
	 * @param value     This parameter contains the new value of the field.
	 * @return 0 for normal execution, else error code
	 */
	long setFieldLongValue(AMHandle recHandle, String fieldName, long value);

	/**
	 * This function modifies a field in a record. This function does not update the
	 * database.
	 * 
	 * @param recHandle This parameter contains the handle of the record containing
	 *                  the field to be modified.
	 * @param fieldName This parameter contains the SQL name of the field to be
	 *                  modified.
	 * @param value     This parameter contains the new value of the field in
	 *                  "String" format.
	 * @return 0 for normal execution, else error code.
	 */
	long setFieldStrValue(AMHandle recHandle, String fieldName, String value);

	/**
	 * This function sets various operational flags which may be acted upon by the
	 * Connection Provider
	 */
	void setProcessingFlag(int flag);

	/**
	 * This function transforms a string to be used in a query. The following
	 * operations are performed on the string: n All single quotes (') are doubled,
	 * n Single quotes are added at the start and end of the string.
	 * 
	 * @param aqlQuery This parameter contains the character string to process.
	 * @param target
	 * 
	 * @return
	 */
	long sqlTextConst(String aqlQuery, AMString target);

	/**
	 * This function starts a new transaction with the database associated with the
	 * connection. The next "Commit" or "Rollback" statement will validate or cancel
	 * all the modifications made to the database.
	 * 
	 * @return 0 for normal execute else error code.
	 */

	long startTransaction();

	/**
	 * This function enables you to update a record.
	 * 
	 * @param recHandle
	 * @return 0 for normal execute else error code
	 */
	long updateRecord(AMHandle recHandle);

}
