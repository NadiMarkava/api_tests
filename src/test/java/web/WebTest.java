package web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.testng.Assert.*;

public class WebTest {

    protected WebDriver driver;

    @Test
    public void checkLogin(){
        String username = "standard_user";
        String password = "secret_sauce";
        driver = new ChromeDriver();
        LoginPage login = new LoginPage(driver);
        ProductsPage productsPage = login.loginAs(username, password);
        assertEquals(productsPage.getTitle(), "Products", "Titles are not equal");
        var products = productsPage.getProducts();
        assertEquals(6, products.size()); // expected, actual
        driver.quit();
    }

    @Test
    public void testSortOptions() {
        String username = "standard_user";
        String password = "secret_sauce";
        driver = new ChromeDriver();
        LoginPage login = new LoginPage(driver);
        ProductsPage productsPage = login.loginAs(username, password);
        var products = productsPage.getProducts();
        var pricesBefore = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        assertEquals(productsPage.getSortActiveOption(), "Name (A to Z)", "Filters are not equal");
        List<String> sortOptionEnums = Stream.of("Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)")
                .collect(Collectors.toList());
        assertEquals(productsPage.getSortOptionNames(), sortOptionEnums, "Sort options are not equal");
        productsPage.selectSortOption("Price (low to high)");
        products = productsPage.getProducts();
        var pricesAfter = products.stream().map(p -> p.getPrice()).collect(Collectors.toList());
        assertNotEquals(pricesBefore, pricesAfter, "Prices are equal");
        Collections.sort(pricesBefore);
        assertEquals(pricesBefore, pricesAfter, "Prices are not equal");
        assertEquals(productsPage.getSortActiveOption(), "Price (low to high)", "Filters are not equal");
        driver.quit();
    }

    @Test
    public void testAddToCartButton() {
        String username = "standard_user";
        String password = "secret_sauce";
        driver = new ChromeDriver();
        LoginPage login = new LoginPage(driver);
        ProductsPage productsPage = login.loginAs(username, password);
        List<Product> productsList = productsPage.getProducts();
        int randomProductIndex = new Random().nextInt(productsList.size());
        Product product = productsList.get(randomProductIndex);
        assertEquals(product.getButtonName(), "Add to cart", "Buttons are not equal");
        product.clickAddToCart();
        assertEquals(product.getButtonName(), "Remove", "Buttons are not equal");
        product.clickAddToCart();
        assertEquals(product.getButtonName(), "Add to cart", "Buttons are not equal");
        driver.quit();
    }


    public WebDriver getDriver() {
        return driver;
    }
}
