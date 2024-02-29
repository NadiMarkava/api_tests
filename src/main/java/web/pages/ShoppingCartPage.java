package web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import web.components.Product;
import web.pages.BasePage;
import web.pages.CheckOutPage;

import java.util.List;

public class ShoppingCartPage extends BasePage {

    private By checkOutButton = By.xpath("//button[text()='Checkout']");

    private By continueShoppingButton = By.xpath("//button[text()='Continue Shopping']");

    public ShoppingCartPage(WebDriver driver) {
        super(driver);
    }

    public List<Product> getProducts() {
        return driver.findElements(By.className("cart_item"))
                .stream()
                .map(e -> new Product(e)) // Map WebElement to a product component
                .toList();
    }

    public CheckOutPage clickCheckOutButton() {
        driver.findElement(checkOutButton).click();
        return new CheckOutPage(driver);
    }

    public void clickContinueShoppingButton() {
        driver.findElement(continueShoppingButton).click();
    }
}
