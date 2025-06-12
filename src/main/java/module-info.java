module org.example.atpprojectpartc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires ATPProjectJAR;


    opens org.example.atpprojectpartc to javafx.fxml;
    exports org.example.atpprojectpartc;
}