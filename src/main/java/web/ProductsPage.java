package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.function.Predicate;

public class ProductsPage extends BasePage {
    private By productTitle = By.className("title");

    private By selectedOption = By.className("active_option");
    private By sortOptionName = By.xpath("//select[@class='product_sort_container']/option");

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

    public String getSortActiveOption() {
        return driver.findElement(selectedOption).getText();
    }

    public List<String> getSortOptionNames() {
        return driver.findElements(sortOptionName).stream()
                .map(o -> o.getText())
                .toList();
    }

    public void selectSortOption(String option) {
        driver.findElement(By.xpath(String.format("//select/option[text()='%s']", option))).click();
    }
}


