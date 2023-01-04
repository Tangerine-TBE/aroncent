package com.aroncent.module.mine;

public class MenuBean {
    private int type;
    private String title;
    private int rightIcon;
    private boolean showLine = true;

    public MenuBean(int type,String title) {
        this.title = title;
        this.type = type;
    }

    public MenuBean(int type,  String title, boolean showLine) {
        this.type = type;
        this.title = title;
        this.showLine = showLine;
    }
    public boolean isShowLine() {
        return showLine;
    }

    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }


    public int getRightIcon() {
        return rightIcon;
    }

    public void setRightIcon(int rightIcon) {
        this.rightIcon = rightIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
