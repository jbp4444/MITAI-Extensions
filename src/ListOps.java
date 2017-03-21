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

import java.lang.Number;
import java.lang.Integer;

@DesignerComponent(version = ListOps.VERSION,
    description = "Provides list operations (sort, min, max) to MIT-AI/Thunkable apps",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
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
			for( String v : (String[])objList ) {
				if( v.compareTo(maxval) > 0 ) {
					maxval = v;
				}
			}
			rtn = (Object)maxval;
		} else if( objList[0] instanceof int ) {
			int maxval = Integer.MIN_VALUE;
			for( int v : (int[])objList ) {
				if( v > maxval ) {
					maxval = v;
				}
			}
			rtn = (Object)maxval;
		}

		return( rtn );
	}

}
