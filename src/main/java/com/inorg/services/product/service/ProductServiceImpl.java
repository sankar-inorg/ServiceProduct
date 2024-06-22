package com.inorg.services.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.category.CategoryResourceIdentifier;
import com.commercetools.api.models.category.CategoryResourceIdentifierBuilder;
import com.commercetools.api.models.common.ImageBuilder;
import com.commercetools.api.models.common.ImageDimensions;
import com.commercetools.api.models.common.ImageDimensionsBuilder;
import com.commercetools.api.models.common.LocalizedStringBuilder;
import com.commercetools.api.models.common.MoneyBuilder;
import com.commercetools.api.models.common.PriceDraftBuilder;
import com.commercetools.api.models.product.Attribute;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductDraft;
import com.commercetools.api.models.product.ProductPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjection;
import com.commercetools.api.models.product.ProductProjectionPagedQueryResponse;
import com.commercetools.api.models.product.ProductProjectionPagedSearchResponse;
import com.commercetools.api.models.product.ProductPublishActionBuilder;
import com.commercetools.api.models.product.ProductSetDescriptionAction;
import com.commercetools.api.models.product.ProductSetDescriptionActionBuilder;
import com.commercetools.api.models.product.ProductUpdate;
import com.commercetools.api.models.product.ProductUpdateAction;
import com.commercetools.api.models.product.ProductVariantDraft;
import com.commercetools.api.models.product_type.ProductTypeResourceIdentifier;
import com.commercetools.api.models.product_type.ProductTypeResourceIdentifierBuilder;
import com.inorg.services.product.models.ProductData;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public Product getProductByKey(String productKey) {
        return apiRoot.products()
                .withKey(productKey)
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

    @Override
    public ProductProjectionPagedQueryResponse getProductProjectionByQuery() {
        return apiRoot
        .productProjections()
        .get()
        .withWhere("masterVariant(attributes(name=\"colorlabel\" and value(en-GB=\"Steel Gray\")))")
        .withPriceCurrency("GBP")
            .withPriceCountry("GB")
        .executeBlocking()
        .getBody();
    }

    @Override
    public List<Product> createProducts() {
        List<Product> products = new ArrayList<>();
        List<ProductData> productDataList = new ArrayList<>();
        try{
            productDataList =readProductData();
        }catch (Exception e){
            LOG.error("Error reading product data", e);
        }

        productDataList.forEach(productData -> {
            Product product = null;
            try {
                product = apiRoot.products()
                        .withKey(productData.getKey())
                        .get()
                        .executeBlocking()
                        .getBody();
                } catch (Exception e) {
                    LOG.error("Product not found", e);
                }

                if(product != null){
                    //Update Product
                    List<ProductUpdateAction> updateActions = new ArrayList<>();
                    //TODO Create List of Update Actions
                    updateActions.add(
                            ProductSetDescriptionActionBuilder.of()
                                    .description(LocalizedStringBuilder.of()
                                            .addValue("de-DE", productData.getDescription())
                                            .build())
                                    .build()
                    );
                    updateActions.add(
                            ProductPublishActionBuilder.of()
                                    .build()
                    );

                    ProductUpdate productUpdate = ProductUpdate.builder()
                            .version(product.getVersion())
                            .actions(updateActions)
                            .build();
                    product =   apiRoot.products()
                            .withId(product.getId())
                            .post(productUpdate)
                            .executeBlocking()
                            .getBody();
                    products.add(product);
                } else {

                    List<Attribute> varinatAttributes = new ArrayList<>();

                    Attribute sizeAttribute = Attribute.builder()
                            .name("shirt-size")
                            .value(productData.getSize())
                            .build();
                    varinatAttributes.add(sizeAttribute);
                    Attribute colorAttribute = Attribute.builder()
                            .name("shirt-color")
                            .value(productData.getColor())
                            .build();
                    varinatAttributes.add(colorAttribute);
                    Attribute brandAttribute = Attribute.builder()
                            .name("brand")
                            .value(productData.getBrand())
                            .build();
                    varinatAttributes.add(brandAttribute);

                    ProductVariantDraft masterVariant = ProductVariantDraft.builder()
                            .sku(productData.getSku())
                            .key(productData.getVariantId())
                            .prices(PriceDraftBuilder.of()
                                    .value(MoneyBuilder.of()
                                            .currencyCode("USD")
                                            .centAmount(new Long(productData.getPrice()))
                                            .build())
                                    .build())
                            .attributes(varinatAttributes)
                            .images(Arrays.asList(ImageBuilder.of()
                                        .url(productData.getImages().get(0))
                                            .dimensions(ImageDimensionsBuilder.of()
                                                    .w(100)
                                                    .h(100)
                                                    .build())
                                        .build()))
                            .build();

                    //Create Product
                    ProductDraft productDraft = ProductDraft.builder()
                            .key(productData.getKey())
                            .productType(ProductTypeResourceIdentifierBuilder.of()
                                    .key(productData.getProductType())
                                    .build()
                            )
                            .categories(CategoryResourceIdentifierBuilder.of()
                                    .key(productData.getCategories().get(0))
                                    .build()
                            )
                            .name(LocalizedStringBuilder.of()
                                    .addValue("EN-US", productData.getName())
                                .build())
                            .slug(LocalizedStringBuilder.of()
                                    .addValue("EN-US", productData.getSlug())
                                    .build())
                            .masterVariant(masterVariant)
                            .description(LocalizedStringBuilder.of()
                                    .addValue("EN-US", productData.getDescription())
                                    .build())
                            .build();
                    product = apiRoot.products()
                            .post(productDraft)
                            .executeBlocking()
                            .getBody();
                }
                products.add(product);

        });

        return products;
    }

    @Override
    public ProductPagedQueryResponse getProductsByCategory(String categoryId) {
        return apiRoot.products().get()
                .executeBlocking().getBody();
    }

    @Override
    public ProductProjectionPagedSearchResponse searchProducts(String searchText) {
        return apiRoot.productProjections().search().get()
                .withText("en",searchText)
                .withFacet("variants.attributes.color:\""+searchText+"\"")
                .withFacet("variants.attributes.Size:\"" + searchText + "\"")
                .executeBlocking().getBody();
    }


    private List<ProductData> readProductData() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new InputStreamReader(Objects.requireNonNull(this
                .getClass()
                .getClassLoader()
                .getResourceAsStream("product.csv"))));
        List<ProductData> productDataList = new ArrayList<>();

        // read line by line
        String[] record = null;
        reader.skip(1);//header
        while ((record = reader.readNext()) != null) {
          ProductData productData =
              ProductData.builder()
                  .productType(record[0])
                  .key(record[1])
                  .variantId(record[2])
                  .sku(record[3])
                  .price(Long.valueOf(record[4]))
                  .tax(record[5])
                  .categories(Arrays.asList(record[6].split(",")))
                  .images(Arrays.asList(record[7].split(",")))
                  .name(record[8])
                  .description(record[9])
                  .slug(record[10])
                  .size(Integer.parseInt(record[11]))
                  .color(record[12])
                  .details(record[13])
                  .style(record[14])
                  .brand(record[15])
                  .build();
          productDataList.add(productData);
    }

        return productDataList;
    }
}
