module de.leon.bstcgf {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires java.annotation;
    requires static lombok;
    requires org.json;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    opens de.leon.bstcgf to javafx.fxml;
    exports de.leon.bstcgf;
    exports de.leon.bstcgf.data.steamcardexchange;
    exports de.leon.bstcgf.data.steam;
    exports de.leon.bstcgf.data;
}