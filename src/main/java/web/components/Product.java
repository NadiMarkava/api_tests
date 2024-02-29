package web.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import web.components.BaseComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product extends BaseComponent {

    private By productName = By.className("inventory_item_name");

    private By productDesc = By.className("inventory_item_desc");

    private By productImage = By.xpath(".//div[@class='inventory_item_img']//img");

    private By productPrice = By.className("inventory_item_price");

    private By buttonText = By.xpath(".//div[@class='pricebar']//button");

    private By addToCartButton = By.xpath(".//button[text()='Add to cart']");

    private By removeButton = By.xpath(".//button[text()='Remove']");

    public Product(WebElement root) {
        super(root);
    }

    public String getNameLabelText() {
        return root.findElement(productName).getText();
    }

    public String getDescriptionLabelText() {
        return root.findElement(productDesc).getText();
    }

    public String getImageAttribute() {
        return root.findElement(productImage).getAttribute("src");
    }

    public String getButtonLabelText() {
        return root.findElement(buttonText).getText();
    }

    public String getPriceLabelText() {
        return root.findElement(productPrice).getText();
    }

    public BigDecimal getPrice() {
        return new BigDecimal(
                root.findElement(productPrice)
                        .getText()
                        .replace("$", "")
        ).setScale(2, RoundingMode.UNNECESSARY); // Sanitation and formatting
    }

    public void clickAddToCartButton() {
        root.findElement(addToCartButton).click();
    }

    public void clickRemoveButton() {
        root.findElement(removeButton).click();
    }
}
