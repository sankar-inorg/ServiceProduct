package com.inorg.services.product.service;

import com.commercetools.api.models.product.*;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    public Product getProductById(String productId);
    public Product getProductByKey(String productKey);
    ProductProjection getProductProjectionById(String productId);

    ProductProjectionPagedQueryResponse getProductProjectionByQuery();

    List<Product> createProducts();

    ProductPagedQueryResponse getProductsByCategory(String categoryId);

    ProductProjectionPagedSearchResponse searchProducts(String searchText);

    List<Product> Updatebysku() throws CsvValidationException, IOException;
}
