// Copyright 2017, John Pormann, Duke University
//
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
// borrowed from several java files in main-line AppInventor source
// that code is Copyright 2011-2014 MIT, All rights reserved

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.Integer;
import java.lang.Exception;


@DesignerComponent(version = KvDb.VERSION,
    description = "Provides connectivity to a web-based key-value storage service",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject( external=true )
public final class KvDb extends AndroidNonvisibleComponent {

	public static final int VERSION = 1;

	private final Context context;
	private final Activity activity;
	private final SharedPreferences sharedPreferences;

	private String last_status = "not initialized";

	// Constructor
	public KvDb( ComponentContainer container ) {
		super(container.$form());

		// Set up the Tagline in the 'About' screen
		//form.setKvDbTagline();

		context = container.$context();
		activity = container.$context();
		sharedPreferences = context.getSharedPreferences("KvDb",Context.MODE_PRIVATE);
	}

	// getter/setter methods for status, session, csrf, remember, etc.
	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The status of the last network request")
	public String Status() {
		return last_status;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The value of the Auth Token")
	public String AuthToken() {
		return GetConfigValue( "auth_token", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "Set the value of the Auth Token")
	@SimpleProperty
	public void AuthToken( String x ) {
		PutConfigValue( "auth_token", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The base url for the remote web service")
	public String BaseURL() {
		return GetConfigValue( "base_url", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "Set the base url for the remote web service")
	@SimpleProperty
	public void BaseURL( String x ) {
		PutConfigValue( "base_url", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The value of the Remember Token")
	public String RememberToken() {
		return GetConfigValue( "remember_token", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void RememberToken( String x ) {
		PutConfigValue( "remember_token", x );
	}

	@SimpleFunction(description = "Converts a JSON-string to an AppInventor object" )
	public Object JsonToPairs( String jsonText ) {
		try {
			return JsonUtil.getObjectFromJson(jsonText);
		} catch ( Exception e ) {
			form.dispatchErrorOccurredEvent(this, "JsonToPairs",
				ErrorMessages.ERROR_WEB_JSON_TEXT_DECODE_FAILED, jsonText);
			return "";
		}
	}

	//  //  //  //  //  //  //  //  //  // //  //  //  //

	@SimpleFunction(description = "Get the value associated with a key")
	public void GetValue( String tag ) {
		final String f_tag = new String(tag);
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					String response = performRequest( "get", f_tag, null );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(KvDb.this, "GetConfigValue", 9902 );
				}
			}
		});
	}

	@SimpleFunction(description = "Set the value associated with a key")
	public void SetValue( String tag, String value ) {
		final String f_tag = new String(tag);
		final String f_value = new String(value);
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					String response = performRequest( "set", f_tag, f_value );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(KvDb.this, "GetConfigValue", 9902 );
				}
			}
		});
	}

	// Event indicating that the web/remote request is complete and successful
	@SimpleEvent( description="Event triggered after successful get/set on remote web-server" )
	public void CommandSuccess( String responseCode, String response ) {
		EventDispatcher.dispatchEvent( this, "CommandSuccess", responseCode, response );
	}

	@SimpleEvent( description="Event triggered after an error during connection" )
	public void CommandError( String responseCode, String response ) {
		// Invoke the application's "WebServiceError" event handler
		// Log.w(LOG_TAG, "calling error event handler: " + message);
		EventDispatcher.dispatchEvent(this, "CommandError", responseCode, response );
	}

	private static String getResponseContent(HttpURLConnection connection) throws IOException {
		// Use the content encoding to convert bytes to characters.
		String encoding = connection.getContentEncoding();
		if (encoding == null) {
			encoding = "UTF-8";
		}
		InputStreamReader reader;
		// JBP - in case there's an error and values are on error-stream
		if( connection.getResponseCode() >= 400 ) {
			reader = new InputStreamReader(connection.getErrorStream(), encoding);
		} else {
		 	reader = new InputStreamReader(connection.getInputStream(), encoding);
		}
		try {
			int contentLength = connection.getContentLength();
			StringBuilder sb = (contentLength != -1)
				? new StringBuilder(contentLength)
				: new StringBuilder();
			char[] buf = new char[1024];
			int read;
			while ((read = reader.read(buf)) != -1) {
				sb.append(buf, 0, read);
			}
			return sb.toString();
		} finally {
			reader.close();
		}
	}

	private String performRequest( String cmd, String tag, String value ) throws IOException {
		String rtnval = "error";

		String auth_token  = GetConfigValue( "auth_token", "none" );
		String base_url = GetConfigValue( "base_url", "none" );

		String finalURL = base_url + "?o=" + cmd;
		if( cmd == "get" ) {
			finalURL = finalURL + "&k=" + tag;
		} else if( cmd == "set" ) {
			finalURL = finalURL + "&k=" + tag + "&v=" + value;
		}

		URL url = new URL(finalURL);
		HttpURLConnection cnx = (HttpURLConnection)url.openConnection();

		// assume an error
		int responseCode = 400;
		String response = "error";

		if( cnx == null ) {
			responseCode = 400;
			response = "cannot connect";
		} else {
			try {
				cnx.setRequestMethod( "GET" );
				cnx.setRequestProperty( "Content-type", "application/json" );
				cnx.setRequestProperty( "KV-AUTH-TOKEN", auth_token );

				// make the actual request and get response code
				last_status = "getting http data";
				responseCode = cnx.getResponseCode();
				// get the response data
				//String response = processResponse( cnx );
				last_status = "getting response content";
				rtnval = getResponseContent( cnx );
				last_status = "response code = "+Integer.toString(responseCode)
					+ "; text = "+rtnval;
				response = rtnval;
				//System.out.println( "response ["+response+"]" );

			} catch( Exception e ) {
				responseCode = 400;
				response = e.toString();
				last_status = last_status + "  caught exception in performRequest [" + e.toString() +"]";

			} finally {
				cnx.disconnect();
			}
		}

		// Dispatch the event.
		final String f_response = new String(response);
		final String f_responseCode = new String(Integer.toString(responseCode));
		if( (responseCode>=200) && (responseCode<300) ) {
			last_status = "launching CommandSuccess event";
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					CommandSuccess( f_responseCode, f_response );
				}
			});
		} else {
			last_status = "launching CommandError event";
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					CommandError( f_responseCode, f_response );
				}
			});

		}

		return( rtnval );
	}

	//  //  //  //  //  //  //  //  //  //  //  //  //  //  //
	  //  //  //  //  //  //  //  //  //  //  //  //  //  //
	//  //  //  //  //  //  //  //  //  //  //  //  //  //  //

	// taken from TinyDB.java in main source code
	public void PutConfigValue(final String tag, final String valueToStore) {
		final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
		try {
			sharedPrefsEditor.putString( tag, valueToStore );
			//sharedPrefsEditor.commit();
			sharedPrefsEditor.apply();
		} catch( Exception e ) {
			//throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
		}
	}
	public String GetConfigValue(final String tag, final String valueIfTagNotThere ) {
		try {
			return sharedPreferences.getString( tag, valueIfTagNotThere );
		} catch( Exception e ) {
			return "error";
		}
	}

}
