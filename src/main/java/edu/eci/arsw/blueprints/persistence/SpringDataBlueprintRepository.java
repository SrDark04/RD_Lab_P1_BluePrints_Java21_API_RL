package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.entity.blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface SpringDataBlueprintRepository extends JpaRepository<blueprint, Long> {
    Optional<blueprint> findByAuthorAndName(String author, String name);

}
