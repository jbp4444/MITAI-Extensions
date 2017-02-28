package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

// the AppInventor class ...

@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
  description = "This extension provides Base64 text encoding and decoding functions",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/extension.png")

@SimpleObject( external=true )
public final class EncodeText extends AndroidNonvisibleComponent {

  // default encoding is UTF-8
  public static final String DEFAULT_ENCODING = "UTF-8";

  public EncodeText(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Encode a string in Base64" )
  public String ToBase64 ( String input ) {
    // NOTE: getBytes needs a char-set (default='utf-8')
    byte b[] = { 'E','R','R','O','R' };
    try {
      b = Base64.encodeBase64( input.getBytes(DEFAULT_ENCODING) );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( new String(b) );
  }

  @SimpleFunction(description = "Decode a Base64 string" )
  public String FromBase64 ( String input ) {
    byte b[] = { 'E','R','R','O','R' };
    // NOTE: getBytes needs a char-set (default='utf-8')
    try {
      b = Base64.decodeBase64( input.getBytes(DEFAULT_ENCODING) );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( new String(b) );
  }

  @SimpleFunction(description = "Encode a string in hex" )
  public String ToHex ( String input ) {
    //byte b[] = { 'E','R','R','O','R' };
    char b[] = { 'E','R','R','O','R' };
    // NOTE: getBytes needs a char-set (default='utf-8')
    try {
      b = Hex.encodeHex( input.getBytes(DEFAULT_ENCODING) );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( new String(b) );
  }
 
  @SimpleFunction(description = "Decode a hex string" )
  public String FromHex ( String input ) {
    byte b[] = { 'E','R','R','O','R' };
    // NOTE: getBytes needs a char-set (default='utf-8')
    try {
      //b = Hex.decodeHex( input.getBytes(DEFAULT_ENCODING) );
      b = Hex.decodeHex( input.toCharArray() );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( new String(b) );
  }

}

