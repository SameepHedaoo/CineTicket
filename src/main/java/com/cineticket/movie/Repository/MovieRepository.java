package com.cineticket.movie.Repository;

import com.cineticket.movie.Entity.MovieEntity;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    List<MovieEntity> findByActiveTrue();
}