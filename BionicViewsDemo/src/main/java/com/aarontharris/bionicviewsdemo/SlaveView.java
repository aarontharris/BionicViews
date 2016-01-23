package com.aarontharris.bionicviewsdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aarontharris.bionicviews.Bionic;
import com.aarontharris.bionicviews.Bionic.Meta;
import com.aarontharris.bionicviews.Bionic.MetaEvent;
import com.aarontharris.bionicviews.Bionic.MetaKeyChangedEvent;
import com.aarontharris.bionicviews.Bionic.OnMetaEvent;

public class SlaveView extends LinearLayout {
	private TextView message;

	public SlaveView( Context context ) {
		super( context );
		init();
	}

	public SlaveView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init();
	}

	public SlaveView( Context context, AttributeSet attrs, int defStyleAttr ) {
		super( context, attrs, defStyleAttr );
		init();
	}

	private void init() throws RuntimeException {
		try {
			LayoutInflater.from( getContext() ).inflate( R.layout.merge_slaveview, this, true );
			setOrientation( VERTICAL );
			message = (TextView) findViewById( R.id.slaveview_message_textview );

			if ( !isInEditMode() ) {


				Meta meta = Bionic.getInstance().attainMeta( this );
				meta.subscribeKeyChange( "master.testkey1", true, new OnMetaEvent() {
					@Override
					public boolean handleEvent( Meta metaSend, Meta metaRecv, MetaEvent event ) {
						if ( MetaKeyChangedEvent.class.equals( event.getType() ) ) {
							String key = ( (MetaKeyChangedEvent) event ).getKey();
							message.setText( metaSend.getString( key, "fail" ) );
						}
						return true;
					}
				} );


			}
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

}
