package edu.school21.reflection.classes;

import java.util.StringJoiner;

public class Car {
    private String brand;
    private int releaseYear;
    private double maxSpeed;
    private int mileage;

    public Car() {
        brand = "default brand";
        releaseYear = 2000;
        maxSpeed = 130;
        mileage = 0;
    }

    public void addMileage() {
        ++mileage;
    }

    public Car(String brand, int releaseYear, double maxSpeed, int mileage) {
        this.brand = brand;
        this.releaseYear = releaseYear;
        this.maxSpeed = maxSpeed;
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", Car.class.getSimpleName() + "[", "]")
                .add("brand='" + brand + "'")
                .add("releaseYear=" + releaseYear)
                .add("maxSpeed=" + maxSpeed)
                .add("mileage=" + mileage)
                .toString();
    }
}
