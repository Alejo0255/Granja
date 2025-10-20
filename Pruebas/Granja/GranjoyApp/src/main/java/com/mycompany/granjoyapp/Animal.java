package com.mycompany.granjoyapp;

public class Animal {
    private String id, species, name, health, entryDate;
    private int age;
    private double weight, cost;

    public Animal(String id, String species, String name, int age, double weight,
                  String health, String entryDate, double cost) {
        this.id = id;
        this.species = species;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.health = health;
        this.entryDate = entryDate;
        this.cost = cost;
    }

    // ✅ Getters públicos (para usar en tus tests)
    public String getId() { return id; }
    public String getSpecies() { return species; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public String getHealth() { return health; }
    public String getEntryDate() { return entryDate; }
    public double getCost() { return cost; }
}
