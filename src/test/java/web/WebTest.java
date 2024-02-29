package web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import web.components.Product;
import web.enums.CheckOutInfo;
import web.enums.SortingOptions;
import web.pages.CheckOutPage;
import web.pages.LoginPage;
import web.pages.ProductsPage;
import web.pages.ShoppingCartPage;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.testng.Assert.*;

public class WebTest {

    protected WebDriver driver;

    @Test
    public void loginTest() {
        ProductsPage productsPage = login();
        assertEquals(productsPage.getTitle(), "Products", "Titles are not equal");
        List<Product> products = productsPage.getProducts();
        assertEquals(6, products.size(), "Product sizes are not equal");
        Product product = products.get(0);
        assertTrue(product.getNameLabelText().matches("[a-zA-Z]+\\s[a-zA-Z]+\\s[a-zA-Z]+"), "Name does not match");
        int descLength = product.getDescriptionLabelText().length();
        assertTrue(descLength >= 134 && descLength <= 159, "Length does not match");
        assertTrue(product.getImageAttribute().matches("https:\\/\\/www\\.saucedemo\\.com\\/static\\/media\\/[a-z]+(-[a-z]+)(-1200x1500\\.)+([a-zA-Z0-9]){8}+\\.jpg"), "Image does not match");
        assertTrue(product.getPriceLabelText().matches("\\$[0-9]{1,2}.[0-9]{1,2}"), "Price does not match");
        assertEquals(product.getButtonLabelText(), "Add to cart", "");
        driver.quit();
    }

    @Test
    public void sortOptionsTest() {
        ProductsPage productsPage = login();
        var products = productsPage.getProducts();
        var pricesBefore = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        assertEquals(productsPage.getSortActiveOption(), SortingOptions.AZ.getName(), "Filters are not equal");
        List<String> sortingOptions = Stream.of(SortingOptions.values())
                .map(SortingOptions::getName)
                .collect(Collectors.toList());
        assertEquals(productsPage.getSortOptionNames(), sortingOptions, "Sort options are not equal");
        productsPage.selectSortOption(SortingOptions.LOWHIGH.getName());
        products = productsPage.getProducts();
        var pricesAfter = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        assertNotEquals(pricesBefore, pricesAfter, "Prices are equal");
        Collections.sort(pricesBefore);
        assertEquals(pricesBefore, pricesAfter, "Prices are not equal");
        assertEquals(productsPage.getSortActiveOption(), SortingOptions.LOWHIGH.getName(), "Filters are not equal");
        driver.quit();
    }

    @Test
    public void addToCartButtonTest() {
        ProductsPage productsPage = login();
        List<Product> productsList = productsPage.getProducts();
        Product product = getRandomProduct(productsList);
        assertEquals(product.getButtonLabelText(), "Add to cart", "Buttons are not equal");
        product.clickAddToCartButton();
        assertEquals(product.getButtonLabelText(), "Remove", "Buttons are not equal");
        product.clickRemoveButton();
        assertEquals(product.getButtonLabelText(), "Add to cart", "Buttons are not equal");
        driver.quit();
    }

    @Test
    public void userCanAddAndRemoveProductInCartTest() {
        ProductsPage productsPage = login();
        List<Product> productsList = productsPage.getProducts();
        Product product = getRandomProduct(productsList);
        String productName = product.getNameLabelText();
        String productDesc = product.getDescriptionLabelText();
        String productPrice = product.getPriceLabelText();
        product.clickAddToCartButton();
        ShoppingCartPage cart = productsPage.clickCartButton();
        assertEquals(productsPage.getTitle(), "Your Cart", "Titles are not equal");
        List<Product> productsCartList = cart.getProducts();
        assertEquals(productsCartList.size(), 1, "Products sizes are not equal");
        Product cartProduct = productsCartList.get(0);
        assertEquals(cartProduct.getNameLabelText(), productName, "Products names are not equal");
        assertEquals(cartProduct.getDescriptionLabelText(), productDesc, "Products descs are not equal");
        assertEquals(cartProduct.getPriceLabelText(), productPrice, "Products prices are not equal");
        cartProduct.clickRemoveButton();
        productsCartList = cart.getProducts();
        assertTrue(productsCartList.isEmpty(), "Products are present");
        driver.quit();
    }

    @Test
    public void checkOutButtonTest() {
        ProductsPage productsPage = login();
        List<Product> productsList = productsPage.getProducts();
        Product product = getRandomProduct(productsList);
        product.clickAddToCartButton();
        ShoppingCartPage cartPage = productsPage.clickCartButton();
        CheckOutPage checkOut = cartPage.clickCheckOutButton();
        assertEquals(productsPage.getTitle(), "Checkout: Your Information", "Titles are not equal");
        for (CheckOutInfo info : CheckOutInfo.values()) {
            assertTrue(checkOut.isInputPresent(info.getName()), "Input is not present" + info.getName());
        }
        assertTrue(checkOut.IsContinueButtonPresent(), "Button 'Continue' is not present");
        checkOut.clickCancelButton();
        productsPage = new ProductsPage(driver);
        assertEquals(productsPage.getTitle(), "Your Cart", "Titles are not equal");
        driver.quit();
    }

    @Test
    public void continueShoppingButtonTest() {
        ProductsPage productsPage = login();
        ShoppingCartPage cart = productsPage.clickCartButton();
        List<Product> productsCartList = cart.getProducts();
        assertTrue(productsCartList.isEmpty(), "Products sizes are present");
        cart.clickContinueShoppingButton();
        List<Product> productsList = productsPage.getProducts();
        assertEquals(6, productsList.size(), "Product sizes are not equal");
    }

    @Test
    public void continueShoppingButtonTest() {
      


    }

    public ProductsPage login() {
        String username = "standard_user";
        String password = "secret_sauce";
        Thread thread = new Thread(new Runner());
        thread.run();
        this.driver = new ChromeDriver();
        LoginPage login = new LoginPage(this.driver);
        login.loginAs(username, password);
        return new ProductsPage(this.driver);
    }
    public Product getRandomProduct(List<Product> productsList) {
        int randomProductIndex = new Random().nextInt(productsList.size());
        Product product = productsList.get(randomProductIndex);
        return product;
    }
}
