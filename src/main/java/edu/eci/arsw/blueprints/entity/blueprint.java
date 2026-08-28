package edu.eci.arsw.blueprints.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blueprints")
public class blueprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String name;

    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlueprintPoint> points = new ArrayList<>();

    protected blueprint() {
    }

    public blueprint(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public List<BlueprintPoint> getPoints() {
        return points;
    }

    public void addPoint(int x, int y) {
        points.add(new BlueprintPoint(x, y, this));
    }
}
