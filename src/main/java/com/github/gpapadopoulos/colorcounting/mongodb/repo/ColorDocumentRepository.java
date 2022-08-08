package com.github.gpapadopoulos.colorcounting.mongodb.repo;

import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ColorDocumentRepository extends MongoRepository<Color, String> { }
