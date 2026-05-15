package com.supplysync.repository;

import com.supplysync.models.MarketerOrderDraft;

import java.util.Optional;

/** Marketer order draft persistence (ISP). */
public interface DraftRepository {
    void saveMarketerOrderDraft(MarketerOrderDraft draft);

    Optional<MarketerOrderDraft> findMarketerOrderDraft(String marketerId);

    void deleteMarketerOrderDraft(String marketerId);
}
