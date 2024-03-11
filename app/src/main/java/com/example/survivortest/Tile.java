package com.example.survivortest;

import android.graphics.Color;

import java.util.Random;

public class Tile {
    private int value;

    public Tile(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTileColor() {
        switch (value) {
            case 4:
                return Color.parseColor("#ede0c8"); // Beige
            case 8:
                return Color.parseColor("#f2b179"); // Orange
            case 16:
                return Color.parseColor("#f59563"); // Dark Orange
            case 32:
                return Color.parseColor("#f67c5f"); // Reddish Orange
            case 64:
                return Color.parseColor("#f65e3b"); // Red
            case 128:
                return Color.parseColor("#edcf72"); // Yellow
            case 256:
                return Color.parseColor("#edcc61"); // Light Yellow
            case 512:
                return Color.parseColor("#edc850"); // Gold
            // Ajoutez d'autres cas selon vos besoins
            default:
                return Color.parseColor("#cdc1b4"); // Default color for other values
        }
    }
}
