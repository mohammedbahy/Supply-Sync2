module com.library.supplysync {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.library.supplysync to javafx.fxml;
    opens com.supplysync.presentation to javafx.fxml;
    exports com.library.supplysync;
    exports com.supplysync.presentation;
    exports com.supplysync.facade;
    exports com.supplysync.dashboard;
    exports com.supplysync.models;
    exports com.supplysync.repository;
}