package it.mahd.taxi.database;

/**
 * Created by salem on 4/1/16.
 */
public class ServicesDB {
    private String id;
    private String name;
    private int value;

    public ServicesDB(String id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
