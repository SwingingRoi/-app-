package com.cpd.soundbook.Repository;

import com.cpd.soundbook.Entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface ChapterRepository extends JpaRepository<Chapter,Integer> {



}
