package com.staecorp.staefilemanager.thread;

import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.ui.CustomRecyclerView;

public class ScrollThread extends Thread{
    private int scrollByY=0;
    private CustomRecyclerView customRecyclerView;
    private boolean shouldStop=false;

    public ScrollThread(CustomRecyclerView customRecyclerView) {
        this.customRecyclerView = customRecyclerView;
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
                customRecyclerView.scrollBy(0, scrollByY);
            });
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
