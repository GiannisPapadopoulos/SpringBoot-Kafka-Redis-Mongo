package com.github.gpapadopoulos.colorcounting.mongodb.repo;

import com.github.gpapadopoulos.colorcounting.mongodb.model.ColorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ColorDocumentRepository extends MongoRepository<ColorDocument, String> { }
