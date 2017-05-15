// Copyright 2017, John Pormann, Duke University
//
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//

package edu.duke.appinventor;

import com.google.appinventor.components.runtime.*;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

// the AppInventor class ...

@DesignerComponent(version = DumbEncoder.VERSION,
  description = "This extension provides Caeser/Rot-13 Encryption",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/extension.png")

@SimpleObject( external=true )
public final class DumbEncoder extends AndroidNonvisibleComponent {

	public static final int VERSION = 1;

	public DumbEncoder(ComponentContainer container) {
		super(container.$form());
	}

	@SimpleFunction(description = "Encode a string in Caeser/Rot-13" )
	String EncodeText( String message, int shift ){
		String s = "";
		int len = message.length();
		char[] msg = message.toLowerCase().toCharArray();
		shift = shift % 26;
		for( int i=0; i<len; i++ ){
			char c = msg[i];
			if( (c>='a') && (c<='z') ) {
				s += (char)( 'a'+ ((c-'a')+shift)%26 );
			} else {
				// pass non-letters through un-touched
				s += c;
			}
		}
		return( s );
	}

}
