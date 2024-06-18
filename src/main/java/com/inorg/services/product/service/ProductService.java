package com.inorg.services.product.service;

import com.commercetools.api.models.product.*;

import java.util.List;

public interface ProductService {

    public Product getProductById(String productId);

    public Product getProductByKey(String productKey);

    public ProductProjection getProductProjectionById(String productId);

    public ProductProjectionPagedQueryResponse getProductProjectionByQuery();

    public List<Product> createProducts();

    public ProductPagedQueryResponse getProductsByCategory(String categoryId);

    public ProductProjectionPagedSearchResponse searchProducts(String searchText);
}
