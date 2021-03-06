// Copyright 2017, John Pormann, Duke University
//
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
// borrowed from several java files in main-line AppInventor source
// that code is Copyright 2011-2014 MIT, All rights reserved

package edu.duke.appinventor;

import com.google.appinventor.components.runtime.*;

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


@DesignerComponent(version = Crowdcrafting.VERSION,
    description = "Provides connectivity to Crowdcrafting.org services",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject( external=true )
public final class Crowdcrafting extends AndroidNonvisibleComponent {

	public static final int VERSION = 1;

	private final Context context;
	private final Activity activity;
	private final SharedPreferences sharedPreferences;

	private static final String base_acct_url = "http://crowdcrafting.org/account/";
	private static final String base_api_url = "http://crowdcrafting.org/api/";

	private static final int H_SESSION  = 2;
	private static final int H_CSRF     = 4;
	private static final int H_REMEMBER = 8;
	private static final int H_JSON     = 16;

	private String last_status = "not initialized";

	// Constructor
	public Crowdcrafting( ComponentContainer container ) {
		super(container.$form());

		// Set up the Tagline in the 'About' screen
		//form.setCrowdcraftingTagline();

		context = container.$context();
		activity = container.$context();
		sharedPreferences = context.getSharedPreferences("Crowdcrafting",Context.MODE_PRIVATE);
	}

	// getter/setter methods for status, session, csrf, remember, etc.
	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The status of the last network request")
	public String Status() {
		return last_status;
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The value of the CSRF Token")
	public String CsrfToken() {
		return GetValue( "csrf_token", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void CsrfToken( String x ) {
		StoreValue( "csrf_token", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The value of the Session Token")
	public String SessionToken() {
		return GetValue( "session_token", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void SessionToken( String x ) {
		StoreValue( "session_token", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The value of the Remember Token")
	public String RememberToken() {
		return GetValue( "remember_token", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void RememberToken( String x ) {
		StoreValue( "remember_token", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The email/username for the current user")
	public String Username() {
		return GetValue( "username", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void Username( String x ) {
		StoreValue( "username", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The password for the current user")
	public String Password() {
		return GetValue( "password", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void Password( String x ) {
		StoreValue( "password", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The project-ID for the current project")
	public String ProjectID() {
		return GetValue( "project_id", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void ProjectID( String x ) {
		StoreValue( "project_id", x );
	}

	@SimpleProperty(category = PropertyCategory.BEHAVIOR,
		description = "The task-ID for the current task")
	public String TaskID() {
		return GetValue( "task_id", "none" );
	}
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		defaultValue = "")
	@SimpleProperty
	public void TaskID( String x ) {
		StoreValue( "task_id", x );
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

	// start the 2-phase login process
	@SimpleFunction(description = "Login to the Crowdcrafting services")
	public void Login() {
		last_status = "starting login process";
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					last_status = "starting phase-1 of login";
					String url = base_acct_url + "signin";
					String response = performRequest( "login1", url, null, H_JSON );

					// TODO: there is no error checking here
					int csrf_i = response.indexOf( "csrf" );
					int csrf_j = response.indexOf( "\"", csrf_i+5 );
					int csrf_k = response.indexOf( "\"", csrf_j+1 );
					String csrf_token = response.substring(csrf_j+1,csrf_k);
					StoreValue( "csrf_token", csrf_token );

					last_status = "starting phase-2 of login";
					url = base_acct_url + "signin";
					// send the post data
					String username = GetValue( "username", "none" );
					String password = GetValue( "password", "none" );
					String jsondata = "{ \"email\": \""+username+"\", \"password\": \""+password+"\" }";
					response = performRequest( "login2", url, jsondata, H_JSON|H_CSRF|H_SESSION );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "login", 9901 );
				}
			}
		});
	}

	// Event indicating that the login is complete
	@SimpleEvent(description = "Event triggered when the user is logged in")
	public void GotLogin( String responseCode, String response ) {
		EventDispatcher.dispatchEvent( this, "GotLogin", responseCode, response );
	}

	//  //  //  //  //  //  //  //  //  // //  //  //  //

	@SimpleFunction(description = "Get the user's Crowdcrafting profile")
	public void getUserProfile() {
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					String url = base_acct_url + "profile";
					String response = performRequest( "userprofile", url, null, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "getUserProfile", 9902 );
				}
			}
		});
	}

	// Event indicating that the get-user-profile request is complete
	@SimpleEvent(description = "Event triggered when the user profile is retrieved")
	public void GotUserProfile( String responseCode, String response ) {
		EventDispatcher.dispatchEvent( this, "GotUserProfile", responseCode, response );
	}

	//  //  //  //  //  //  //  //  //  // //  //  //  //

	@SimpleFunction(description = "Get a list of projects owned by this user")
	public void getProjectList() {
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					String url = base_api_url + "project";
					String response = performRequest( "projectlist", url, null, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "getProjectList", 9903 );
				}
			}
		});
	}

	@SimpleFunction(description = "Get the list of all available projects for this user")
	public void getAllProjectList() {
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					// TODO: last_id parameter allows for pagination
					String url = base_api_url + "project?all=1";
					last_status = "url is ["+url+"]";
					String response = performRequest( "projectlist", url, null, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "getAllProjectList", 9904 );
				}
			}
		});
	}

	@SimpleFunction(description = "Query the list of all available projects based on a short-name")
	public void queryProjectList( String shortName ) {
		final String tmpVal = shortName;
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					// TODO: last_id parameter allows for pagination
					// TODO: should check shortName for validity (or uu-encode it?)
					String url = base_api_url + "project?all=1&short_name=" + tmpVal;
					String response = performRequest( "projectlist", url, null, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "queryProjectList", 9905 );
				}
			}
		});
	}

	// Event indicating that the get-user-profile request is complete
	@SimpleEvent(description = "Event triggered when the user's project list is retrieved")
	public void GotProjectList( String responseCode, String response ) {
		EventDispatcher.dispatchEvent( this, "GotProjectList", responseCode, response );
	}

	//  //  //  //  //  //  //  //  //  // //  //  //  //

	@SimpleFunction(description = "Get the next task in a project")
	public void getNextTask() {
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					// TODO: make sure project_id is not null or zero
					String project_id = GetValue( "project_id", "0" );
					String url = base_api_url + "project/" + project_id + "/newtask";
					String response = performRequest( "nexttask", url, null, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "getNextTask", 9906 );
				}
			}
		});
	}

	// Event indicating that the get-user-profile request is complete
	@SimpleEvent(description = "Event triggered when the next task is retrieved")
	public void GotNextTask( String responseCode, String response ) {
		try {
			JSONObject obj = new JSONObject(response);
			String nextTaskID = String.valueOf( obj.getInt("id") );
			StoreValue( "task_id", nextTaskID );
		} catch( Exception e ) {
			//StoreValue( "task_id", "0" );
		}
		EventDispatcher.dispatchEvent( this, "GotNextTask", responseCode, response );
	}

	//  //  //  //  //  //  //  //  //  // //  //  //  //

	@SimpleFunction(description = "Post the answer to the current task")
	public void postAnswer( String answerValue ) {
		final String tmpVal = answerValue;
		AsynchUtil.runAsynchronously(new Runnable() {
			@Override
			public void run() {
				try {
					String url = base_api_url + "/taskrun";
					// TODO: make sure task_id is not null or zero
					// TODO: make sure project_id is not null or zero
					String project_id = GetValue( "project_id", "0" );
					String task_id    = GetValue( "task_id", "0" );
					String jsondata = "{ \"project_id\": " + project_id + ", "
						+ "\"task_id\": " + task_id + ", "
						+ "\"info\": \""+ tmpVal +"\" "
				 		+ "}";
					String response = performRequest( "postanswer", url, jsondata, H_JSON|H_SESSION|H_REMEMBER );
				} catch( Exception e ) {
					form.dispatchErrorOccurredEvent(Crowdcrafting.this, "postAnswer", 9907 );
				}
			}
		});
	}

	// Event indicating that the get-user-profile request is complete
	@SimpleEvent(description = "Event triggered when an answer is posted")
	public void PostedAnswer( String responseCode, String response ) {
		//StoreValue( "task_id", "0" );
		EventDispatcher.dispatchEvent( this, "PostedAnswer", responseCode, response );
	}

	//  //  //  //  //  //  //  //  //  //  //  //  //  //  //
	  //  //  //  //  //  //  //  //  //  //  //  //  //  //
	//  //  //  //  //  //  //  //  //  //  //  //  //  //  //

	// taken from TinyDB.java in main source code
	public void StoreValue(final String tag, final String valueToStore) {
		final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
		try {
			sharedPrefsEditor.putString( tag, valueToStore );
			//sharedPrefsEditor.commit();
			sharedPrefsEditor.apply();
		} catch( Exception e ) {
			//throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
		}
	}
	public String GetValue(final String tag, final String valueIfTagNotThere ) {
		try {
			return sharedPreferences.getString( tag, valueIfTagNotThere );
		} catch( Exception e ) {
			return "error";
		}
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
					  //session_token = hdr_value;
					  StoreValue( "session_token", hdr_value );
				  } else if( hdr_value.startsWith("remember_token=") ) {
					  //System.out.println( "-- found remember token" );
					  //remember_token = hdr_value;
					  StoreValue( "remember_token", hdr_value );
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

	private static void writeRequestData(HttpURLConnection connection, byte[] postData) throws IOException {
		// According to the documentation at
		// http://developer.android.com/reference/java/net/HttpURLConnection.html
		// HttpURLConnection uses the GET method by default. It will use POST if setDoOutput(true) has
		// been called.
		connection.setDoOutput(true); // This makes it something other than a HTTP GET.
		// Write the data.
		connection.setFixedLengthStreamingMode(postData.length);
		BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
		try {
			out.write(postData, 0, postData.length);
			out.flush();
		} finally {
			out.close();
		}
	}

	private String performRequest( String cmd, String finalURL, String data, int flags ) throws IOException {
		String rtnval = "error";
		String httpVerb = "GET";
		if( (cmd.equals("login2")) || (cmd.equals("postanswer")) ) {
			httpVerb = "POST";
			// TODO: make sure data is not null
		}

		String csrf_token     = GetValue( "csrf_token", "none" );
		String session_token  = GetValue( "session_token", "none" );
		String remember_token = GetValue( "remember_token", "none" );

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

				//System.out.println( "jsonData ["+jsonData+"]" );
				if( data != null ) {
					// TODO: should send a charset encoding into getBytes
					writeRequestData( cnx, data.getBytes() );
				}

				// make the actual request and get response code
				last_status = "getting http data";
				final int responseCode = cnx.getResponseCode();
				// get the response data
				//String response = processResponse( cnx );
				last_status = "getting response content";
				rtnval = getResponseContent( cnx );
				last_status = "response code = "+Integer.toString(responseCode)
					+ "; text = "+rtnval;
				final String response = rtnval;
				//System.out.println( "response ["+response+"]" );

				// check for any set-cookie headers (session tokens)
				last_status = "parsing headers";
				parseHeaders( cnx );

				// Dispatch the event.
				if( cmd.equals("login2") ) {
					last_status = "launching GotLogin event";
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  GotLogin( Integer.toString(responseCode), response );
					  }
					});
				} else if( cmd.equals("userprofile") ) {
					last_status = "launching GotUserProfile event";
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  GotUserProfile( Integer.toString(responseCode), response );
					  }
					});
				} else if( cmd.equals("projectlist") ) {
					last_status = "launching GotProjectList event";
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  GotProjectList( Integer.toString(responseCode), response );
					  }
					});
				} else if( cmd.equals("nexttask") ) {
					last_status = "launching GotNextTask event";
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  GotNextTask( Integer.toString(responseCode), response );
					  }
					});
				} else if( cmd.equals("postanswer") ) {
					last_status = "launching PostedAnswer event";
					activity.runOnUiThread(new Runnable() {
					  @Override
					  public void run() {
						  PostedAnswer( Integer.toString(responseCode), response );
					  }
					});
				}

			} catch( Exception e ) {
				last_status = last_status + "  caught exception in performRequest [" + e.toString() +"]";

			} finally {
				cnx.disconnect();
			}
		}
		return( rtnval );
	}


}
