package com.example.pharmacybackend.dto.invoice;

import java.util.List;

public class CreateInvoiceRequest {

    private Long pharmacyId;
    private String cashierName;
    private String customerName;
    private String customerPhone;

    private List<InvoiceItemReq> items;

    private Double subtotal;
    private Double taxAmount;
    private Double discountAmount;
    private Double totalAmount;

    public CreateInvoiceRequest() {}

    public Long getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<InvoiceItemReq> getItems() { return items; }
    public void setItems(List<InvoiceItemReq> items) { this.items = items; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}

