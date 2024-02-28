package web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Sorting extends BaseComponent{

    private By selectedOption = By.className("active_option");
    private By sortOptionName = By.xpath("//select[@class='product_sort_container']/option");

    public Sorting(WebElement root) {
        super(root);
    }

    public String getSortActiveOption() {
        return root.findElement(selectedOption).getText();
    }

    public List<String> getSortOptionNames() {
        return root.findElements(sortOptionName).stream()
                .map(o -> o.getText())
                .toList();
    }

    public void selectSortOption(String option) {
        root.findElement(By.xpath(String.format("//select/option[text()='%s']", option))).click();
    }
}
