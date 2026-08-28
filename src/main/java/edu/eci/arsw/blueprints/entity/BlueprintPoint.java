package edu.eci.arsw.blueprints.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blueprint_points")
public class BlueprintPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;

    @ManyToOne
    @JoinColumn(name = "blueprint_id", nullable = false)
    private blueprint blueprint;

    protected BlueprintPoint() {
    }

    public BlueprintPoint(int x, int y, blueprint blueprint) {
        this.x = x;
        this.y = y;
        this.blueprint = blueprint;
    }

    public Long getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public blueprint getBlueprint() {
        return blueprint;
    }
}