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
import org.apache.commons.codec.digest.DigestUtils;

// the AppInventor class ...

@DesignerComponent(version = EncodeText.VERSION,
  description = "This extension provides Base64 text encoding and decoding functions",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/extension.png")

@SimpleObject( external=true )
public final class EncodeText extends AndroidNonvisibleComponent {

  public static final int VERSION = 1;
  // default encoding is UTF-8
  public static final String DEFAULT_ENCODING = "UTF-8";

  public EncodeText(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Encode a string in Base64" )
  public String ToBase64( String input ) {
    // NOTE: getBytes needs a char-set (default='utf-8')
    String rtn = "ERROR";
    try {
      rtn = Base64.encodeBase64String( input.getBytes(DEFAULT_ENCODING) );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( rtn );
  }

  @SimpleFunction(description = "Decode a Base64 string" )
  public String FromBase64( String input ) {
    byte b[] = Base64.decodeBase64( input );
    return( new String(b) );
  }

  @SimpleFunction(description = "Encode a string in hex" )
  public String ToHex( String input ) {
    String rtn = "ERROR";
    // NOTE: getBytes needs a char-set (default='utf-8')
    try {
      rtn = Hex.encodeHexString( input.getBytes(DEFAULT_ENCODING) );
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( rtn );
  }
 
  @SimpleFunction(description = "Decode a hex string" )
  public String FromHex( String input ) {
    String rtn = "ERROR";
    try {
      byte b[] = Hex.decodeHex( input.toCharArray() );
      rtn = new String(b);
    } catch( Exception e ) {
      // TODO: throw an error
    }
    return( rtn );
  }

  @SimpleFunction(description = "Return the MD5 hash of a string" )
  public String ToMD5( String input ) {
    String rtn = DigestUtils.md5Hex( input );
    return( rtn );
  }

  @SimpleFunction(description = "Return the SHA-1 hash of a string" )
  public String ToSHA1( String input ) {
    String rtn = DigestUtils.sha1Hex( input );
    return( rtn );
  }

  @SimpleFunction(description = "Return the SHA-256 hash of a string" )
  public String ToSHA256( String input ) {
    String rtn = DigestUtils.sha256Hex( input );
    return( rtn );
  }

  @SimpleFunction(description = "Return the SHA-512 hash of a string" )
  public String ToSHA512( String input ) {
    String rtn = DigestUtils.sha512Hex( input );
    return( rtn );
  }

}

