package com.github.gpapadopoulos.colorcounting.redis.repo;

import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends CrudRepository<Color, String> {
}
