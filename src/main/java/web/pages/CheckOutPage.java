package web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckOutPage extends BasePage {

    private By cancelButton = By.xpath("//button[text()='Cancel']");
    private By continueButton = By.xpath("//input[@value='Continue']");

    public CheckOutPage(WebDriver driver) {
        super(driver);
    }

    public boolean isInputPresent(String input) {
        return driver.findElement(By.xpath(String.format("//input[@placeholder='%s']", input))).isDisplayed();
    }

    public boolean IsContinueButtonPresent() {
        return driver.findElement(continueButton).isDisplayed();
    }

    public ShoppingCartPage clickCancelButton() {
        driver.findElement(cancelButton).click();
        return new ShoppingCartPage(driver);
    }
}
