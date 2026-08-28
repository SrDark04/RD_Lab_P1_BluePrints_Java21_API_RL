package edu.eci.arsw.blueprints.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.entity.BlueprintPoint;
import edu.eci.arsw.blueprints.entity.blueprint;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

@Component
@Primary
public class PostgressBluePrintPersistence implements BlueprintPersistence {

    private final SpringDataBlueprintRepository repository;

    public PostgressBluePrintPersistence(SpringDataBlueprintRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repository.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }

        blueprint entity = new blueprint(bp.getAuthor(), bp.getName());
        bp.getPoints().forEach(point -> entity.addPoint(point.x(), point.y()));
        repository.save(entity);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        blueprint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        return toModel(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = repository.findAll().stream()
                .filter(entity -> entity.getAuthor().equals(author))
                .map(this::toModel)
                .collect(Collectors.toSet());

        if (blueprints.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return repository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        blueprint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.addPoint(x, y);
        repository.save(entity);
    }

    private Blueprint toModel(blueprint entity) {
        List<Point> points = entity.getPoints().stream()
                .map(this::toModel)
                .toList();
        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }

    private Point toModel(BlueprintPoint point) {
        return new Point(point.getX(), point.getY());
    }
}