package com.example.androidtrlts.Model;


import com.example.androidtrlts.Adapters.ItemAdapter;

public class Item {
    private String name;
    private Long dateModified;
    private ItemAdapter.Types type;
    private long child;
    private String extension;
    private boolean isVisible = true;
    private int imageResource = -1;

    public Item(String name, Long modified, ItemAdapter.Types type, String extension){
        this.name = name;
        this.dateModified = modified;
        this.type = type;
        this.extension = extension;
        this.child = 0;
    }

    public Item(String name, Long modified, ItemAdapter.Types type, long count){
        this.name = name;
        this.dateModified = modified;
        this.type = type;
        this.extension = "";
        this.child = count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    public void setType(ItemAdapter.Types type) {
        this.type = type;
    }

    public long getChild() {
        return child;
    }

    public void setChild(long child) {
        this.child = child;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getChildCount(){
        return child;
    }

    public String getName(){
        return name;
    }

    public Long getDateModified(){
        return dateModified;
    }

    public ItemAdapter.Types getType(){
        return type;
    }

    public String getExtension(){return extension; }

    public void setVisible(boolean flag){
        isVisible = flag;
    }

    public boolean isVisible(){
        return isVisible;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
