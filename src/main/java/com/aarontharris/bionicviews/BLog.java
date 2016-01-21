package com.aarontharris.bionicviews;

public class BLog {
	public static String TAG = "Bionic";
	public static final String EMPTY_STRING = "";

	public static final void d( String format, Object... objects ) {
		if ( BionicConfig.isDebug() ) {
			aLogD( defaultPrefix() + String.format( format, objects ) + defaultPostfix() );
		}
	}

	public static final void w( String format, Object... objects ) {
		aLogW( String.format( format, objects ) + defaultPostfix() );
	}

	public static final void e( String format, Object... objects ) {
		aLogE( defaultPrefix() + String.format( format, objects ) + defaultPostfix() );
	}

	public static final void e( Exception e ) {
		aLogE( defaultPrefix() + e.getMessage() + defaultPostfix(), e );
	}

	public static final void e( Exception e, String format, Object... objects ) {
		aLogE( defaultPrefix() + String.format( format, objects ) + defaultPostfix(), e );
	}

	private static final String defaultPrefix() {
		// return getCallingClass() + "@" + getCallingClassLineNumber() + ": ";
		return EMPTY_STRING;
	}

	private static final String defaultPostfix() {
		return " {" + getCallingClass() + "@" + getCallingClassLineNumber() + "}";
	}

	private static final void aLogD( String message ) {
		android.util.Log.d( TAG, message );
	}

	private static final void aLogW( String message ) {
		android.util.Log.w( TAG, message );
	}

	private static final void aLogE( String message ) {
		android.util.Log.e( TAG, message );
	}

	private static final void aLogE( String message, Exception e ) {
		if ( message == null ) {
			message = "Message: " + e.getMessage();
		}

		android.util.Log.e( TAG, message );
	}

	private static final StackTraceElement getCallingStackElem() {
		return findStackElem( BLog.class );
	}

	// May appear fairly inefficient but it doesn't need to be, it only needs to be reliable
	// since logging should be VERY infrequent when not in debug mode.
	// production logging should only be errors or maybe warns.
	// This is less efficient but it is absolutely guaranteed that the class is correct
	// rather than using a relative offset, we search for the first item that is not the Logger (after finding the logger in case there's some
	// preceding garbage)

	/**
	 * Find the first line in the stack trace that follows the given class and isn't the given class.
	 *
	 * @param sentinel
	 * @return
	 */
	public static final StackTraceElement findStackElem( Class<?> sentinel ) {
		StackTraceElement[] elems = Thread.currentThread().getStackTrace();
		int i = -1;
		boolean foundLog = false;
		for ( StackTraceElement e : elems ) {
			i++;

			// We've not run into Log yet
			if ( !foundLog && e.getClassName().equals( sentinel.getCanonicalName() ) ) {
				foundLog = true;
			}
			// We've previously found Log and now we've found something that isn't -- this is what we want
			else if ( foundLog && !e.getClassName().equals( sentinel.getCanonicalName() ) ) {
				break;
			}
		}
		StackTraceElement elem = elems[i];
		return elem;
	}

	public static final String getSimpleName( StackTraceElement elem ) {
		String full = elem.getClassName();
		int pos = full.lastIndexOf( '.' );
		return full.substring( pos + 1 ); // should be safe, can't see a class name ending with a .
	}

	private static final String getCallingClass() {
		return getSimpleName( getCallingStackElem() );
	}

	private static final int getCallingClassLineNumber() {
		return getCallingStackElem().getLineNumber();
	}
}
