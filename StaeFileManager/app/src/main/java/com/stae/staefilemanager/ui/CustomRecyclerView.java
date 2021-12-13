package com.stae.staefilemanager.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {
    private boolean selectionMode=false;
    private View currentChildInFocus;
    private LockableNestedScrollView nestedScrollView;
    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("MYAPPP","Intercept touched!!!!!!");
        /*if(selectionMode)
        {
            View child=findChildHovered(ev);
            if(currentChildInFocus!=child)
            {
                if(child!=null)
                {
                    currentChildInFocus=child;
                    child.performClick();
                }
            }
        }*/
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d("MYAPPP","touched!!!!!!:"+ev.getX()+" "+ev.getY());
        if(selectionMode)
        {
            scrollIfUpOrDown(ev);
            View child=findChildHovered(ev);
            if(currentChildInFocus!=child)
            {
                if(child!=null)
                {
                    currentChildInFocus=child;
                    child.performClick();
                }
            }
        }
        if(ev.getAction()==MotionEvent.ACTION_UP)
        {
            currentChildInFocus=null;
        }
        return super.onTouchEvent(ev);
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    public View getCurrentChildInFocus() {
        return currentChildInFocus;
    }

    public void setCurrentChildInFocus(View currentChildInFocus) {
        this.currentChildInFocus = currentChildInFocus;
    }

    public LockableNestedScrollView getNestedScrollView() {
        return nestedScrollView;
    }

    public void setNestedScrollView(LockableNestedScrollView nestedScrollView) {
        this.nestedScrollView = nestedScrollView;
    }

    private View findChildHovered(MotionEvent ev)
    {
        View v;
        for(int i=0;i<getChildCount();i++)
        {
            v=getChildAt(i);
            if(v.getLeft()<=Math.round(ev.getX()) && Math.round(ev.getX())<=v.getLeft()+v.getWidth())
            {
                if(v.getTop()<=Math.round(ev.getY()) && Math.round(ev.getY())<=v.getTop()+v.getHeight())
                {
                    return getChildAt(i);
                }
            }
        }
        return getChildAt(0);
    }

    private void scrollIfUpOrDown(MotionEvent ev)
    {
        int coordY=Math.round(ev.getY()-nestedScrollView.getScrollY());
        if(-200<=coordY && coordY<=50)
        {
            nestedScrollView.scrollBy(0,-20);
            Log.d("MYAPPP","scrollBy(0,-10);");
        }
        if(nestedScrollView.getHeight()-100<=coordY && coordY<=nestedScrollView.getHeight())
        {
            nestedScrollView.scrollBy(0,20);
            Log.d("MYAPPP","scrollBy(0,10);");
        }
        Log.d("MYAPPP","Height:"+nestedScrollView.getHeight()+"Top:"+nestedScrollView.getTop());
    }
}
