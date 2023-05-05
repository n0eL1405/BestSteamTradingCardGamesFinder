module de.leon.bstcgf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.annotation;
    requires static lombok;
    requires org.json;

    opens de.leon.bstcgf to javafx.fxml;
    exports de.leon.bstcgf;
    exports de.leon.bstcgf.data.steamcardexchange;
    exports de.leon.bstcgf.data.steam;
    exports de.leon.bstcgf.data;
}