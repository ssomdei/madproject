package com.example.madproject;

import java.io.Serializable;

public class Game implements Serializable {
    private String name;
    private String imageUrl;
    private double rating;
    private int id;
    private String genre;

    public String getRequirements() {
        return requirements;
    }

    public String getGenre() {
        return genre;
    }

    private String requirements;

    public Game() {
    }

    public Game(String name, String imageUrl, double rating, int id, String genre, String requirements) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.id = id;
        this.genre = genre;
        this.requirements = requirements;
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