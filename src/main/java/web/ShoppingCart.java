package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ShoppingCart extends BaseComponent{

    private By buttonCheckOut = By.xpath(".//button[text()='Checkout']");
    private By buttonContinue = By.xpath(".//button[text()='Continue Shopping']");

    private By buttonRemove = By.xpath(".//button[text()='Remove']");

    public ShoppingCart(WebElement root) {
        super(root);
    }

    public List<Product> getProducts() {
        return root.findElements(By.className("cart_item"))
                .stream()
                .map(e -> new Product(e)) // Map WebElement to a product component
                .toList();
    }

    public void clickCheckOut() {
        root.findElement(buttonCheckOut).click();
    }

    public void clickRemove() {
        root.findElement(buttonRemove).click();
    }

    public void clickContinue() {
        root.findElement(buttonContinue).click();
    }
}
