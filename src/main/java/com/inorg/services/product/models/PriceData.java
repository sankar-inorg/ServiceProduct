package com.inorg.services.product.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceData {

    private String sku;
    private String price;
}
