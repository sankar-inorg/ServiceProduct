package com.inorg.services.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.category.CategoryResourceIdentifierBuilder;
import com.commercetools.api.models.common.*;
import com.commercetools.api.models.product.*;
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
import java.util.*;

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

                ProductUpdateAction Desc = ProductSetDescriptionActionBuilder.of().
                        description(LocalizedString.of(Locale.ENGLISH,productData.getDescription())).build();
                updateActions.add(Desc);

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
                        .name("size")
                        .value(productData.getSize())
                        .build();
                varinatAttributes.add(sizeAttribute);
                Attribute colorAttribute = Attribute.builder()
                        .name("color")
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
                                        .currencyCode("INR")
                                        .centAmount(Long.valueOf(productData.getPrice()))
                                        .build())
                                .build())
                        .attributes(varinatAttributes)
                        .images(Arrays.asList(ImageBuilder.of()
                                .url(productData.getImages().get(0))
                                        .dimensions(imageDimensionsBuilder -> new ImageDimensionsBuilder().h(100).w(100)
                                        )
                                .build()))
                        .build();

                //Create Product
                ProductDraft productDraft = ProductDraft.builder()
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
        return apiRoot.products()
                .get()
                .withWhere(String.format("masterData(current(categories(id = \"%s\")))",categoryId))
                .executeBlocking()
                .getBody();
    }

    @Override
    public ProductProjectionPagedSearchResponse searchProducts(String searchText) {
        return apiRoot.productProjections()
                .search()
                .get()
                .withText("EN-US",searchText)
                .executeBlocking()
                .getBody();
    }

    @Override
    public List<Product> Updatebysku() throws CsvValidationException, IOException {
        List<Product> UpdatedProductList = new ArrayList<>();
        List<ProductData> productDataList = readProductData();
        productDataList.forEach(productData -> {
            ProductProjectionPagedQueryResponse Product = getProductBySKU(productData.getSku());
            if(Product!=null){
                ThreadLocal<String> priceId = new ThreadLocal<>();
                if(Product.getResults().get(0).getMasterVariant().getSku().equals(productData.getSku())){
                    List<Price> masterVariantPrices = Product.getResults().get(0).getMasterVariant().getPrices();
                    masterVariantPrices.forEach(prices -> {
                        if (prices.getValue().getCurrencyCode().equals("INR")) {
                            priceId.set(prices.getId());
                        }
                    });
                } else {
                    List<ProductVariant> Variants = Product.getResults().get(0).getVariants();
                    Variants.forEach(Variant -> {
                        if(Variant.getSku().equals(productData.getSku())){
                            Variant.getPrices().forEach(prices -> {
                                if(prices.getValue().getCurrencyCode().equals("INR")){
                                    priceId.set(prices.getId());
                                }
                            });
                        }
                    });
                }

                List<ProductUpdateAction> updateActions = new ArrayList<>();
                ProductUpdateAction setPrice = ProductChangePriceActionBuilder.of()
                        .priceId(priceId.get())
                        .price(PriceDraftBuilder.of()
                                .value(MoneyBuilder.of()
                                        .currencyCode("INR")
                                        .centAmount(Long.parseLong(productData.getPrice())
                                        ).build()
                                ).build())
                        .build();
                updateActions.add(setPrice);
                ProductUpdate update = ProductUpdateBuilder.of()
                        .version(Product.getResults().get(0).getVersion())
                        .actions(updateActions)
                        .build();
                Product updatedProduct = apiRoot.products()
                        .withId(Product.getResults().get(0).getId())
                        .post(update)
                        .executeBlocking()
                        .getBody();
                UpdatedProductList.add(updatedProduct);
            }
        });
        return UpdatedProductList;
    }

    public ProductProjectionPagedQueryResponse getProductBySKU(String sku) {
        ProductProjectionPagedQueryResponse SearchProduct = apiRoot.productProjections()
                .get()
                .executeBlocking()
                .getBody();
        LOG.info(SearchProduct.toString());
        return SearchProduct;
    }

    public List<ProductData> readpricecsv() throws IOException, CsvValidationException{
        CSVReader reader = new CSVReader(new InputStreamReader(Objects.requireNonNull(this.getClass()
                .getClassLoader()
                .getResourceAsStream("product_pricing.csv"))));
        List<ProductData> productDataList = new ArrayList<>();

        String[] record = null;
        reader.skip(1);//header
        while ((record = reader.readNext()) != null) {
            ProductData productData = ProductData.builder()
                    .sku(record[0])
                    .price(record[1])
                    .build();
            System.out.print(productData);
            productDataList.add(productData);
        }
        return productDataList;
    }



    private List<ProductData> readProductData() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new InputStreamReader(Objects.requireNonNull(this.getClass()
                .getClassLoader()
                .getResourceAsStream("product.csv"))));
        List<ProductData> productDataList = new ArrayList<>();

        // read line by line
        String[] record = null;
        reader.skip(1);//header
        while ((record = reader.readNext()) != null) {
            ProductData productData = ProductData.builder()
                    .productType(record[0])
                    .key(record[1])
                    .variantId(record[2])
                    .sku(record[3])
                    .price(record[4])
                    .tax(record[5])
                    .categories(Arrays.asList(record[6].split(",")))
                    .images(Arrays.asList(record[7].split(",")))
                    .name(record[8])
                    .description(record[9])
                    .slug(record[10])
                    .size(record[11])
                    .color(record[12])
                    .details(record[13])
                    .style((record[14]))
                    .brand(record[15])
                    .build();
            productDataList.add(productData);
        }

        return productDataList;
    }
}
