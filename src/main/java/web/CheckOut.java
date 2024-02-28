package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CheckOut extends BaseComponent {
    private By buttonCancel = By.xpath("//button[text()='Cancel']");
    private By buttonContinue = By.xpath("//input[@value='Continue']");


    public CheckOut(WebElement root) {
        super(root);
    }

    public boolean inputIsPresent(String input) {
        return root.findElement(By.xpath(String.format("//input[@placeholder='%s']", input))).isDisplayed();
    }

    public boolean buttonContinueIsPresent() {
        return root.findElement(buttonContinue).isDisplayed();
    }

    public void clickCancel() {
        root.findElement(buttonCancel).click();
    }
}
