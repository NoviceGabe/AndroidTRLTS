package com.example.androidtrlts.Utils;

public class DialogItem {
    private String itemName;
    private String itemType;


    public DialogItem(String name, String type){
        this.itemName = name;
        this.itemType = type;
    }

    public DialogItem() {
    }
    public String getItemName(){
        return this.itemName;
    }

    public String getItemType(){
        return this.itemType;
    }

}
