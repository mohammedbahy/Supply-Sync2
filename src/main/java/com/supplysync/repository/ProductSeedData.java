package com.supplysync.repository;

/**
 * Shared catalog rows for initial database seed and in-memory demo storage.
 * Columns: id, name, category, quantity, price
 */
public final class ProductSeedData {
    private ProductSeedData() {}

    public static Object[][] rows() {
        return new Object[][]{
            {"101", "Samsung TV 55 Inch", "Electronics", 145, 650.0},
            {"102", "HP Laptop ProBook", "Electronics", 85, 720.0},
            {"103", "Dell Gaming Mouse", "Electronics", 210, 35.0},
            {"104", "Lenovo Keyboard K120", "Electronics", 95, 20.0},
            {"105", "Canon Printer X200", "Electronics", 0, 180.0},
            {"106", "JBL Bluetooth Speaker", "Electronics", 175, 90.0},
            {"107", "Apple AirPods Gen 3", "Electronics", 60, 250.0},
            {"108", "Xiaomi Smart Watch", "Electronics", 130, 140.0},
            {"109", "Logitech Webcam HD", "Electronics", 40, 70.0},
            {"110", "Sony Headphones WH1000", "Electronics", 0, 310.0},
            {"111", "Office Chair Comfort", "Furniture", 120, 150.0},
            {"112", "Wooden Office Desk", "Furniture", 90, 240.0},
            {"113", "Metal Storage Cabinet", "Furniture", 75, 300.0},
            {"114", "Adjustable Study Table", "Furniture", 180, 200.0},
            {"115", "Sofa Modern Style", "Furniture", 0, 550.0},
            {"116", "Dining Table Set", "Furniture", 110, 420.0},
            {"117", "Bookshelf Classic", "Furniture", 98, 120.0},
            {"118", "Wardrobe 4 Doors", "Furniture", 55, 600.0},
            {"119", "Coffee Table Glass", "Furniture", 150, 175.0},
            {"120", "King Size Bed Frame", "Furniture", 0, 800.0},
            {"121", "Nike Running Shoes", "Fashion", 220, 130.0},
            {"122", "Adidas Sports Jacket", "Fashion", 80, 95.0},
            {"123", "Puma Sweatpants", "Fashion", 67, 70.0},
            {"124", "Levi's Blue Jeans", "Fashion", 190, 85.0},
            {"125", "Gucci Leather Belt", "Fashion", 0, 350.0},
            {"126", "Zara Cotton Shirt", "Fashion", 145, 45.0},
            {"127", "H&M Casual Hoodie", "Fashion", 90, 55.0},
            {"128", "RayBan Sunglasses", "Fashion", 102, 160.0},
            {"129", "Chanel Handbag", "Fashion", 35, 1200.0},
            {"130", "Prada Sneakers", "Fashion", 0, 980.0},
            {"131", "Organic Milk 1L", "Grocery", 300, 2.5},
            {"132", "Brown Bread Pack", "Grocery", 280, 1.5},
            {"133", "Basmati Rice 5KG", "Grocery", 95, 14.0},
            {"134", "Sunflower Oil 2L", "Grocery", 0, 8.0},
            {"135", "Coca Cola Can", "Grocery", 500, 1.0},
            {"136", "Pepsi Bottle 1L", "Grocery", 420, 1.2},
            {"137", "Nescafe Coffee Jar", "Grocery", 75, 9.0},
            {"138", "Lipton Tea Pack", "Grocery", 130, 4.0},
            {"139", "Oreo Biscuits Pack", "Grocery", 65, 2.0},
            {"140", "KitKat Chocolate Bar", "Grocery", 0, 1.5},
            {"141", "Panadol Extra", "Health", 250, 5.0},
            {"142", "Vitamin C Tablets", "Health", 180, 12.0},
            {"143", "Face Mask Pack", "Health", 95, 8.0},
            {"144", "Hand Sanitizer 500ml", "Health", 110, 6.0},
            {"145", "Digital Thermometer", "Health", 0, 15.0},
            {"146", "Blood Pressure Monitor", "Health", 70, 45.0},
            {"147", "First Aid Kit", "Health", 130, 30.0},
            {"148", "Protein Powder 2KG", "Health", 88, 55.0},
            {"149", "Omega 3 Capsules", "Health", 140, 20.0},
            {"150", "Medical Gloves Box", "Health", 0, 10.0}
        };
    }
}
