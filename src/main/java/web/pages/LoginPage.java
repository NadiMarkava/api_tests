package web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private By usernameLocator = By.cssSelector("input#user-name");
    private By passwordLocator = By.cssSelector("input[placeholder='Password']");
    private By loginButtonLocator = By.id("login-button");

    public LoginPage(WebDriver driver) {
        super(driver);
        driver.get("https://www.saucedemo.com/");
    }

    public ProductsPage loginAs(String username, String password) {
        driver.findElement(usernameLocator).sendKeys(username);
        driver.findElement(passwordLocator).sendKeys(password);
        driver.findElement(loginButtonLocator).submit();
        return new ProductsPage(driver);
    }
}
