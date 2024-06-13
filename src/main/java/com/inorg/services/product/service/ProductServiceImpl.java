package com.inorg.services.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductProjection;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProjectApiRoot apiRoot;

    public ProductServiceImpl(ProjectApiRoot projectApiRoot) {
        this.apiRoot = projectApiRoot;
    }


    @Override
    public Product getProductById(String productId) {

        return apiRoot.products()
                .withId(productId)
                .get()
                .executeBlocking()
                .getBody();
    }

    @Override
    public ProductProjection getProductProjectionById(String productId) {
        return apiRoot.productProjections()
                .withId(productId)
                .get()
                .executeBlocking()
                .getBody();
    }
}
