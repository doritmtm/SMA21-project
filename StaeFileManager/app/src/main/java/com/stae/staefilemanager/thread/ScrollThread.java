package com.stae.staefilemanager.thread;

import com.stae.staefilemanager.AppState;
import com.stae.staefilemanager.ui.LockableNestedScrollView;

public class ScrollThread extends Thread{
    private int scrollByY=0;
    private LockableNestedScrollView nestedScrollView;
    private boolean shouldStop=false;

    public ScrollThread(LockableNestedScrollView nestedScrollView) {
        this.nestedScrollView = nestedScrollView;
    }

    public int getScrollByY() {
        return scrollByY;
    }

    public void setScrollByY(int scrollByY) {
        this.scrollByY = scrollByY;
    }

    public void shouldStop() {
        shouldStop=true;
    }

    @Override
    public void run() {
        super.run();
        while(!shouldStop)
        {
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                nestedScrollView.scrollBy(0, scrollByY);
            });
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
