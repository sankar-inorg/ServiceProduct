package com.inorg.services.product.service;

import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductProjection;

public interface ProductService {
    public Product getProductById(String productId);

    ProductProjection getProductProjectionById(String productId);
}
