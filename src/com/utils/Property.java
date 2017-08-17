package com.utils;

/**
 * Created by Islam-uddin on 4/10/2017.
 */
public class Property {
    private String partNum;
    private Integer prevQty;
    private Integer currentQty;

    public Property(){

    }
    public Property(String partNum,Integer prevQty,Integer currentQty){
        this.partNum=partNum;
        this.prevQty=prevQty;
        this.currentQty=currentQty;
    }

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public Integer getPrevQty() {
        return prevQty;
    }

    public void setPrevQty(Integer prevQty) {
        this.prevQty = prevQty;
    }

    public Integer getCurrentQty() {
        return currentQty;
    }

    public void setCurrentQty(Integer currentQty) {
        this.currentQty = currentQty;
    }
}
