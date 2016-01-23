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

		public String getString( String key, String defaultValue ) {
			try {
				Meta meta = Bionic.this.lookup( getView( this ), key );
				if ( meta != null ) {
					Object value = meta.data.get( key );
					return value == null ? null : String.valueOf( value );
				}
			} catch ( Exception e ) {
				BLog.e( e );
			}
			return defaultValue;
		}

		public void putString( String key, String value ) {
			if ( data == null ) {
				data = new HashMap<>();
			}
			data.put( key, value );
			notifyChildren( this, new MetaKeyChangedEvent( key ) );
		}

		public void subscribeKeyChange( String key, boolean asap, OnMetaEvent eventHandler ) {
			if ( subscribedKeyChangedEvents == null ) {
				subscribedKeyChangedEvents = new HashMap<>();
			}
			subscribedKeyChangedEvents.put( key, eventHandler );
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
		}

		private void onEvent( Meta metaSend, MetaEvent event ) { // FIXME need param model
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

		private void notifyChildren( Meta metaSend, MetaEvent event ) {
			View view = getView( this );
			Bionic.this.notifyChildren( view, metaSend, event );
		}
	}

	private WeakHashMap<View, Meta> viewMetaMap = new WeakHashMap<>();
	private WeakHashMap<Meta, WeakReference<View>> metaViewMap = new WeakHashMap<>();

	public Meta getMeta( View view ) {
		if ( viewMetaMap != null ) {
			return viewMetaMap.get( view );
		}
		return null;
	}

	public Meta attainMeta( View view ) {
		Meta meta = getMeta( view );
		if ( meta == null ) {
			meta = new Meta();
			viewMetaMap.put( view, meta );
			metaViewMap.put( meta, new WeakReference<View>( view ) );
		}
		return meta;
	}

	private View getView( Meta meta ) {
		View view = metaViewMap.get( meta ).get(); // should never be null since a meta should not exist outside the scope of the view
		if ( view == null ) {
			metaViewMap.remove( meta );
		}
		return view;
	}

	public void notifyChildren( View view, Meta metaSend, MetaEvent event ) {
		if ( view != null && view instanceof ViewGroup ) {
			ViewGroup viewGroup = (ViewGroup) view;
			int childCount = viewGroup.getChildCount();
			for ( int childIdx = 0; childIdx < childCount; childIdx++ ) {
				View childView = viewGroup.getChildAt( childIdx );
				Meta meta = getMeta( childView );
				if ( meta == null ) {
					notifyChildren( childView, metaSend, event ); // FIXME: recursive for speediness -- for now
				} else {
					meta.onEvent( metaSend, event );
				}
			}
		}
	}

	private Meta lookup( View view, String key ) {
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
}
