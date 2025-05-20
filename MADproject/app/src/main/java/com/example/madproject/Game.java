package com.example.madproject;

public class Game {
    private String name;
    private String imageUrl;
    private double rating;
    private int id;

    public Game() {
    }

    public Game(String name, String imageUrl, double rating, int id) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public int getId() {
        return id;
    }
}