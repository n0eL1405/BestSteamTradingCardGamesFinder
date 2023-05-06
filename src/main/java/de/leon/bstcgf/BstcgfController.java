package de.leon.bstcgf;

import de.leon.bstcgf.data.CountryCode;
import de.leon.bstcgf.data.TableGameData;
import de.leon.bstcgf.data.steam.SteamGame;
import de.leon.bstcgf.data.steam.SteamJsonData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeGameData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeJsonData;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.TextFields;

public class BstcgfController implements Initializable {

    @FXML
    VBox vBox;

    @FXML
    HBox hBox;

    @FXML
    SearchableComboBox<CountryCode> countryCodeSearchComboBox;

    @FXML
    private TableView<TableGameData> tableGameDataTableView;

    @FXML
    private TableColumn<TableGameData, String> tableColumnName;

    @FXML
    private TableColumn<TableGameData, Integer> tableColumnId;

    @FXML
    private TableColumn<TableGameData, Integer> tableColumnCards;

    @FXML
    private TableColumn<TableGameData, String> tableColumnPrice;

    @FXML
    private TableColumn<TableGameData, Double> tableColumnRating;

    @FXML
    private Button loadDataButton;

    private ObservableList<TableGameData> tableGameDataObservableList = FXCollections.observableArrayList();
    private final ObservableList<CountryCode> countryCodesObservableList = FXCollections.observableArrayList();

    private final static String STEAM_STORE_URL = "https://store.steampowered.com/app/";
    private final static String STEAMDB_URL = "https://steamdb.info/app/";

    private CountryCode selectedCountryCode;

    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "controller-thread");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            countryCodesObservableList.clear();
            countryCodesObservableList.addAll(Request.getSteamCountryCodes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        selectedCountryCode = countryCodesObservableList.get(0);

        initUI();

    }

    private void initUI() {

        initDropDown();

        initTable();

    }

    private void initTable() {

        setCellValueFactories();

        createContextMenu();

        /* This should set the column width to a percentage of the table width, but because the table doesn't have a fixed size, widthProperty returns 0.0
        tableColumnName.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(2/6));
        tableColumnId.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnCards.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnPrice.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnRating.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
         */

        tableGameDataTableView.setItems(tableGameDataObservableList);
    }

    public void clickAction(ActionEvent actionEvent) {
        //loadData();
        countryCodeSearchComboBox.setDisable(true);

        LoadGameDataFromSteamTask loadGameDataFromSteamTask = new LoadGameDataFromSteamTask(
            selectedCountryCode);

        loadGameDataFromSteamTask.setOnSucceeded(wse -> {
            tableGameDataObservableList.setAll(
                new ArrayList<>(loadGameDataFromSteamTask.getValue()));
            countryCodeSearchComboBox.setDisable(false);
        });

        loadGameDataFromSteamTask.setOnFailed(wse -> {
                loadGameDataFromSteamTask.getException().printStackTrace();
                countryCodeSearchComboBox.setDisable(true);
            });

        executor.execute(loadGameDataFromSteamTask);
    }

    @Deprecated
    private void loadData() {

        tableGameDataObservableList.clear();
        List<TableGameData> tableGameDataList = new LinkedList<>();
        List<SteamCardExchangeGameData> steamCardExchangeGameDataList;
        List<SteamGame> steamGameList = new LinkedList<>();

        try {
            SteamCardExchangeJsonData steamCardExchangeJsonData = Request.getSteamCardExchangeData();
            steamCardExchangeGameDataList = new LinkedList<>(
                steamCardExchangeJsonData.getSteamCardExchangeGameData());
            steamCardExchangeJsonData.getInPackages(100).forEach(packages -> {
                try {
                    SteamJsonData steamJsonData = Request.getGameDataFromSteamIds(
                        packages.getOnlyIds(), selectedCountryCode);
                    steamGameList.addAll(steamJsonData.getSteamGames());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        steamCardExchangeGameDataList.forEach(scegd -> {

            SteamGame steamGame = steamGameList.stream().filter(sg -> sg.getId() == scegd.getId())
                .findFirst().orElseThrow();

            // skip if a game is free2play because you can only obtain 1 cord for ~10$ spend;
            // using initial price should still add free2keep games in the list;
            if (steamGame.getData().getSteamPriceOverview().getInitialPrice() > 0) {
                TableGameData tableGameData = new TableGameData(steamGame, scegd);
                tableGameDataList.add(tableGameData);
            }
        });

        tableGameDataObservableList.addAll(tableGameDataList
            .stream()
            .sorted(Comparator.comparing(TableGameData::getName))
            .sorted(Comparator.comparing(TableGameData::getRating))
            .collect(Collectors.toList())
        );
    }

    private void setCellValueFactories() {

        tableColumnName.setCellValueFactory(
            new PropertyValueFactory<>("name")
        );

        tableColumnId.setCellValueFactory(
            new PropertyValueFactory<>("id")
        );

        tableColumnCards.setCellValueFactory(
            new PropertyValueFactory<>("cardsString")
        );

        tableColumnPrice.setCellValueFactory(
            new PropertyValueFactory<>("price")
        );

        tableColumnRating.setCellValueFactory(
            new PropertyValueFactory<>("rating")
        );
    }

    private void createContextMenu() {

        tableGameDataTableView.setRowFactory(tableGameDataTableView -> {

                final TableRow<TableGameData> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                MenuItem copyName = new MenuItem("Copy Name");
                MenuItem copyID = new MenuItem("Copy ID");
                MenuItem copyCards = new MenuItem("Copy Cards");
                MenuItem copyPrice = new MenuItem("Copy Price");
                MenuItem copyRating = new MenuItem("Copy Rating");
                MenuItem copySteamShopLink = new MenuItem("Copy Steam Shop Link");
                MenuItem copySteamDbLink = new MenuItem("Copy SteamDB Link");

                SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

                copyName.setOnAction(actionEvent -> clipboard.setContents(
                    new StringSelection(
                        row.isEmpty() ? "" : row.getItem().getName()
                    ), null));

                copyID.setOnAction(actionEvent -> clipboard.setContents(
                    new StringSelection(
                        row.isEmpty() ? "" : row.getItem().getId().toString()
                    ), null));

                copyCards.setOnAction(actionEvent -> clipboard.setContents(
                    new StringSelection(
                        row.isEmpty() ? "" : row.getItem().getCardsString()
                    ), null
                ));

                copyPrice.setOnAction(actionEvent -> clipboard.setContents(
                    new StringSelection(
                        row.isEmpty() ? "" : row.getItem().getPrice()
                    ), null
                ));

                copyRating.setOnAction(actionEvent -> clipboard.setContents(
                    new StringSelection(
                        row.isEmpty() ? "" : row.getItem().getRating().toString()
                    ), null
                ));

                copySteamShopLink.setOnAction(actionEvent -> {
                    StringBuilder stringBuilder = new StringBuilder()
                        .append(STEAM_STORE_URL);

                    clipboard.setContents(
                        new StringSelection(
                            row.isEmpty() ? "" : stringBuilder.append(row.getItem().getId()).toString()
                        ), null
                    );
                });

                copySteamDbLink.setOnAction(actionEvent -> {
                    StringBuilder stringBuilder = new StringBuilder()
                        .append(STEAMDB_URL);

                    clipboard.setContents(
                        new StringSelection(
                            row.isEmpty() ? "" : stringBuilder.append(row.getItem().getId()).toString()
                        ), null
                    );
                });

                contextMenu.getItems()
                    .addAll(copyName, copyID, copyCards, copyPrice, copyRating,
                        separatorMenuItem, copySteamShopLink, copySteamDbLink);

                row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
                );

                return row;
            }
        );
    }

    private void initDropDown() {

        countryCodeSearchComboBox.setItems(countryCodesObservableList);
        countryCodeSearchComboBox.setValue(countryCodeSearchComboBox.getItems().get(0));

        Callback<ListView<CountryCode>, ListCell<CountryCode>> cellFactory = new Callback<>() {
            @Override
            public ListCell<CountryCode> call(ListView<CountryCode> countryCodeListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(CountryCode countryCode, boolean empty) {
                        super.updateItem(countryCode, empty);

                        if (countryCode == null || empty) {
                            setText(null);
                        } else {
                            setText(countryCode.getLabel());
                        }
                    }
                };
            }
        };

        countryCodeSearchComboBox.setButtonCell(cellFactory.call(null));
        countryCodeSearchComboBox.setCellFactory(cellFactory);

    }

    @Deprecated
    private List<CountryCode> matchingItems(List<CountryCode> allItems, String searchTerm) {
        List<CountryCode> matches = new ArrayList<>();
        allItems.forEach(cc -> {
            if (cc.getCode().toLowerCase().contains(searchTerm.toLowerCase())
                || cc.getLabel().toLowerCase().contains(searchTerm.toLowerCase())) {
                matches.add(cc);
            }
        });
        return matches;
    }

    public void searchComboAction(ActionEvent actionEvent) {
        selectedCountryCode = countryCodeSearchComboBox.getValue();
    }

    private static class LoadGameDataFromSteamTask extends Task<ObservableList<TableGameData>> {

        ObservableList<TableGameData> steamGames = FXCollections.observableArrayList();
        private final CountryCode countryCode;

        private LoadGameDataFromSteamTask(CountryCode countryCode) {
            this.countryCode = countryCode;
        }

        @Override
        protected ObservableList<TableGameData> call() throws Exception {

            try {

                SteamCardExchangeJsonData steamCardExchangeJsonData = Request.getSteamCardExchangeData();

                List<SteamCardExchangeGameData> steamCardExchangeGameDataList = new LinkedList<>(
                    steamCardExchangeJsonData.getSteamCardExchangeGameData());

                steamCardExchangeJsonData.getInPackages(100).forEach(packages -> {

                    List<TableGameData> tableGameDataList = new LinkedList<>();

                    try {
                        SteamJsonData steamJsonData = Request.getGameDataFromSteamIds(
                            packages.getOnlyIds(), countryCode);
                        List<SteamGame> steamGameList = new LinkedList<>(
                            steamJsonData.getSteamGames());

                        steamGameList.forEach(sg -> {

                            SteamCardExchangeGameData steamCardExchangeGameData = steamCardExchangeGameDataList.stream()
                                .filter(scegd -> scegd.getId() == sg.getId())
                                .findFirst().orElseThrow();

                            // skip if a game is free2play because you can only obtain 1 cord for ~10$ spend;
                            // using initial price should still add free2keep games in the list;
                            if (sg.getData().getSteamPriceOverview().getInitialPrice() > 0) {
                                tableGameDataList.add(
                                    new TableGameData(sg, steamCardExchangeGameData));
                            }
                        });

                        steamGames.addAll(tableGameDataList);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                // sorting the list
                steamGames.setAll(steamGames
                    .stream()
                    .sorted(Comparator.comparing(TableGameData::getName))
                    .sorted(Comparator.comparing(TableGameData::getRating))
                    .collect(Collectors.toList())
                );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return steamGames;
        }
    }
}