package com.aarontharris.bionicviews;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class Bionic {
	private static final Bionic self = new Bionic();
	private static final String UNDEF = "__BIONIC::UNDEF__";

	public static Bionic getInstance() {
		return self;
	}

	public static abstract class BionicKey<T> {
		private String key;

		public BionicKey( String key ) {
			this.key = key;
		}

		public String getKeyString() {
			return key;
		}

		public abstract T get( Meta meta );
		public abstract void put( Meta meta, T value );
	}

	public static class StringKey extends BionicKey<String> {
		public StringKey( String key ) {
			super( key );
		}

		@Override
		public String get( Meta meta ) {
			return meta.getString( getKeyString(), null );
		}

		@Override
		public void put( Meta meta, String value ) {
			meta.putString( getKeyString(), value );
		}
	}

	/**
	 * A simple BionicKey that just puts and gets an object associated with the given key without any translation.<br>
	 * The type is just cast to T when returned.<br>
	 * Convenience for what is likely to be the 99% use case.<br>
	 *
	 * @param <T>
	 */
	public static class SimpleKey<T> extends BionicKey<T> {

		public SimpleKey( String key ) {
			super( key );
		}

		@Override
		public T get( Meta meta ) {
			return (T) meta.getObject( getKeyString(), null );
		}

		@Override
		public void put( Meta meta, T value ) {
			meta.putObject( getKeyString(), value );
		}
	}

	public static interface OnMetaEvent {
		/**
		 * Handle an event propogated to this Meta.
		 *
		 * @param metaSend - the meta that created the event
		 * @param metaRecv - the meta receiving the event
		 * @param event
		 * @return propagate? false if the event was consumed and should not be propagated.
		 */
		public boolean handleEvent( Meta metaSend, Meta metaRecv, MetaEvent event );
	}


	public static abstract class MetaEvent {
		private Class<? extends MetaEvent> type;

		public MetaEvent( Class<? extends MetaEvent> type ) {
			this.type = type;
		}

		public Class<? extends MetaEvent> getType() {
			return type;
		}
	}


	public static class MetaKeyChangedEvent extends MetaEvent {
		private String key;

		public MetaKeyChangedEvent( String key ) {
			super( MetaKeyChangedEvent.class );
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}


	// Meta is an inner class because it is managed by Bionic
	// Each Meta should only be alive as long as its manager, so its okay to nest.
	public class Meta {
		private Map<String, Object> data;
		private Map<String, OnMetaEvent> subscribedKeyChangedEvents = null;

		public Meta() {
		}

		public boolean containsKey( String key ) {
			return data != null && data.containsKey( key );
		}

		/**
		 * Find the nearest ancestor to the meta's view containing the given key and return its value<br>
		 * If key is not obtainable, defaultValue is returned<br>
		 *
		 * @param key
		 * @param defaultValue
		 */
		public Object getObject( String key, Object defaultValue ) {
			try {
				return Bionic.this.getObject( getView( this ), key, defaultValue );
			} catch ( Exception e ) {
				BLog.e( e );
			}
			return defaultValue;
		}


		/**
		 * Associates the given key->value to this Meta.  Descendants of this View will get this Meta's copy of the value if this key exists
		 * higher up the tree.
		 *
		 * @param key
		 * @param value
		 */
		public void putObject( String key, Object value ) {
			if ( data == null ) {
				data = new HashMap<>();
			}
			data.put( key, value );
			try {
				notifyChildren( this, new MetaKeyChangedEvent( key ) );
			} catch ( Exception e ) {
				BLog.e( e );
			}
		}

		/**
		 * Find the nearest ancestor to the meta's view containing the given key and return its value<br>
		 * If key is not obtainable, defaultValue is returned<br>
		 *
		 * @param key
		 * @param defaultValue
		 */
		public String getString( String key, String defaultValue ) {
			try {
				return Bionic.this.getString( getView( this ), key, defaultValue );
			} catch ( Exception e ) {
				BLog.e( e );
			}
			return defaultValue;
		}

		/**
		 * Associates the given key->value to this Meta.  Descendants of this View will get this Meta's copy of the value if this key exists
		 * higher up the tree.
		 *
		 * @param key
		 * @param value
		 */
		public void putString( String key, String value ) {
			if ( data == null ) {
				data = new HashMap<>();
			}
			data.put( key, value );
			try {
				notifyChildren( this, new MetaKeyChangedEvent( key ) );
			} catch ( Exception e ) {
				BLog.e( e );
			}
		}

		public void subscribeKeyChange( String key, boolean asap, OnMetaEvent eventHandler ) {
			if ( subscribedKeyChangedEvents == null ) {
				subscribedKeyChangedEvents = new HashMap<>();
			}
			subscribedKeyChangedEvents.put( key, eventHandler );
			try {
				if ( asap ) {
					String value = getString( key, UNDEF );
					if ( value != UNDEF ) {
						Meta meta = Bionic.this.lookup( getView( this ), key );
						MetaKeyChangedEvent event = new MetaKeyChangedEvent( key );
						if ( eventHandler.handleEvent( meta, this, event ) ) {
							notifyChildren( meta, event );
						}
					}
				}
			} catch ( Exception e ) {
				BLog.e( e );
			}
		}

		private void onEvent( Meta metaSend, MetaEvent event ) throws Exception {
			if ( MetaKeyChangedEvent.class.equals( event.getType() ) ) {
				MetaKeyChangedEvent kEvent = (MetaKeyChangedEvent) event;

				// Don't deliver key events to metas that overwrite the key
				// also do not propagate beyond this meta since the children should
				// not care about key changes above this meta (since it controls this key)
				if ( containsKey( kEvent.getKey() ) ) {
					return;
				}

				if ( subscribedKeyChangedEvents != null ) {
					OnMetaEvent onMetaEvent = subscribedKeyChangedEvents.get( kEvent.getKey() );
					if ( onMetaEvent != null ) {
						if ( onMetaEvent.handleEvent( metaSend, this, event ) ) {
							notifyChildren( metaSend, event );
							return;
						}
					}
				}
			}
			notifyChildren( metaSend, event );
		}

		private void notifyChildren( Meta metaSend, MetaEvent event ) throws Exception {
			View view = getView( this );
			Bionic.this.notifyChildren( view, metaSend, event );
		}
	}

	private WeakHashMap<View, Meta> viewMetaMap = new WeakHashMap<>();
	private WeakHashMap<Meta, WeakReference<View>> metaViewMap = new WeakHashMap<>();

	/**
	 * @return may be null if no meta was attained, see {@link #attainMeta(View)}
	 */
	public Meta getMeta( View view ) {
		if ( viewMetaMap != null ) {
			return viewMetaMap.get( view );
		}
		return null;
	}

	public Meta attainMeta( View view ) throws Exception {
		Meta meta = getMeta( view );
		if ( meta == null ) {
			meta = new Meta();
			viewMetaMap.put( view, meta );
			metaViewMap.put( meta, new WeakReference<View>( view ) );
		}
		return meta;
	}

	/**
	 * @return never null
	 */
	private View getView( Meta meta ) throws Exception {
		View view = metaViewMap.get( meta ).get(); // should never be null since a meta should not exist outside the scope of the view
		if ( view == null ) {
			metaViewMap.remove( meta );
		}
		if ( view == null ) {
			throw new IllegalStateException( "No View associated with this Meta!" );
		}
		return view;
	}

	public void notifyChildren( View view, Meta metaSend, MetaEvent event ) throws Exception {
		if ( view != null && view instanceof ViewGroup ) {
			ViewGroup viewGroup = (ViewGroup) view;
			int childCount = viewGroup.getChildCount();
			for ( int childIdx = 0; childIdx < childCount; childIdx++ ) {
				View childView = viewGroup.getChildAt( childIdx );
				Meta meta = getMeta( childView );
				if ( meta == null ) {
					notifyChildren( childView, metaSend, event ); // FIXME: recursive for speediness -- for now
				} else {
					try {
						meta.onEvent( metaSend, event );
					} catch ( Exception e ) {
						BLog.e( e );
					}
				}
			}
		}
	}

	private Meta lookup( View view, String key ) throws Exception {
		Meta meta = viewMetaMap.get( view );
		if ( meta != null && meta.containsKey( key ) ) {
			return meta;
		}

		if ( view instanceof ViewGroup ) {
			ViewParent ptr = view.getParent();
			while ( ptr != null ) {
				meta = viewMetaMap.get( ptr );
				if ( meta != null && meta.containsKey( key ) ) {
					return meta;
				}
				ptr = ptr.getParent();
			}
		}
		return null;
	}

	/**
	 * Find the nearest ancestor to the given view containing the given key and return its value<br>
	 * If key is not obtainable, defaultValue is returned<br>
	 *
	 * @param view
	 * @param key
	 * @param defaultValue
	 */
	public String getString( View view, String key, String defaultValue ) {
		try {
			if ( view == null ) {
				throw new NullPointerException( "View cannot be null" );
			}
			Meta meta = lookup( view, key );
			if ( meta != null ) {
				Object value = meta.data.get( key );
				return value == null ? null : String.valueOf( value );
			}
		} catch ( Exception e ) {
			BLog.e( e );
		}
		return defaultValue;
	}

	/**
	 * Attains a Meta for the given View and associates the value.
	 *
	 * @param view
	 * @param key
	 * @param value
	 * @throws Exception if unable to attain a meta
	 */
	public void putString( View view, String key, String value ) throws Exception {
		attainMeta( view ).putString( key, value );
	}

	/**
	 * Find the nearest ancestor to the given view containing the given key and return its value<br>
	 * If key is not obtainable, defaultValue is returned<br>
	 *
	 * @param view
	 * @param key
	 * @param defaultValue
	 */
	public Object getObject( View view, String key, Object defaultValue ) {
		try {
			if ( view == null ) {
				throw new NullPointerException( "View cannot be null" );
			}
			Meta meta = lookup( view, key );
			if ( meta != null ) {
				return meta.data.get( key );
			}
		} catch ( Exception e ) {
			BLog.e( e );
		}
		return defaultValue;
	}

	/**
	 * Attains a Meta for the given View and associates the value.
	 *
	 * @param view
	 * @param key
	 * @param value
	 * @throws Exception if unable to attain a meta
	 */
	public void putObject( View view, String key, String value ) throws Exception {
		attainMeta( view ).putObject( key, value );
	}

	/**
	 * Get a typed value based on the given type aware key.<br>
	 * Find the nearest ancestor to the given view containing the given key and return its value<br>
	 * If key is not obtainable, defaultValue is returned<br>
	 *
	 * @param view
	 * @param key
	 * @param defaultValue
	 * @param <T>
	 * @return
	 */
	public <T> T getValue( View view, BionicKey<T> key, T defaultValue ) {
		try {
			if ( view == null ) {
				throw new NullPointerException( "View cannot be null" );
			}
			Meta meta = lookup( view, key.getKeyString() );
			if ( meta != null ) {
				return key.get( meta );
			}
		} catch ( Exception e ) {
			BLog.e( e );
		}
		return defaultValue;
	}

	/**
	 * Put a typed value based on the given type aware key<br>
	 * Attains a Meta for the given View and associates the value.
	 *
	 * @param view
	 * @param key
	 * @param value
	 * @param <T>
	 * @throws Exception
	 */
	public <T> void putValue( View view, BionicKey<T> key, T value ) throws Exception {
		Meta meta = attainMeta( view );
		key.put( meta, value );
	}

}
