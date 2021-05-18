package models;

public class ColumnNode<T> {
    private String name;
    private boolean nullable;
    private boolean unique;

    public ColumnNode(String name, boolean nullable, boolean unique) {
        this.name = name;
        this.nullable = nullable;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}