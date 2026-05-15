package com.supplysync.repository;

import com.supplysync.models.Marketer;

import java.util.List;

/** Marketer persistence (ISP). */
public interface MarketerRepository {
    List<Marketer> findAllMarketers();

    void saveMarketer(Marketer marketer);
}
