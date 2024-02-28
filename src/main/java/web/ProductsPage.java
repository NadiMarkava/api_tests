package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.function.Predicate;

public class ProductsPage extends BasePage {
    private By productTitle = By.className("title");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public String getTitle() {
        return driver.findElement(productTitle).getText();
    }

    public List<Product> getProducts() {
        return driver.findElements(By.className("inventory_item"))
                .stream()
                .map(e -> new Product(e)) // Map WebElement to a product component
                .toList();
    }

    public Product getProduct(Predicate<Product> condition) {
        return getProducts()
                .stream()
                .filter(condition) // Filter by product name or price
                .findFirst()
                .orElseThrow();
    }

    public Sorting getSortComponent(){
        return new Sorting(driver.findElement(By.className("select_container")));
    }

    public ShoppingCart clickCartButton() {
        driver.findElement(By.className("shopping_cart_link")).click();
        return new ShoppingCart(driver.findElement(By.className("cart_contents_container")));
    }

    public CheckOut getCheckOutComponent() {
        return new CheckOut(driver.findElement(By.id("checkout_info_container")));
    }
}


