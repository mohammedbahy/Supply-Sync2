package com.supplysync.facade;

import com.supplysync.models.Product;
import com.supplysync.repository.ProductRepository;
import com.supplysync.services.inventory.InventoryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Product catalog, stock queries, and session cart (SRP).
 */
public final class CatalogFacade {
    private final InventoryService inventoryService;
    private final ProductRepository products;
    private final List<Product> cart = new ArrayList<>();

    public CatalogFacade(InventoryService inventoryService, ProductRepository products) {
        this.inventoryService = inventoryService;
        this.products = products;
    }

    public List<Product> getCatalog() {
        return inventoryService.getAllProducts();
    }

    public void saveProduct(Product product) {
        products.saveProduct(product);
    }

    public void deleteProduct(String productId) {
        products.deleteProduct(productId);
    }

    public int countProductInCart(String productId) {
        if (productId == null) {
            return 0;
        }
        return (int) cart.stream().filter(p -> productId.equals(p.getId())).count();
    }

    public int availableUnitsToAddFromCatalog(Product product) {
        if (product == null) {
            return 0;
        }
        return Math.max(0, product.getQuantity() - countProductInCart(product.getId()));
    }

    public boolean addToCart(Product product) {
        if (product == null) {
            return false;
        }
        if (availableUnitsToAddFromCatalog(product) <= 0) {
            return false;
        }
        cart.add(product);
        return true;
    }

    public List<Product> getCart() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public boolean removeOneUnitFromCart(String productId) {
        if (productId == null) {
            return false;
        }
        for (int i = 0; i < cart.size(); i++) {
            if (productId.equals(cart.get(i).getId())) {
                cart.remove(i);
                return true;
            }
        }
        return false;
    }

    /** Restores cart lines from a draft without persisting. */
    public void loadCartFromProductIds(java.util.Map<String, Integer> productIdToQty) {
        clearCart();
        List<Product> catalog = getCatalog();
        for (java.util.Map.Entry<String, Integer> e : productIdToQty.entrySet()) {
            Product ref = catalog.stream()
                    .filter(p -> p.getId().equals(e.getKey()))
                    .findFirst()
                    .orElse(null);
            if (ref == null) {
                continue;
            }
            for (int i = 0; i < e.getValue(); i++) {
                if (!addToCart(ref)) {
                    break;
                }
            }
        }
    }
}
