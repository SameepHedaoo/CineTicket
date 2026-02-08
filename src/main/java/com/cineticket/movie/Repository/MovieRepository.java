package com.cineticket.movie.Repository;

import com.cineticket.movie.Entity.MovieEntity;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    List<MovieEntity> findByActiveTrue();

    @Query("""
            SELECT m FROM MovieEntity m
            WHERE m.id IN (
                SELECT MIN(m2.id)
                FROM MovieEntity m2
                GROUP BY m2.title
            )
            """)
    List<MovieEntity> findDistinctMoviesForMoviesTab();
}
