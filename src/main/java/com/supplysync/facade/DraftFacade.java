package com.supplysync.facade;

import com.supplysync.models.MarketerOrderDraft;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.repository.DraftRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Marketer order draft lifecycle (SRP).
 */
public final class DraftFacade {
    private final DraftRepository drafts;
    private final AuthFacade auth;
    private final CatalogFacade catalog;

    public DraftFacade(DraftRepository drafts, AuthFacade auth, CatalogFacade catalog) {
        this.drafts = drafts;
        this.auth = auth;
        this.catalog = catalog;
    }

    public void saveOrderDraftFromForm(String customerName, String phone, String country, String address) {
        User u = auth.getCurrentUser();
        if (u == null || u.getId() == null) {
            return;
        }
        MarketerOrderDraft d = new MarketerOrderDraft();
        d.setMarketerId(u.getId());
        d.setCustomerName(customerName != null ? customerName.trim() : "");
        d.setCustomerPhone(phone != null ? phone.trim() : "");
        d.setCustomerCountry(country != null ? country.trim() : "");
        d.setShippingAddress(address != null ? address.trim() : "");
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Product p : catalog.getCart()) {
            counts.merge(p.getId(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            d.addLine(e.getKey(), e.getValue());
        }
        drafts.saveMarketerOrderDraft(d);
    }

    public boolean hasOrderDraft() {
        User u = auth.getCurrentUser();
        return u != null && u.getId() != null && drafts.findMarketerOrderDraft(u.getId()).isPresent();
    }

    public Optional<MarketerOrderDraft> getOrderDraft() {
        User u = auth.getCurrentUser();
        if (u == null || u.getId() == null) {
            return Optional.empty();
        }
        return drafts.findMarketerOrderDraft(u.getId());
    }

    public void discardOrderDraft() {
        User u = auth.getCurrentUser();
        if (u != null && u.getId() != null) {
            drafts.deleteMarketerOrderDraft(u.getId());
        }
    }

    public void applyOrderDraft(MarketerOrderDraft draft) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MarketerOrderDraft.DraftCartLine line : draft.getLines()) {
            counts.put(line.getProductId(), line.getQuantity());
        }
        catalog.loadCartFromProductIds(counts);
    }

    void discardDraftForCurrentUser() {
        discardOrderDraft();
    }
}
