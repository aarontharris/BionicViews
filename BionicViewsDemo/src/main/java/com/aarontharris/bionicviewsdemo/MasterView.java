package com.aarontharris.bionicviewsdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aarontharris.bionicviews.BLog;
import com.aarontharris.bionicviews.Bionic;
import com.aarontharris.bionicviews.Bionic.SimpleKey;
import com.aarontharris.bionicviews.Bionic.StringKey;

public class MasterView extends LinearLayout {
	public static final StringKey testkey1 = new StringKey();
	public static final SimpleKey<Integer> countkey = new SimpleKey<>();

	private int counter = 0;

	private Button mInput = null;

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
			setOrientation( VERTICAL );

			if ( !isInEditMode() ) {
				mInput = (Button) findViewById( R.id.masterview_input_button );
				mInput.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick( View v ) {
						try {
							BLog.d( "Master: click" );
							counter++;
							Bionic.get().putValue( MasterView.this, testkey1, "master.click " + counter );
							Bionic.get().putValue( MasterView.this, countkey, counter );
						} catch ( Exception e ) {
							BLog.e( e );
						}
					}
				} );

				// Programmatic works too
				{
					SlaveView slaveView = new SlaveView( getContext() );
					( (TextView) slaveView.findViewById( R.id.slaveview_message_textview ) ).setText( "I am a slave2" );
					slaveView.setOrientation( VERTICAL );
					this.addView( slaveView );
				}

				BLog.d( "Master: init" );
				Bionic.get().putValue( this, testkey1, "master.init" );
			}
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		super.onLayout( changed, l, t, r, b );
		if ( !isInEditMode() ) {
			BLog.d( "Master: onLayout %s", changed );
		}
	}
}
