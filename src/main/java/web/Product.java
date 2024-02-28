package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Product extends BaseComponent {

    protected WebDriver driver;
    private By productName = By.className("inventory_item_name");
    private By productDesc = By.className("inventory_item_desc");
    private By productImage = By.xpath(".//div[@class='inventory_item_img']//img");
    private By productPrice = By.className("inventory_item_price");
    private By button = By.xpath(".//div[@class='pricebar']//button");

    public Product(WebElement root) {
        super(root);
    }

    public Product() {
        super();
    }

    public String getName() {
        return root.findElement(productName).getText();
    }

    public String getDescription() {
        return root.findElement(productDesc).getText();
    }

    public String getImage() {
        return root.findElement(productImage).getAttribute("src");
    }

    public String getButtonName() {
        return root.findElement(button).getText();
    }

    public String getPriceText() {
        return root.findElement(productPrice).getText();
    }

    public BigDecimal getPrice() {
        return new BigDecimal(
                root.findElement(productPrice)
                        .getText()
                        .replace("$", "")
        ).setScale(2, RoundingMode.UNNECESSARY); // Sanitation and formatting
    }

    public void clickAddToCart() {
        root.findElement(button).click();
    }

}
