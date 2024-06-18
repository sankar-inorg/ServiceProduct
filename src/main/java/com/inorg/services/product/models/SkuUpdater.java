package com.inorg.services.product.models;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkuUpdater {
    private String sku;
    private String price;
}
