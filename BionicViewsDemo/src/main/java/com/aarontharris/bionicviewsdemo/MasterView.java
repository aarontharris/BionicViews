package com.aarontharris.bionicviewsdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aarontharris.bionicviews.BLog;
import com.aarontharris.bionicviews.Bionic;
import com.aarontharris.bionicviews.Bionic.Meta;

public class MasterView extends LinearLayout {
	public static final String testkey1 = "master.testkey1";

	public MasterView( Context context ) {
		super( context );
		init();
	}

	public MasterView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init();
	}

	public MasterView( Context context, AttributeSet attrs, int defStyleAttr ) {
		super( context, attrs, defStyleAttr );
		init();
	}

	private void init() throws RuntimeException {
		try {
			LayoutInflater.from( getContext() ).inflate( R.layout.merge_masterview, this, true );

			if ( !isInEditMode() ) {

				// Programmatic works too
				{
					SlaveView slaveView = new SlaveView( getContext() );
					((TextView)slaveView.findViewById( R.id.slaveview_message_textview )).setText("I am a slave2");
					slaveView.setOrientation( VERTICAL );
					this.addView( slaveView );
				}

				BLog.d( "Master: init" );

				Meta meta = Bionic.getInstance().attainMeta( this );
				meta.putString( testkey1, "master.testval1" );
			}
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		super.onLayout( changed, l, t, r, b );
		BLog.d( "Master: onLayout %s", changed );
	}
}
