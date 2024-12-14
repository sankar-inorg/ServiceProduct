package com.inorg.services.product.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.category.CategoryResourceIdentifierBuilder;
import com.commercetools.api.models.common.*;
import com.commercetools.api.models.product.*;
import com.commercetools.api.models.product_type.ProductTypeResourceIdentifierBuilder;
import com.inorg.services.product.models.PriceData;
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
import java.util.concurrent.atomic.AtomicReference;

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
    public ProductProjectionPagedQueryResponse getProductProjectionByQuery(String color) {
    return apiRoot
        .productProjections()
        .get()
        .withWhere("masterVariant(attributes(name=\"colorlabel\" and value(en-GB=\""+color+"\")))")
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

                    ProductUpdateAction setDescription = ProductSetDescriptionActionBuilder.of()
                            .description(LocalizedStringBuilder.of().addValue("en-US", productData.getDescription()).build())
                            .build();

                    updateActions.add(setDescription);

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
//                    Attribute brandAttribute = Attribute.builder()
//                            .name("brand")
//                            .value(productData.getBrand())
//                            .build();
//                    varinatAttributes.add(brandAttribute);

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
                                            .dimensions(ImageDimensionsBuilder.of().w(100).h(100).build())
                                    .build()))
                            .build();

                    //Create Product
                    ProductDraft productDraft = ProductDraft.builder()
                            .productType(ProductTypeResourceIdentifierBuilder.of()
                                    .key(productData.getProductType())
                                    .build()
                            )
                            .key(productData.getKey())
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
       // return null;
        return  apiRoot.products().get()
                .addWhere("masterData(current(categories(id = \""+categoryId+"\")))")
                .executeBlocking().getBody();
    }

    @Override
    public ProductProjectionPagedSearchResponse searchProducts(String searchText) {
        //return null;
         return apiRoot.productProjections().search()
                 .get()
                 .withText("EN-US", searchText)
                 .withFacet("variants.attributes.Color")
                 .withFacet("variants.attributes.Size")
                .executeBlocking().getBody();
    }

    @Override
    public ProductProjectionPagedQueryResponse getProductBySKU(String sku) {
        return apiRoot.productProjections()
                .get()
                .addWhere("masterVariant(sku=\""+sku+"\") or variants(sku=\""+sku+"\")")
                .executeBlocking()
                .getBody();
    }

    @Override
    public Product updateProductPriceWithSKU(String sku, String price) {

        Product productToBeUpdate = null;
        ProductProjectionPagedQueryResponse product = null;
        product =  getProductBySKU(sku);
        if(product.getResults().isEmpty()){
            System.out.println("No Product found ");
        } else {
            System.out.println("Product found");
            AtomicReference<String> priceId = new AtomicReference<>();
            if(product.getResults().get(0).getMasterVariant().getSku().equals(sku)) {
                List<Price> masterVariantPrices = product.getResults().get(0).getMasterVariant().getPrices();
                masterVariantPrices.forEach(prices -> {
                    if (prices.getValue().getCurrencyCode().equals("USD")) {
                        priceId.set(prices.getId());
                        System.out.println("Product found with USD price and a master variant");
                    }
                });
            }
            if(priceId.get() == null){
                List<ProductVariant> variants = product.getResults().get(0).getVariants();
                variants.forEach(variant -> {
                    if(variant.getSku().equals(sku)) {
                        variant.getPrices().forEach(prices -> {
                            if (prices.getValue().getCurrencyCode().equals("USD")){
                                priceId.set(prices.getId());
                                System.out.println("Product found with USD price and a variant not master");
                            }
                        });
                    }

                });
            }
            if(priceId.get() == null){
                System.out.println("Product found but no price with usd found");
            } else {
                List<ProductUpdateAction> updateActions = new ArrayList<>();
                ProductUpdateAction setPriceOfProduct = ProductChangePriceActionBuilder.of()
                        .priceId(priceId.get())
                        .price(PriceDraftBuilder.of()
                                .value(MoneyBuilder.of().currencyCode("USD")
                                        .centAmount(Long.parseLong(price)).build()).build()).build();

                updateActions.add(setPriceOfProduct);

                ProductUpdate productUpdate = ProductUpdate.builder()
                        .version(product.getResults().get(0).getVersion())
                        .actions(updateActions)
                        .build();
                productToBeUpdate =   apiRoot.products()
                        .withId(product.getResults().get(0).getId())
                        .post(productUpdate)
                        .executeBlocking()
                        .getBody();


            }
        }

        return productToBeUpdate;
    }

    @Override
    public List<Product> updateProductPriceBySKUUsingCSV() {
        List<Product> listOfProduct = new ArrayList<>();
        List<PriceData> priceDataList = new ArrayList<>();
        try{
            priceDataList = readPriceDataFromCSV();
        }catch (Exception e){
            LOG.error("Error reading product data", e);
        }

        priceDataList.forEach(priceData -> {
            Product updateTheProduct = updateProductPriceWithSKU(priceData.getSku(),priceData.getPrice());
            listOfProduct.add(updateTheProduct);
        });

        return listOfProduct;
    }

//    @Override
//    public List<Product> updateProductPrices() {
//        List<Product> products = new ArrayList<>();
//        return products;
//    }
//
//    private List<PriceData> readPriceData() throws IOException,CsvValidationException{
//        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass()
//                .getClassLoader()
//                .getResourceAsStream("pricedata.csv")));
//
//        List<PriceData> productPricesList = new ArrayList<>();
//
//        return productPricesList;
//    }



    private List<ProductData> readProductData() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass()
            .getClassLoader()
            .getResourceAsStream("product.csv")));
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
                    .size(Integer.parseInt(record[11]))
                    .color(record[12])
                    .details(record[13])
                    .style(record[14])
                    .build();
            productDataList.add(productData);
        }

        return productDataList;
    }

    private List<PriceData> readPriceDataFromCSV() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("priceData.csv")));
        List<PriceData> priceDataList = new ArrayList<>();

        // read line by line
        String[] record = null;
        reader.skip(1);//header
        while ((record = reader.readNext()) != null) {
            PriceData productData = PriceData.builder()
                    .sku(record[0])
                    .price(record[1])
                    .build();
            priceDataList.add(productData);
        }

        return priceDataList;
    }
}
