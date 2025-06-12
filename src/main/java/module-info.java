module org.example.atpprojectpartc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.atpprojectpartc to javafx.fxml;
    exports org.example.atpprojectpartc;
}