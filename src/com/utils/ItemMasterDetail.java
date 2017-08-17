package com.utils;

/**
 * Created by Islam-uddin on 5/10/2017.
 */
public class ItemMasterDetail {
    String item;
    String brand;
    String vendorPartNumber;
    String trQty;
    String wpsQty;
    String puQty;
    Integer qtySum;

    @Override
    public String toString() {
        return item+comma()+brand+comma()+getVendorPartNumber()+comma()+trQty+comma()+wpsQty+comma()+puQty;
    }

    public String comma(){
        return ",";
    }



    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVendorPartNumber() {
        return vendorPartNumber;
    }

    public void setVendorPartNumber(String vendorPartNumber) {
        this.vendorPartNumber = vendorPartNumber;
    }

    public String getTrQty() {
        return trQty;
    }

    public void setTrQty(String trQty) {
        this.trQty = trQty;
    }

    public String getWpsQty() {
        return wpsQty;
    }

    public void setWpsQty(String wpsQty) {
        this.wpsQty = wpsQty;
    }

    public String getPuQty() {
        return puQty;
    }

    public void setPuQty(String puQty) {
        this.puQty = puQty;
    }

    public Integer getQtySum() {
        return qtySum;
    }

    public void setQtySum(Integer qtySum) {
        this.qtySum = qtySum;
    }

}
