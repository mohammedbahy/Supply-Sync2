package com.supplysync.presentation;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static boolean isArabic = false;
    private static final Map<String, String> enToAr = new HashMap<>();
    private static final Map<String, String> arToEn = new HashMap<>();

    static {
        // Common Sidebar & Navigation
        add("Dashboard", "لوحة التحكم");
        add("Products", "المنتجات");
        add("Orders", "الطلبات");
        add("My Orders", "طلباتي");
        add("Marketers", "المسوقين");
        add("Reports", "التقارير");
        add("Support", "الدعم");
        add("Logout", "تسجيل الخروج");
        add("Sign Out", "تسجيل الخروج");
        add("Search", "بحث");
        add("English", "English");
        add("Arabic", "العربية");
        
        // Login / Auth
        add("Welcome back", "مرحباً بعودتك");
        add("Email", "البريد الإلكتروني");
        add("Password", "كلمة المرور");
        add("Sign In", "تسجيل الدخول");
        add("Create account", "إنشاء حساب");

        // Dashboard Labels
        add("TOTAL PRODUCTS", "إجمالي المنتجات");
        add("TOTAL ORDERS", "إجمالي الطلبات");
        add("PENDING ORDERS", "طلبات معلقة");
        add("REVENUE", "الأرباح");
        add("Dashboard Overview", "نظرة عامة");
        add("Real-time logistics and inventory tracking system", "نظام تتبع الخدمات اللوجستية والمخزون في الوقت الفعلي");
        add("Statistics Overview", "نظرة عامة على الإحصائيات");
        add("Recent Inventory Updates", "آخر تحديثات المخزن");
        add("Order Velocity", "سرعة الطلبات");
        add("View All", "عرض الكل");

        // Products Management
        add("Products Management", "إدارة المنتجات");
        add("Add Product", "إضافة منتج");
        add("Edit Product", "تعديل منتج");
        add("Export CSV", "تصدير CSV");
        add("IN STOCK", "متوفر");
        add("LOW STOCK", "مخزون منخفض");
        add("OUT OF STOCK", "نفذت الكمية");
        add("PRODUCT NAME", "اسم المنتج");
        add("QUANTITY", "الكمية");
        add("PRICE", "السعر");
        add("STATUS", "الحالة");
        add("ACTIONS", "الإجراءات");
        add("Category", "الفئة");
        add("Action", "الإجراء");

        // Orders Management
        add("ORDER ID", "رقم الطلب");
        add("CUSTOMER NAME", "اسم العميل");
        add("TOTAL PRICE", "السعر الإجمالي");
        add("DATE", "التاريخ");
        add("Approve Order", "الموافقة على الطلب");
        add("CANCEL ORDER", "إلغاء الطلب");
        add("Remove Order", "حذف الطلب");
        add("Order Details", "تفاصيل الطلب");
        add("Customer Information", "معلومات العميل");
        add("Order Summary", "ملخص الطلب");
        add("Subtotal", "المجموع الفرعي");
        add("Shipping", "الشحن");
        add("Total", "الإجمالي");
        add("Confirm Order", "تأكيد الطلب");

        // Product Catalog (User Side)
        add("Product Catalog", "كتالوج المنتجات");
        add("ADD TO ORDER", "إضافة للطلب");
        add("إضافة للطلب", "إضافة للطلب");
        add("Units", "وحدة");
        add("All Categories", "كل الفئات");
        add("Electronics", "إلكترونيات");
        add("Furniture", "أثاث");
        add("Fashion", "أزياء");
        add("Grocery", "بقالة");
        add("Health", "صحة");

        // Notifications / Reports
        add("System Notifications", "إشعارات النظام");
        add("Mark all as read", "تحديد الكل كمقروء");
        add("TODAY", "اليوم");
        add("YESTERDAY", "أمس");
        
        // Marketers
        add("Marketers Management", "إدارة المسوقين");
        add("Add Marketer", "إضافة مسوق");
        add("Edit Marketer", "تعديل مسوق");
    }

    private static void add(String en, String ar) {
        enToAr.put(en, ar);
        arToEn.put(ar, en);
    }

    public static boolean isArabic() {
        return isArabic;
    }

    public static void setArabic(boolean arabic) {
        isArabic = arabic;
    }

    public static String get(String key) {
        if (isArabic) {
            return enToAr.getOrDefault(key, key);
        }
        return key;
    }
}
