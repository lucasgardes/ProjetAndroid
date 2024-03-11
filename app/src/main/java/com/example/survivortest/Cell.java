package com.example.survivortest;

public class Cell {
    private Tile value;
    private int x, y; // Position dans la grille

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.value = null;
    }

    public Tile getValue() {
        return value;
    }

    public boolean isCellEmpty() {
        return this.value == null;
    }

    public void setValue(Tile value) {
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
