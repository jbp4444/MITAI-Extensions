// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.collect.Lists;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


@DesignerComponent(version = ListOps.VERSION,
    description = "Provides list operations (sort, min, max) to MIT-AI/Thunkable apps",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject( external=true )
public final class ListOps extends AndroidNonvisibleComponent {
	public static final int VERSION = 1;

	// Constructor
	public ListOps( ComponentContainer container ) {
		super( container.$form() );
	}

	@SimpleFunction( description="Return max of a list" )
	public Object ListMax( YailList itemList ) {
		Object rtn;
		Object[] objList = itemList.toArray();

		if( objList[0] instanceof String ) {
			String maxval = "";
			for( String v : (String)objList ) {
				if( v > maxval ) {
					maxval = v;
				}
			}
			rtn = (Object)maxval;
		} else if( objList[0] instanceof int ) {
			int maxval = "";
			for( int v : (int)objList ) {
				if( v > maxval ) {
					maxval = v;
				}
			}
			rtn = (Object)maxval;
		}

		return( rtn );
	}

}


/**
 * Static methods to convert between CSV-formatted strings and YailLists.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public final class CsvUtil {

  private CsvUtil() {
  }

  public static YailList fromCsvTable(String csvString) throws Exception {
    CsvParser csvParser = new CsvParser(new StringReader(csvString));
    ArrayList<YailList> csvList = new ArrayList<YailList>();
    while (csvParser.hasNext()) {
      csvList.add(YailList.makeList(csvParser.next()));
    }
    csvParser.throwAnyProblem();
    return YailList.makeList(csvList);
  }

  public static YailList fromCsvRow(String csvString) throws Exception {
    CsvParser csvParser = new CsvParser(new StringReader(csvString));
    if (csvParser.hasNext()) {
      YailList row = YailList.makeList(csvParser.next());
      if (csvParser.hasNext()) {
        // more than one row is an error
        throw new IllegalArgumentException("CSV text has multiple rows. Expected just one row.");
      }
      csvParser.throwAnyProblem();
      return row;
    }
    throw new IllegalArgumentException("CSV text cannot be parsed as a row.");
  }

  // Requires: elements of csvRow are strings
  public static String toCsvRow(YailList csvRow) {
    StringBuilder csvStringBuilder = new StringBuilder();
    makeCsvRow(csvRow, csvStringBuilder);
    return csvStringBuilder.toString();
  }

  // Requires: elements of rows are strings
  // TODO(sharon): do we want to enforce any consistency constraints here, e.g.,
  // all rows have same number of elements?
  public static String toCsvTable(YailList csvList) {
    StringBuilder csvStringBuilder = new StringBuilder();
    for (Object rowObj : csvList.toArray()) {
      makeCsvRow((YailList) rowObj, csvStringBuilder);
      // http://tools.ietf.org/html/rfc4180 suggests that CSV lines should be
      // terminated
      // by CRLF, hence the \r\n.
      csvStringBuilder.append("\r\n");
    }
    return csvStringBuilder.toString();
  }

  private static void makeCsvRow(YailList row, StringBuilder csvStringBuilder) {
    String fieldDelim = "";
    for (Object fieldObj : row.toArray()) {
      String field = fieldObj.toString();
      field = field.replaceAll("\"", "\"\"");
      csvStringBuilder.append(fieldDelim).append("\"").append(field).append("\"");
      fieldDelim = ",";
    }
  }

}
