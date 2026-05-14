package com.supplysync.repository;

import com.supplysync.models.Marketer;
import com.supplysync.models.Message;
import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.models.OrderStatuses;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteStorage implements Storage {
    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String jdbcUrl;

    public SqliteStorage(String databaseFilePath) {
        String normalized = databaseFilePath.replace('\\', '/');
        this.jdbcUrl = "jdbc:sqlite:" + normalized;
        try (Connection c = connect()) {
            initSchema(c);
            ensureSeedData(c);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open or initialize database: " + databaseFilePath, e);
        }
    }

    private Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(jdbcUrl);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    private void initSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id TEXT PRIMARY KEY,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password TEXT NOT NULL,"
                    + "name TEXT NOT NULL,"
                    + "role TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "category TEXT NOT NULL,"
                    + "price REAL NOT NULL,"
                    + "image_path TEXT NOT NULL DEFAULT '')");
            s.execute("CREATE TABLE IF NOT EXISTS marketers ("
                    + "id TEXT PRIMARY KEY,"
                    + "name TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS orders ("
                    + "id TEXT PRIMARY KEY,"
                    + "marketer_id TEXT,"
                    + "customer_name TEXT,"
                    + "customer_phone TEXT,"
                    + "customer_address TEXT,"
                    + "status TEXT NOT NULL,"
                    + "total_amount REAL NOT NULL,"
                    + "commission REAL NOT NULL,"
                    + "order_date TEXT NOT NULL,"
                    + "FOREIGN KEY (marketer_id) REFERENCES marketers(id))");
            s.execute("CREATE TABLE IF NOT EXISTS order_line_items ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id TEXT NOT NULL,"
                    + "product_id TEXT NOT NULL,"
                    + "product_name TEXT NOT NULL,"
                    + "unit_price REAL NOT NULL,"
                    + "quantity INTEGER NOT NULL DEFAULT 1,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE)");
            s.execute("CREATE TABLE IF NOT EXISTS messages ("
                    + "id TEXT PRIMARY KEY,"
                    + "order_id TEXT,"
                    + "recipient_email TEXT NOT NULL,"
                    + "sender_email TEXT NOT NULL,"
                    + "title TEXT NOT NULL,"
                    + "content TEXT NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "created_at TEXT NOT NULL,"
                    + "is_read INTEGER NOT NULL DEFAULT 0)");
            ensureOrdersPlacedAtColumn(c);
            ensureOrdersCustomerCountryColumn(c);
            ensureUserOrderPrefsColumns(c);
        }
    }

    private void ensureOrdersCustomerCountryColumn(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE orders ADD COLUMN customer_country TEXT");
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (!msg.contains("duplicate column") && !msg.contains("already exists")) {
                throw e;
            }
        }
    }

    private void ensureUserOrderPrefsColumns(Connection c) throws SQLException {
        String[] cols = {"pref_customer_name", "pref_customer_phone", "pref_customer_country", "pref_shipping_address"};
        for (String col : cols) {
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE users ADD COLUMN " + col + " TEXT");
            } catch (SQLException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (!msg.contains("duplicate column") && !msg.contains("already exists")) {
                    throw e;
                }
            }
        }
    }

    private void ensureOrdersPlacedAtColumn(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("ALTER TABLE orders ADD COLUMN placed_at TEXT");
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (!msg.contains("duplicate column") && !msg.contains("already exists")) {
                throw e;
            }
        }
    }

    private int countRows(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void ensureSeedData(Connection c) throws SQLException {
        if (countRows(c, "products") > 0) {
            return;
        }
        c.setAutoCommit(false);
        try {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO products (id,name,quantity,category,price,image_path) VALUES (?,?,?,?,?,?)")) {
                for (Object[] row : ProductSeedData.rows()) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setInt(3, (Integer) row[3]);
                    ps.setString(4, (String) row[2]);
                    ps.setDouble(5, (Double) row[4]);
                    ps.setString(6, "");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT OR REPLACE INTO users (id,email,password,name,role,pref_customer_name,pref_customer_phone,pref_customer_country,pref_shipping_address) VALUES (?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, "1");
                ps.setString(2, "admin@gmail.com");
                ps.setString(3, "Admin@123!");
                ps.setString(4, "Admin User");
                ps.setString(5, "ADMIN");
                ps.setString(6, "");
                ps.setString(7, "");
                ps.setString(8, "");
                ps.setString(9, "");
                ps.addBatch();
                ps.setString(1, "2");
                ps.setString(2, "user@gmail.com");
                ps.setString(3, "User@123");
                ps.setString(4, "Marcus Miller");
                ps.setString(5, "MARKETER");
                ps.setString(6, "");
                ps.setString(7, "");
                ps.setString(8, "");
                ps.setString(9, "");
                ps.addBatch();
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT OR REPLACE INTO marketers (id,name) VALUES (?,?)")) {
                ps.setString(1, "1");
                ps.setString(2, "Marcus Miller");
                ps.addBatch();
                ps.setString(1, "2");
                ps.setString(2, "John Doe");
                ps.addBatch();
                ps.executeBatch();
            }
            c.commit();
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }

    private static User readUser(ResultSet rs) throws SQLException {
        User u = new User(
                rs.getString("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("role"));
        u.setPrefCustomerName(nullToEmpty(rs, "pref_customer_name"));
        u.setPrefCustomerPhone(nullToEmpty(rs, "pref_customer_phone"));
        u.setPrefCustomerCountry(nullToEmpty(rs, "pref_customer_country"));
        u.setPrefShippingAddress(nullToEmpty(rs, "pref_shipping_address"));
        return u;
    }

    private static String nullToEmpty(ResultSet rs, String column) throws SQLException {
        String v = rs.getString(column);
        return v != null ? v : "";
    }

    private static Product readProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("quantity"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getString("image_path") != null ? rs.getString("image_path") : "");
    }

    private static Marketer readMarketer(ResultSet rs) throws SQLException {
        return new Marketer(rs.getString("id"), rs.getString("name"));
    }

    @Override
    public void saveOrder(Order order) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement del = c.prepareStatement("DELETE FROM order_line_items WHERE order_id=?")) {
                    del.setString(1, order.getId());
                    del.executeUpdate();
                }
                if (order.getMarketer() != null) {
                    try (PreparedStatement mk = c.prepareStatement(
                            "INSERT OR IGNORE INTO marketers (id,name) VALUES (?,?)")) {
                        mk.setString(1, order.getMarketer().getId());
                        mk.setString(2, order.getMarketer().getName() != null ? order.getMarketer().getName() : "");
                        mk.executeUpdate();
                    }
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT OR REPLACE INTO orders (id,marketer_id,customer_name,customer_phone,customer_country,customer_address,"
                                + "status,total_amount,commission,order_date,placed_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                    ins.setString(1, order.getId());
                    if (order.getMarketer() != null) {
                        ins.setString(2, order.getMarketer().getId());
                    } else {
                        ins.setNull(2, Types.VARCHAR);
                    }
                    ins.setString(3, order.getCustomerName());
                    ins.setString(4, order.getCustomerPhone());
                    ins.setString(5, order.getCustomerCountry() != null ? order.getCustomerCountry() : "");
                    ins.setString(6, order.getCustomerAddress());
                    ins.setString(7, order.getStatus());
                    ins.setDouble(8, order.getTotalAmount());
                    ins.setDouble(9, order.getCommission());
                    ins.setString(10, order.getDate() != null ? order.getDate().toString() : LocalDate.now().toString());
                    if (order.getPlacedAt() != null) {
                        ins.setString(11, order.getPlacedAt().format(ISO_DT));
                    } else {
                        ins.setNull(11, Types.VARCHAR);
                    }
                    ins.executeUpdate();
                }
                try (PreparedStatement line = c.prepareStatement(
                        "INSERT INTO order_line_items (order_id,product_id,product_name,unit_price,quantity) VALUES (?,?,?,?,?)")) {
                    for (Product p : order.getProducts()) {
                        line.setString(1, order.getId());
                        line.setString(2, p.getId());
                        line.setString(3, p.getName());
                        line.setDouble(4, p.getPrice());
                        line.setInt(5, 1);
                        line.addBatch();
                    }
                    line.executeBatch();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("saveOrder failed", e);
        }
    }

    @Override
    public Optional<Order> findOrderById(String id) {
        return findAllOrders().stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    @Override
    public List<Order> findAllOrders() {
        List<Order> list = new ArrayList<>();
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT o.id,o.marketer_id,o.customer_name,o.customer_phone,o.customer_country,o.customer_address,"
                             + "o.status,o.total_amount,o.commission,o.order_date,o.placed_at,m.name AS marketer_name "
                             + "FROM orders o LEFT JOIN marketers m ON o.marketer_id = m.id "
                             + "ORDER BY o.order_date DESC, o.id DESC")) {
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getString("id"));
                String mid = rs.getString("marketer_id");
                if (mid != null) {
                    Marketer mk = new Marketer(mid, rs.getString("marketer_name"));
                    order.setMarketer(mk);
                }
                order.setCustomerName(rs.getString("customer_name"));
                order.setCustomerPhone(rs.getString("customer_phone"));
                order.setCustomerCountry(rs.getString("customer_country"));
                order.setCustomerAddress(rs.getString("customer_address"));
                String rawStatus = rs.getString("status");
                if (OrderStatuses.APPROVED.equals(rawStatus)) {
                    order.setStatus(OrderStatuses.IN_TRANSIT);
                } else {
                    order.setStatus(rawStatus);
                }
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setCommission(rs.getDouble("commission"));
                String d = rs.getString("order_date");
                if (d != null && !d.isEmpty()) {
                    order.setDate(LocalDate.parse(d));
                }
                String pa = rs.getString("placed_at");
                if (pa != null && !pa.isEmpty()) {
                    order.setPlacedAt(LocalDateTime.parse(pa, ISO_DT));
                } else {
                    order.setPlacedAt(null);
                }
                loadOrderLines(c, order);
                list.add(order);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAllOrders failed", e);
        }
        return list;
    }

    private void loadOrderLines(Connection c, Order order) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT product_id,product_name,unit_price,quantity FROM order_line_items WHERE order_id=? ORDER BY id")) {
            ps.setString(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qty = rs.getInt("quantity");
                    for (int i = 0; i < qty; i++) {
                        order.getProducts().add(new Product(
                                rs.getString("product_id"),
                                rs.getString("product_name"),
                                0,
                                "",
                                rs.getDouble("unit_price"),
                                ""));
                    }
                }
            }
        }
    }

    @Override
    public void saveProduct(Product product) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT OR REPLACE INTO products (id,name,quantity,category,price,image_path) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, product.getId());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getQuantity());
            ps.setString(4, product.getCategory());
            ps.setDouble(5, product.getPrice());
            ps.setString(6, product.getImagePath() != null ? product.getImagePath() : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("saveProduct failed", e);
        }
    }

    @Override
    public void deleteProduct(String productId) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setString(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("deleteProduct failed", e);
        }
    }

    @Override
    public List<Product> findAllProducts() {
        List<Product> list = new ArrayList<>();
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM products ORDER BY CAST(id AS INTEGER)")) {
            while (rs.next()) {
                list.add(readProduct(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAllProducts failed", e);
        }
        return list;
    }

    @Override
    public void saveUser(User user) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT OR REPLACE INTO users (id,email,password,name,role,pref_customer_name,pref_customer_phone,pref_customer_country,pref_shipping_address) VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getName());
            ps.setString(5, user.getRole());
            ps.setString(6, nz(user.getPrefCustomerName()));
            ps.setString(7, nz(user.getPrefCustomerPhone()));
            ps.setString(8, nz(user.getPrefCustomerCountry()));
            ps.setString(9, nz(user.getPrefShippingAddress()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("saveUser failed", e);
        }
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE LOWER(email)=LOWER(?)")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(readUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findUserByEmail failed", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) {
                list.add(readUser(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAllUsers failed", e);
        }
        return list;
    }

    @Override
    public List<Marketer> findAllMarketers() {
        List<Marketer> list = new ArrayList<>();
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM marketers ORDER BY id")) {
            while (rs.next()) {
                list.add(readMarketer(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAllMarketers failed", e);
        }
        return list;
    }

    @Override
    public void saveMarketer(Marketer marketer) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO marketers (id,name) VALUES (?,?)")) {
            ps.setString(1, marketer.getId());
            ps.setString(2, marketer.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("saveMarketer failed", e);
        }
    }

    @Override
    public void saveMessage(Message message) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT OR REPLACE INTO messages (id,order_id,recipient_email,sender_email,title,content,status,created_at,is_read) "
                             + "VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, message.getId());
            ps.setString(2, message.getOrderId());
            ps.setString(3, message.getRecipientEmail());
            ps.setString(4, message.getSenderEmail());
            ps.setString(5, message.getTitle());
            ps.setString(6, message.getContent());
            ps.setString(7, message.getStatus());
            LocalDateTime at = message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now();
            ps.setString(8, at.format(ISO_DT));
            ps.setInt(9, message.isRead() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("saveMessage failed", e);
        }
    }

    @Override
    public List<Message> findMessagesByRecipient(String recipientEmail) {
        List<Message> list = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM messages WHERE LOWER(recipient_email)=LOWER(?) ORDER BY created_at DESC")) {
            ps.setString(1, recipientEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findMessagesByRecipient failed", e);
        }
        return list;
    }

    @Override
    public List<Message> findAllMessages() {
        List<Message> list = new ArrayList<>();
        try (Connection c = connect();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM messages ORDER BY created_at DESC")) {
            while (rs.next()) {
                list.add(readMessage(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findAllMessages failed", e);
        }
        return list;
    }

    private static Message readMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getString("id"));
        m.setOrderId(rs.getString("order_id"));
        m.setRecipientEmail(rs.getString("recipient_email"));
        m.setSenderEmail(rs.getString("sender_email"));
        m.setTitle(rs.getString("title"));
        m.setContent(rs.getString("content"));
        m.setStatus(rs.getString("status"));
        String ca = rs.getString("created_at");
        if (ca != null && !ca.isEmpty()) {
            m.setCreatedAt(LocalDateTime.parse(ca, ISO_DT));
        }
        m.setRead(rs.getInt("is_read") != 0);
        return m;
    }
}
