package de.leon.bstcgf;

import de.leon.bstcgf.data.GitHubInformation;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class BstcgfApplication extends Application {

    PropertiesReader mavenProperties;
    GitHubInformation gitHubInformation;

    @Override
    public void start(Stage stage) throws IOException {

        gitHubInformation = Request.getInfoLatestRelease();
        mavenProperties = new PropertiesReader("app.properties");

        if(!isVersionUpToDate()) {
            showNewerVersionPopup();
        }

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(BstcgfApplication.class.getResource("bstcgf-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 780, 480);
        stage.setTitle("Best Steam Trading Card Games Finder");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private boolean isVersionUpToDate() {
        // very lazy implementation of checking the version, but I think it's enough for now
        return gitHubInformation.getVersion().equals(mavenProperties.getProperty("app.versionString"));
    }

    private void showNewerVersionPopup() {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("New release");
        alert.setHeaderText("New Version " + gitHubInformation.getVersion() + " available.");

        ButtonType openButton = new ButtonType("Open GitHub");
        ButtonType downloadButton = new ButtonType("Download");
        ButtonType okButton = new ButtonType("Ok", ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(openButton, downloadButton, okButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == openButton) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(gitHubInformation.getUrl()));
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

        } else if (result.isPresent() && result.get() == downloadButton) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(gitHubInformation.getDownloadUrl()));
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
        alert.close();

    }
}