package com.aarontharris.bionicviewsdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.aarontharris.bionicviews.BLog;
import com.aarontharris.bionicviews.Bionic;
import com.aarontharris.bionicviews.Bionic.Meta;

public class InputView extends LinearLayout {
	private Button mInput = null;
	private int counter = 0;

	public InputView( Context context ) {
		super( context );
		init();
	}

	public InputView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init();
	}

	public InputView( Context context, AttributeSet attrs, int defStyleAttr ) {
		super( context, attrs, defStyleAttr );
		init();
	}

	private void init() throws RuntimeException {
		try {
			LayoutInflater.from( getContext() ).inflate( R.layout.merge_inputview, this, true );

			if ( !isInEditMode() ) {
				mInput = (Button) findViewById( R.id.inputview_input_button );
				mInput.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick( View v ) {
						BLog.d( "Input: click" );
						Meta meta = Bionic.getInstance().attainMeta( InputView.this );
						counter++;
						meta.putString( MasterView.testkey1, "input.click " + counter ); // deliberately overwrite, hacky but serves our test
					}
				} );
			}
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}
}
