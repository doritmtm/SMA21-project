package com.staecorp.staefilemanager.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.staecorp.staefilemanager.thread.ScrollThread;

public class CustomRecyclerView extends RecyclerView {
    private boolean selectionMode=false;
    private boolean locked=false;
    private View currentChildInFocus;
    private ScrollThread scrollThread=new ScrollThread(this);
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
        if(locked)
        {
            return true;
        }
        else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
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
            locked=false;
            selectionMode=false;
            scrollThread.shouldStop();
        }
        if(locked)
        {
            return true;
        }
        else
        {
            return super.onTouchEvent(ev);
        }
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
        int coordY=Math.round(ev.getY()-getScrollY());
        if(-200<=coordY && coordY<=50)
        {
            startScrollThread();
            scrollThread.setScrollByY(-25);
        }
        else if(getHeight()-100<=coordY && coordY<=getHeight())
        {
            startScrollThread();
            scrollThread.setScrollByY(25);
        }
        else
        {
            scrollThread.setScrollByY(0);
            scrollThread.shouldStop();
        }
    }

    private void startScrollThread()
    {
        if(scrollThread.getState().equals(Thread.State.NEW))
        {
            scrollThread.start();
        }
        if(scrollThread.getState().equals(Thread.State.TERMINATED))
        {
            scrollThread=new ScrollThread(this);
            scrollThread.start();
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
