package com.inorg.services.product.controller;

import com.commercetools.api.models.product.*;
import com.inorg.services.product.service.ProductService;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    public ProductProjectionPagedQueryResponse getProductProjectionById() {
        LOG.info("Get Product Projection  Query");
        return productService.getProductProjectionByQuery();
    }

    @GetMapping(value = "create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void CreateProduct(){
        LOG.info("Creating products");
        productService.createProducts();
    }

    @GetMapping(value = "getbycategory/{cat}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPagedQueryResponse getbycategory(@PathVariable String cat){
        LOG.info("Getting by category {}",cat);
        return productService.getProductsByCategory(cat.strip());
    }

    @GetMapping(value = "search/{text}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjectionPagedSearchResponse getbytext(@PathVariable String text){
        LOG.info("Getting by text {}",text);
        return productService.searchProducts(text.strip());
    }

    @GetMapping(value = "updatebycsv", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> UpdateBySku() throws CsvValidationException, IOException {
        LOG.info("Updating by SKU");
        return productService.Updatebysku();
    }
}
