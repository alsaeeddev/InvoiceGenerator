package alsaeeddev.com;

public class ItemModel {
    private final double itemPrice;
    private final String itemName;
    private int itemQuantity;

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int quantity){
        this.itemQuantity = quantity;
    }



    public double getItemPrice() {
        return itemPrice;
    }

    public String getItemName() {
        return itemName;
    }


    public ItemModel(double itemPrice, String itemName, int itemQuantity) {
        this.itemPrice = itemPrice;
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
    }




}
