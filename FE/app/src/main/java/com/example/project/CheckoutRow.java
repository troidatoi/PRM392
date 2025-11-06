package com.example.project;

public class CheckoutRow {
    public enum Type { HEADER, ITEM }

    private Type type;
    private String storeId;
    private String storeName;
    private Double distanceKm; // nullable
    private Long shippingFee;  // nullable
    private Long storeTotal;   // Tổng tiền sản phẩm của store (nullable, for header)
    private CartItem item;     // when type == ITEM

    public static CheckoutRow header(String storeId, String storeName) {
        CheckoutRow r = new CheckoutRow();
        r.type = Type.HEADER;
        r.storeId = storeId;
        r.storeName = storeName;
        return r;
    }
    
    public void setStoreTotal(Long storeTotal) { this.storeTotal = storeTotal; }
    public Long getStoreTotal() { return storeTotal; }

    public static CheckoutRow item(CartItem item) {
        CheckoutRow r = new CheckoutRow();
        r.type = Type.ITEM;
        r.item = item;
        r.storeId = item != null ? item.getStoreId() : null;
        return r;
    }

    public Type getType() { return type; }
    public String getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public void setShipping(Double distanceKm, Long shippingFee) { this.distanceKm = distanceKm; this.shippingFee = shippingFee; }
    public Double getDistanceKm() { return distanceKm; }
    public Long getShippingFee() { return shippingFee; }
    public CartItem getItem() { return item; }
}


