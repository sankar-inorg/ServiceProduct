package com.inorg.services.product.controller;

import com.commercetools.api.models.product.*;
import com.inorg.services.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/details-by-key/{productKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product getProductByKey(@PathVariable String productKey) {
        LOG.info("Get Product Details  for Product Key : {}", productKey);
        return productService.getProductByKey(productKey);
    }

    @GetMapping(value = "/projection/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjection getProductProjectionById(@PathVariable String productId) {
        LOG.info("Get Product Projection  for Product : {}", productId);
        return productService.getProductProjectionById(productId);
    }

    @GetMapping(value = "/projection-query", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedQueryResponse getProductProjectionQuery() {
        LOG.info("Get Product Projection  Query");
        return productService.getProductProjectionByQuery();
    }

    @PostMapping(value = "/createProducts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> createProducts() {
        LOG.info("Create product or Update product");
        return productService.createProducts();
    }

    @GetMapping(value = "/getProductsByCategory/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPagedQueryResponse getProductsByCategory(@PathVariable String categoryId) {
        LOG.info("Get Product by Category");
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping(value = "/searchProducts/{searchText}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedSearchResponse searchProducts(@PathVariable String searchText) {
        LOG.info("get Product by search text");
        return productService.searchProducts(searchText);
    }
    @GetMapping(value = "/getProductBySKU/{sku}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedQueryResponse getProductBySKU(@PathVariable String sku) {
        LOG.info("Get Product Projection Query using sku : {}", sku);
        return productService.getProductBySKU(sku);
    }
    @PostMapping(value = "/updateProductPriceBySKU/{sku}/{price}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product updateProductPriceBySKU(@PathVariable String sku, @PathVariable String price) {
        LOG.info("Get Product Projection Query using sku : {}", sku);
        return productService.updateProductPriceWithSKU(sku,price);
    }
    @PostMapping(value = "/updateProductPriceBySKUUsingCSV", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> updateProductPriceBySKUUsingCSV() {
        LOG.info("Get Product Projection Query using sku :  using CSV{}");
        return productService.updateProductPriceBySKUUsingCSV();
    }

    //TODO Add more endpoints to fetch product details
}
