// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// borrowed heavily from YandexTranslate code in main-line AppInventor source

package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.google.appinventor.components.annotations.UsesPermissions;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.Integer;

@DesignerComponent(version = Crowdcrafting.VERSION,
    description = "Provides connectivity to Crowdcrafting.org services",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject( external=true )
public final class Crowdcrafting extends AndroidNonvisibleComponent {

	public static final int VERSION = 1;

	private final Activity activity;

	private static final String base_acct_url = "http://crowdcrafting.org/account/";
	private static final String base_api_url = "http://crowdcrafting.org/api/";

	private static final int H_APIKEY   = 1;
	private static final int H_SESSION  = 2;
	private static final int H_CSRF     = 4;
	private static final int H_REMEMBER = 8;
	private static final int H_JSON     = 16;

	private String api_key = "b5374c06-83f1-4b66-adbd-51c817445b26";
	private String session_token = "none";
	private String csrf_token = "none";
	private String remember_token = "none";

	private String username = "jbp@pormann.net";
	private String password = "Ss2I5v0dXl";
	private String project_id = "4670";  //"4500";
	private String task_id = "0";

	private String last_status = "not initialized";

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public Crowdcrafting( ComponentContainer container ) {
    super(container.$form());

    // Set up the Tagline in the 'About' screen
    //form.setCrowdcraftingTagline();

    activity = container.$context();
  }

  // getter methods for status, session, csrf, remember
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The status of the last network request")
  public String Status() {
	return last_status;
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The value of the CSRF Token")
  public String CsrfToken() {
	return csrf_token;
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The value of the Session token")
  public String SessionToken() {
	return session_token;
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The value of the Remember token")
  public String RememberToken() {
	return remember_token;
  }

  // getter/setter methods for api_key
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The API Key for Crowdcrafting.org")
  public String apiKey() {
	return api_key;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void apiKey( String ak ) {
	api_key = ak;
  }

  // getter/setter methods for username
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The user's email/username for Crowdcrafting.org")
  public String Username() {
	return username;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Username( String un ) {
	username = un;
  }

  // getter/setter methods for password
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The user's password for Crowdcrafting.org")
  public String Password() {
	return password;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Password( String pw ) {
	password = pw;
  }

  // getter/setter methods for project_id
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The project ID from Crowdcrafting.org")
  public String projectID() {
	return project_id;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void projectID( String id ) {
	project_id = id;
  }

  // getter/setter methods for task_id
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The current task ID from Crowdcrafting.org")
  public String taskID() {
	return task_id;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void taskID( String id ) {
	task_id = id;
  }

	// start the 2-phase login process
	@SimpleFunction(description = "Login to the Crowdcrafting services")
	public void Login() {
		last_status = "starting login process";
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					loginPhase1();
				} catch (IOException e) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "loginPhase1", 9900 );
				} catch (JSONException je) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "loginPhase1", 9901 );
				}
			}
		});
	}

	private void loginPhase1() throws IOException, JSONException {
		last_status = "starting phase-1 of login";
		String url = base_acct_url + "signin";
		String response = performRequest( "login1", url, H_JSON );

		// TODO: there is no error checking here
		int csrf_i = response.indexOf( "csrf" );
		int csrf_j = response.indexOf( "\"", csrf_i+5 );
		int csrf_k = response.indexOf( "\"", csrf_j+1 );
		csrf_token = response.substring(csrf_j+1,csrf_k);
		//System.out.println( "phase1 - found csrf token" );
		//System.out.println( "i="+csrf_i+" j="+csrf_j+" k="+csrf_k+" ["+csrf_token+"]" );

		last_status = "ending phase-1 of login";
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					loginPhase2();
				} catch (IOException e) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "loginPhase2", 9902 );
				} catch (JSONException je) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "loginPhase2", 9903 );
				}
			}
		});
	}

	private void loginPhase2() throws IOException, JSONException {
		last_status = "starting phase-2 of login";
		String url = base_acct_url + "signin";
		String response = performRequest( "login2", url, H_JSON|H_CSRF );
	}

	// helper function to catch Set-Cookie response headers
	private void parseHeaders( HttpURLConnection cnx ) {
	  int i = 0;
	  while( true ) {
		  String hdr_value = cnx.getHeaderField(i);
		  String hdr_type = cnx.getHeaderFieldKey(i);

		  if( (hdr_type==null) && (hdr_value==null) ) {
			  break;
		  } else {
			  //System.out.println( "Header "+i+" : ["+
			  //	  hdr_type + "] = [" + hdr_value + "]" );
			  if( (hdr_type!=null) && (hdr_type.equals("Set-Cookie")) ) {
				  //System.out.println( "Found Cookie" );
				  if( hdr_value.startsWith("session=") ) {
					  //System.out.println( "-- found session token" );
					  session_token = hdr_value;
				  } else if( hdr_value.startsWith("remember_token=") ) {
					  //System.out.println( "-- found remember token" );
					  remember_token = hdr_value;
				  }
			  }
		  }

		  i++;
		  // watchdog: make sure we don't loop forever
		  if( i > 20 ) {
			  break;
		  }
	  }
	}

  private static String getResponseContent(HttpURLConnection connection) throws IOException {
    // Use the content encoding to convert bytes to characters.
    String encoding = connection.getContentEncoding();
    if (encoding == null) {
          encoding = "UTF-8";
    }
    InputStreamReader reader = new InputStreamReader(connection.getInputStream(), encoding);
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
    
	
	private String performRequest( String cmd, String finalURL, int flags ) throws IOException, JSONException {
		String rtnval = "error";
		String httpVerb = "GET";
		if( (cmd.equals("login2")) ) {
			httpVerb = "POST";
		}

		URL url = new URL(finalURL);
		HttpURLConnection cnx = (HttpURLConnection)url.openConnection();

		if( cnx != null ) {
			try {
				cnx.setRequestMethod( httpVerb );

				// set any headers, given by flags argument
				if( (flags&H_JSON) != 0 ) {
					cnx.setRequestProperty( "Content-type", "application/json" );
				}
				if( (flags&H_CSRF) != 0 ) {
					cnx.setRequestProperty( "X-CSRFToken", csrf_token );
				}
				if( (flags&H_SESSION) != 0 ) {
					cnx.addRequestProperty( "Cookie", session_token );
				}
				if( (flags&H_REMEMBER) != 0 ) {
					cnx.addRequestProperty( "Cookie", remember_token );
				}

				// make the actual request and get response code
				final int responseCode = cnx.getResponseCode();
				// get the response data
				//String response = processResponse( cnx );
				rtnval = getResponseContent( cnx );
				final String response = rtnval;
				//System.out.println( "response ["+response+"]" );
				//JSONObject jsonResponse = new JSONObject(responseContent);

				// check for any set-cookie headers (session tokens)
				parseHeaders( cnx );

				// Dispatch the event.
				if( cmd.equals("login2") ) {
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  GotLogin( Integer.toString(responseCode), response );
					  }
					});
				}

			} finally {
				cnx.disconnect();
			}
		}
		return( rtnval );
	}

	// Event indicating that the login is complete
	@SimpleEvent(description = "Event triggered when the user is logged in")
	public void GotLogin( String responseCode, String response ) {
		EventDispatcher.dispatchEvent( this, "GotLogin", responseCode, response );
	}

}
