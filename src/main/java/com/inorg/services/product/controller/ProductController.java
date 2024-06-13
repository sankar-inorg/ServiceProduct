package com.inorg.services.product.controller;

import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductProjection;
import com.inorg.services.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(value = "/projection/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductProjection getProductProjectionById(@PathVariable String productId) {
        LOG.info("Get Product Projection  for Product : {}", productId);
        return productService.getProductProjectionById(productId);
    }

    //TODO Add more endpoints to fetch product details
}
