package web.enums;

public enum CheckOutInfo {

    FIRSTN("First Name"),
    LASTN("Last Name"),
    ZIP("Zip/Postal Code");

    private String name;

    private CheckOutInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
