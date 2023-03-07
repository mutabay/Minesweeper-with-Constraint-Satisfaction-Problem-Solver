module com.csp.final_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.chocosolver.solver;
    opens main to javafx.fxml;
    exports main;
}