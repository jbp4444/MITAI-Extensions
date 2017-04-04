// Copyright 2017, John Pormann, Duke University
//
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.YailList;

import android.os.Environment;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Number;
import java.lang.Integer;
import java.lang.Float;

@DesignerComponent(version = ListOps.VERSION,
    description = "Provides list operations (sort, min, max) to MIT-AI/Thunkable apps",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@SimpleObject( external=true )
public final class ListOps extends AndroidNonvisibleComponent {
	public static final int VERSION = 1;

	public static int last_index = -1;

	// Constructor
	public ListOps( ComponentContainer container ) {
		super( container.$form() );
	}

	@SimpleFunction( description = "The list-index of the last operation" )
	public int LastIndex() {
		return last_index;
	}

	@SimpleFunction( description="Return max of a list of strings" )
	public String StringListMax( YailList itemList ) {
		String[] objList = itemList.toStringArray();
		String maxval;

		maxval = objList[0];
		last_index = 0;
		for( int i=1; i<objList.length; i++ ) {
			String v = objList[i];
			if( v.compareTo(maxval) > 0 ) {
				last_index = i;
				maxval = v;
			}
		}

		return( maxval );
	}

	@SimpleFunction( description="Return min of a list of strings" )
	public String StringListMin( YailList itemList ) {
		String[] objList = itemList.toStringArray();
		String minval;

		minval = objList[0];
		last_index = 0;
		for( int i=1; i<objList.length; i++ ) {
			String v = objList[i];
			if( v.compareTo(minval) < 0 ) {
				last_index = i;
				minval = v;
			}
		}

		return( minval );
	}

	@SimpleFunction( description="Return a sorted list of strings" )
	public List<String> MySimpleSort( YailList itemList ) {
		String[] objList = itemList.toStringArray();
		int n = objList.length;

		// flag that the last_index is no longer valid
		last_index = -1;

		// simple insertion sort
		for( int i=1; i<n; i++ ) {
			String x = objList[i];
			int j = i - 1;
			while( (j>=0) && (objList[j].compareTo(x)>0) ) {
				objList[j+1] = objList[j];
				j = j - 1;
			}
			objList[j+1] = x;
		}

		return( new ArrayList<String>(Arrays.asList(objList)) );
	}

}
