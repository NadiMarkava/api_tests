package web.enums;

public enum SortingOptions {

    AZ("Name (A to Z)"),
    ZA("Name (Z to A)"),
    LOWHIGH("Price (low to high)"),
    HIGHTOLOW("Price (high to low)");

    private String name;

    private SortingOptions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
