// Copyright 2017, John Pormann, Duke University
//
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
// borrowed from several java files in main-line AppInventor source
// that code is Copyright 2011-2014 MIT, All rights reserved

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import android.app.Activity;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import org.json.JSONObject;
import org.json.JSONException;

@DesignerComponent( version = JsonFileDB.VERSION,
    description = "JsonFileDB is a non-visible component that loads JSON data from " +
    "a file on the phone's SD card and presents it as a hash table.</p>",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@SimpleObject( external=true )
public class JsonFileDB extends AndroidNonvisibleComponent {

  public static final int VERSION = 1;
  private final Activity activity;

  private static String valueIfTagNotThere = "__NULL__";
  private JSONObject mainDB;
  public static String status = "Not initialized";
  public static String altstatus = "Not initialized";

  public JsonFileDB(ComponentContainer container) {
    super(container.$form());
	activity = container.$context();
  }

  //private void readJsonFile( String filename ) throws Exception {
  //  File file = new File(filename);
  //  String contents = FileUtils.readFileToString(file,"utf-8");
  //  // convert to JSON
  //  mainDB = new JSONObject(contents);
  //}


  @SimpleFunction( description="Return status info" )
  public String GetStatus() {
    return new String(status);
  }

  @SimpleFunction( description="Return alt-status info" )
  public String GetAltStatus() {
    return new String(altstatus);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
	  description = "The default value if tag is not present")
  public String DefaultValueIfTagNotThere() {
	  return valueIfTagNotThere;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
	  defaultValue = "")
  @SimpleProperty
  public void DefaultValueIfTagNotThere( String x ) {
	  valueIfTagNotThere = x;
  }

  @SimpleFunction( description="Load the DB from a JSON file" )
  public void LoadDBFile( final String filename ) {
	status = "Starting db-initialization";
	AsynchUtil.runAsynchronously(new Runnable() {
		@Override
		public void run() {
			try {
				status = "Initializing DB";
				readJsonFile( filename );
			} catch( Exception e ) {
				form.dispatchErrorOccurredEvent( JsonFileDB.this, "login", 9901 );
			}
		}
	});
  }

  // borrowing heavily from File component, ReadFrom
  private void readJsonFile( String filename ) throws Exception {
    String fullFilename;
    if( filename.startsWith("//") ) {
      fullFilename = Environment.getExternalStorageDirectory().getPath() + "/AppInventor/assets/" + filename;
    } else if( filename.startsWith("/") ) {
      fullFilename = Environment.getExternalStorageDirectory().getPath() + filename;
    } else {
      fullFilename = filename;
    }

    status = "FileUtil.readfile";
    byte b[] = FileUtil.readFile( fullFilename );
    status = "new String";
    final String contents = new String(b,"utf-8");
    status = contents;
    mainDB = new JSONObject(contents);
    altstatus = "mainDB is initialized";
	activity.runOnUiThread(new Runnable() {
	  @Override
	  public void run() {
		  InitializationComplete();
	  }
	});
  }

  @SimpleEvent(description = "Event triggered when the db-file is read")
  public void InitializationComplete() {
	  EventDispatcher.dispatchEvent( this, "InitializationComplete" );
  }

  @SimpleFunction( description="Get a value from a level-1 key in the database" )
  public String GetValue1( String tag1 ) {
    String rtn = "ERROR";
    try {
      rtn = mainDB.getString( tag1 );
    } catch( Exception e ) {
      rtn = valueIfTagNotThere;
      status = "GetValue1 threw an error";
    }
    return rtn;
  }

  @SimpleFunction( description="Get a value from level-1/-2 keys in the database" )
  public String GetValue2( String tag1, String tag2 ) {
    String rtn = "ERROR";
    try {
      rtn = "GetValue2-tag1";
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      rtn = "GetValue2-tag1-returned";
      rtn = obj1.getString( tag2 );
    } catch( Exception e ) {
      //rtn = valueIfTagNotThere;
      status = "GetValue2 threw an error";
    }
    return rtn;
  }

  @SimpleFunction( description="Get a value from level-1/-2/-3 keys in the database" )
  public String GetValue3( String tag1, String tag2, String tag3 ) {
    String rtn = "ERROR";
    try {
      rtn = "GetValue3-tag1";
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      rtn = "GetValue3-tag2";
      JSONObject obj2 = obj1.getJSONObject( tag2 );
      rtn = "GetValue3-tag2-returned";
      rtn = obj2.getString( tag3 );
    } catch( Exception e ) {
      //rtn = valueIfTagNotThere;
      status = "GetValue3 threw an error";
    }
    return rtn;
  }

  @SimpleFunction( description="Get a value from level-1/-2/-3/-4 keys in the database" )
  public String GetValue4( String tag1, String tag2, String tag3, String tag4 ) {
    String rtn = "ERROR";
    try {
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      JSONObject obj2 = obj1.getJSONObject( tag2 );
      JSONObject obj3 = obj2.getJSONObject( tag3 );
      rtn = obj3.getString( tag4 );
    } catch( Exception e ) {
      //rtn = valueIfTagNotThere;
      status = "GetValue4 threw an error";
    }
    return rtn;
  }

  @SimpleFunction( description="Get all tags at level-1 in the database" )
  public Object GetTags1() {
    List<String> keyList = new ArrayList<String>();
    Iterator keyIter = mainDB.keys();
    while( keyIter.hasNext() ) {
      String key = (String)keyIter.next();
      keyList.add( key );
    }
    java.util.Collections.sort(keyList);
    return keyList;
  }

  @SimpleFunction( description="Get all tags at level-2 in the database" )
  public Object GetTags2( String tag1 ) {
    List<String> keyList = new ArrayList<String>();
    try {
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      Iterator keyIter = obj1.keys();
      while( keyIter.hasNext() ) {
        String key = (String)keyIter.next();
        keyList.add( key );
      }
      java.util.Collections.sort(keyList);
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return keyList;
  }

  @SimpleFunction( description="Get all tags at level-3 in the database" )
  public Object GetTags3( String tag1, String tag2 ) {
    List<String> keyList = new ArrayList<String>();
    try {
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      JSONObject obj2 = obj1.getJSONObject( tag2 );
      Iterator keyIter = obj2.keys();
      while( keyIter.hasNext() ) {
        String key = (String)keyIter.next();
        keyList.add( key );
      }
      java.util.Collections.sort(keyList);
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return keyList;
  }

  @SimpleFunction( description="Get all tags at level-4 in the database" )
  public Object GetTags4( String tag1, String tag2, String tag3 ) {
    List<String> keyList = new ArrayList<String>();
    try {
      JSONObject obj1 = mainDB.getJSONObject( tag1 );
      JSONObject obj2 = obj1.getJSONObject( tag2 );
      JSONObject obj3 = obj2.getJSONObject( tag3 );
      Iterator keyIter = obj3.keys();
      while( keyIter.hasNext() ) {
        String key = (String)keyIter.next();
        keyList.add( key );
      }
      java.util.Collections.sort(keyList);
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return keyList;
  }

}
