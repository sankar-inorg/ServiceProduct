package com.inorg.services.product.service;

import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductProjection;
import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;

public interface ProductService {
    public Product getProductById(String productId);
    public Product getProductByKey(String productKey);
    ProductProjection getProductProjectionById(String productId);

    ProductProjectionPagedQueryResponse getProductProjectionByQuery();
}
