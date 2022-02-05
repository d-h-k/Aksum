package com.carrot.attachfile.repository;

import com.carrot.attachfile.entity.AttachFile;
import com.carrot.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachFileRepository extends JpaRepository<AttachFile, Long> {
    Optional<List<AttachFile>> findAllByPost(Article article);
}
