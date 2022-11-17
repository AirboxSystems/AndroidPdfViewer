package com.github.barteksc.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;

public class AbxPdfView extends PDFView {
    private  OnTouchListenerWrapper mOnTouchListenerWrapper;

    /**
     * Construct the initial view
     *
     * @param context
     * @param set
     */
    public AbxPdfView(Context context, AttributeSet set) {
        super(context, set);
    }

    public void setOnTouchInterceptor(OnTouchListener listener) {
        ensureOnTouchWrapperReady();
        mOnTouchListenerWrapper.setInterceptor(listener);
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        if (listener == null) {
            super.setOnTouchListener(null);
        }
        else {
            ensureOnTouchWrapperReady();
            mOnTouchListenerWrapper.setListener(listener);
            super.setOnTouchListener(mOnTouchListenerWrapper);
        }
    }

    private  void ensureOnTouchWrapperReady() {
        if (mOnTouchListenerWrapper == null)
            mOnTouchListenerWrapper = new OnTouchListenerWrapper();
    }

    private  class OnTouchListenerWrapper implements OnTouchListener {
        private  OnTouchListener mListener;
        private  OnTouchListener mInterceptor;

        public  void setListener(OnTouchListener listener) {
            mListener = listener;
        }

        public void setInterceptor(OnTouchListener interceptor) {
            mInterceptor = interceptor;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mListener == null && mInterceptor == null) {
                return false;
            }
            else if (mInterceptor == null) {
                return  mListener.onTouch(view, motionEvent);
            }
            else if (mInterceptor.onTouch(view, motionEvent)) {
                return  true;
            }
            else {
                return  mListener.onTouch(view, motionEvent);
            }
        }
    }
}
