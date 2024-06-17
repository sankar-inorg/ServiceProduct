package com.inorg.services.product.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductData {

    private String productType;
    private String key;
    private String variantId;
    private String sku;
    private String price;
    private String tax;
    private List<String> categories;
    private List<String> images;
    private String name;
    private String description;
    private String slug;
    private String size;
    private String color;
    private String details;
    private String style;
    private String gender;

}
