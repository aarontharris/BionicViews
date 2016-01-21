package com.aarontharris.bionicviewsdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class AgnosticView extends LinearLayout {
	public AgnosticView( Context context ) {
		super( context );
		init();
	}

	public AgnosticView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init();
	}

	public AgnosticView( Context context, AttributeSet attrs, int defStyleAttr ) {
		super( context, attrs, defStyleAttr );
		init();
	}

	private void init() throws RuntimeException {
		try {
			LayoutInflater.from( getContext() ).inflate( R.layout.merge_agnosticview, this, true );
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}
}
