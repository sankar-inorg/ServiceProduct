package com.inorg.services.product.controller;

import com.commercetools.api.models.product.*;
import com.inorg.services.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
@RequestMapping(path = "/product", produces = APPLICATION_JSON_VALUE)
public class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/details/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product getProductById(@PathVariable String productId) {
        LOG.info("Get Product Details  for Product : {}", productId);
        return productService.getProductById(productId);
    }

    @GetMapping(value = "/details/key/{productKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product getProductByKey(@PathVariable String productKey) {
        LOG.info("Get Product Details  for Product using Key : {}", productKey);
        return productService.getProductByKey(productKey);
    }

    @GetMapping(value = "/projection/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjection getProductProjectionById(@PathVariable String productId) {
        LOG.info("Get Product Projection  for Product : {}", productId);
        return productService.getProductProjectionById(productId);
    }

    @GetMapping(value = "/projection-query", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedQueryResponse getProductProjectionByQuery() {
        LOG.info("Get Product Projection Query");
        return productService.getProductProjectionByQuery();
    }

    @PostMapping(value = "/createProducts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> createProducts() {
        LOG.info("Create product or Update Product");
        return productService.createProducts();
    }

    @GetMapping(value = "/getProductsByCategory/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPagedQueryResponse getProductsByCategory(@PathVariable String categoryId) {
        LOG.info("Get Product by Category");
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping(value = "/searchProducts/{searchText}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedSearchResponse searchProducts(@PathVariable String searchText) {
        LOG.info("Get Product by Search Text");
        return productService.searchProducts(searchText);
    }

    //TODO Add more endpoints to fetch product details
}
