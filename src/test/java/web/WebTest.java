package web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class WebTest {

    protected WebDriver driver;

    @Test
    public void checkLogin() {
        ProductsPage productsPage = login(driver);
        assertEquals(productsPage.getTitle(), "Products", "Titles are not equal");
        List<Product> products = productsPage.getProducts();
        assertEquals(6, products.size(), "Product sizes are not equal");
        Product product = products.get(0);
        assertTrue(product.getName().matches("[a-zA-Z]+\\s[a-zA-Z]+\\s[a-zA-Z]+"), "Name does not match");
        int descLength = product.getDescription().length();
        assertTrue(descLength >= 134 && descLength <= 159, "Length does not match");
        assertTrue(product.getImage().matches("https:\\/\\/www\\.saucedemo\\.com\\/static\\/media\\/[a-z]+(-[a-z]+)(-1200x1500\\.)+([a-zA-Z0-9]){8}+\\.jpg"), "Image does not match");
        assertTrue(product.getPriceText().matches("\\$[0-9]{1,2}.[0-9]{1,2}"), "Price does not match");
        assertEquals(product.getButtonName(), "Add to cart", "");
        driver.quit();
    }

    @Test
    public void testSortOptions() {
        ProductsPage productsPage = login(driver);
        var products = productsPage.getProducts();
        var pricesBefore = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        Sorting sort = productsPage.getSortComponent();
        assertEquals(sort.getSortActiveOption(), SortingOptions.AZ.getName(), "Filters are not equal");
        List<String> sortingOptions = Stream.of(SortingOptions.values())
                .map(SortingOptions::getName)
                .collect(Collectors.toList());
        assertEquals(sort.getSortOptionNames(), sortingOptions, "Sort options are not equal");
        sort.selectSortOption(SortingOptions.LOWHIGH.getName());
        products = productsPage.getProducts();
        var pricesAfter = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        assertNotEquals(pricesBefore, pricesAfter, "Prices are equal");
        Collections.sort(pricesBefore);
        assertEquals(pricesBefore, pricesAfter, "Prices are not equal");
        sort = productsPage.getSortComponent();
        assertEquals(sort.getSortActiveOption(), SortingOptions.LOWHIGH.getName(), "Filters are not equal");
        driver.quit();
    }

    @Test
    public void testAddToCartButton() {
        ProductsPage productsPage = login(driver);
        List<Product> productsList = productsPage.getProducts();
        Product product = getRandomProduct(productsList);
        assertEquals(product.getButtonName(), "Add to cart", "Buttons are not equal");
        product.clickAddToCart();
        assertEquals(product.getButtonName(), "Remove", "Buttons are not equal");
        product.clickAddToCart();
        assertEquals(product.getButtonName(), "Add to cart", "Buttons are not equal");
        driver.quit();
    }

    @Test
    public void testUserCanAddAndRemoveProductInCart() {
        ProductsPage productsPage = login(driver);
        List<Product> productsList = productsPage.getProducts();
        Product product = productsList.get(1);
//                getRandomProduct(productsList);
        String productName = product.getName();
        String productDesc = product.getDescription();
        String productPrice = product.getPriceText();
        product.clickAddToCart();
        ShoppingCart cart = productsPage.clickCartButton();
        assertEquals(productsPage.getTitle(), "Your Cart", "Titles are not equal");
        List<Product> productsCartList = cart.getProducts();
        assertEquals(productsCartList.size(), 1, "Products sizes are not equal");
        Product cartProduct = productsCartList.get(0);
        assertEquals(cartProduct.getName(), productName, "Products names are not equal");
        assertEquals(cartProduct.getDescription(), productDesc, "Products descs are not equal");
        assertEquals(cartProduct.getPriceText(), productPrice, "Products prices are not equal");
        cart.clickRemove();
        productsCartList = cart.getProducts();
        assertTrue(productsCartList.isEmpty(), "Products are present");
        driver.quit();
    }

    @Test
    public void testCheckOutButton() {
        ProductsPage productsPage = login(driver);
        List<Product> productsList = productsPage.getProducts();
        Product product = getRandomProduct(productsList);
        product.clickAddToCart();
        ShoppingCart cart = productsPage.clickCartButton();
        cart.clickCheckOut();
        CheckOut checkOut = productsPage.getCheckOutComponent();
        assertEquals(productsPage.getTitle(), "Checkout: Your Information", "Titles are not equal");
        for (CheckOutInfo info : CheckOutInfo.values()) {
            assertTrue(checkOut.inputIsPresent(info.getName()), "Input is not present" + info.getName());
        }
        assertTrue(checkOut.buttonContinueIsPresent(), "Button 'Continue' is not present");
        checkOut.clickCancel();
        productsPage = new ProductsPage(driver);
        assertEquals(productsPage.getTitle(), "Your Cart", "Titles are not equal");
        driver.quit();
    }

    @Test
    public void testContinueShoppingButton() {
        ProductsPage productsPage = login(driver);
        ShoppingCart cart = productsPage.clickCartButton();
        List<Product> productsCartList = cart.getProducts();
        assertTrue(productsCartList.isEmpty(), "Products sizes are present");
        cart.clickContinue();
        List<Product> productsList = productsPage.getProducts();
        assertEquals(6, productsList.size(), "Product sizes are not equal");
    }


    public ProductsPage login(WebDriver driver) {
        String username = "standard_user";
        String password = "secret_sauce";
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

    public WebDriver getDriver() {
        return driver;
    }
}
