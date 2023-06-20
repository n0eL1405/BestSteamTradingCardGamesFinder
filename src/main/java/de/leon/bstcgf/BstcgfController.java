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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.SearchableComboBox;

public class BstcgfController implements Initializable {

    @FXML
    public ProgressBar progressionBar;

    @FXML
    private VBox vBoxMain;

    @FXML
    private VBox vBoxTop;

    @FXML
    private HBox hBoxTopTop;

    @FXML
    private HBox hBoxTopBottom;

    @FXML
    private SearchableComboBox<CountryCode> countryCodeSearchComboBox;

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
    private TableColumn<TableGameData, Double> tableColumnStatus;

    @FXML
    private Button loadDataButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button resetFilter;

    private final ObservableList<TableGameData> tableGameDataObservableList = FXCollections.observableArrayList();
    private final FilteredList<TableGameData> filteredTableGameDataList = new FilteredList<>(tableGameDataObservableList, tableGameData -> true);
    private final ObservableList<CountryCode> countryCodesObservableList = FXCollections.observableArrayList();

    private final static String STEAM_STORE_URL = "https://store.steampowered.com/app/";
    private final static String STEAMDB_URL = "https://steamdb.info/app/";

    private CountryCode selectedCountryCode;

    private final Settings settings = new Settings("settings_Main");

    private boolean isLoading = false;

    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "controller-thread");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initUI();

    }

    private void initUI() {

        initSearchField();

        initCountryCodeSearchComboBox();

        initTable();

        initProgressionBar();

    }

    private void initCountryCodeSearchComboBox() {

        try {
            countryCodesObservableList.clear();
            countryCodesObservableList.addAll(Request.getSteamCountryCodes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        countryCodeSearchComboBox.setItems(countryCodesObservableList);

        try {
            selectedCountryCode = settings.getCountryCode();
            countryCodeSearchComboBox.setValue(selectedCountryCode);
        } catch (NullPointerException npe) {
            countryCodeSearchComboBox.setValue(countryCodeSearchComboBox.getItems().get(0));
            selectedCountryCode = countryCodesObservableList.get(0);
        }

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
                            settings.saveCountryCode(countryCode);
                        }
                    }
                };
            }
        };

        countryCodeSearchComboBox.setButtonCell(cellFactory.call(null));
        countryCodeSearchComboBox.setCellFactory(cellFactory);

    }

    public void countryCodeSearchComboBoxAction(ActionEvent actionEvent) {
        selectedCountryCode = countryCodeSearchComboBox.getValue();
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

        tableGameDataTableView.setItems(filteredTableGameDataList);
    }

    private class LoadGameDataFromSteamTask extends Task<Void> {

        private final CountryCode countryCode;

        private LoadGameDataFromSteamTask(CountryCode countryCode) {
            this.countryCode = countryCode;
        }

        @Override
        protected Void call() throws Exception {

            try {

                SteamCardExchangeJsonData steamCardExchangeJsonData = Request.getSteamCardExchangeData();
                AtomicReference<Integer> gamesCounter = new AtomicReference<>(0);

                List<SteamCardExchangeGameData> steamCardExchangeGameDataList = new LinkedList<>(
                        steamCardExchangeJsonData.getSteamCardExchangeGameData());

                steamCardExchangeJsonData.getInPackages(100).forEach(packages -> {

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
                            if (sg.getData().getSteamPriceOverview().getInitialPrice()
                                    > 0) {
                                tableGameDataObservableList.add(
                                        new TableGameData(sg, steamCardExchangeGameData, settings.getStatusByGameId(sg.getId())));
                            }

                            gamesCounter.set(gamesCounter.get() + 1);
                            double progress = calcProgressDouble(gamesCounter.get(),
                                    steamCardExchangeGameDataList.size());
                            progressionBar.setProgress(progress);
                        });

                        // sorting the list
                        // - the first sorting factor is the rating, going from lowest to highest rating (the lower the rating the better)
                        // - the second sorting factor is the number of cards, going from lowest to highest number, in case multiple games have the same rating
                        // - the last sorting factor is the name, in case multiple games have the same rating and number of cards
                        Comparator<TableGameData> tableGameDataComparator = Comparator
                                .comparing(TableGameData::getRating)
                                .thenComparing(TableGameData::getCards)
                                .thenComparing(TableGameData::getName);
                        tableGameDataObservableList.sort(tableGameDataComparator);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    public void loadDataAction(ActionEvent actionEvent) {
        //loadData();
        deactivateWhileLoading();
        isLoading = true;

        tableGameDataObservableList.clear();

        LoadGameDataFromSteamTask loadGameDataFromSteamTask = new LoadGameDataFromSteamTask(
                selectedCountryCode);

        loadGameDataFromSteamTask.setOnSucceeded(wse -> {
            activateAfterLoading();
            isLoading = false;
        });

        loadGameDataFromSteamTask.setOnFailed(wse -> {
            loadGameDataFromSteamTask.getException().printStackTrace();
            activateAfterLoading();
            isLoading = false;
        });

        executor.execute(loadGameDataFromSteamTask);
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

        tableColumnStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
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

                    MenuItem setStatusPurchased = new MenuItem("Set Status \"Purchased\"");
                    MenuItem setStatusWishlisted = new MenuItem("Set Status \"Wishlisted\"");
                    MenuItem setStatusIgnored = new MenuItem("Set Status \"Ignored\"");
                    MenuItem setStatusNone = new MenuItem("Set Status \"None\"");

                    contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {

                            // if the status of the row (game) equals the status that the MenuItem will set the row (game) to OR if isLoading is true, the MenuItem will be disabled
                            setStatusPurchased.setDisable(row.getItem().getStatus().toUpperCase().equals(TableGameData.Status.PURCHASED.toString()) || isLoading);
                            setStatusWishlisted.setDisable(row.getItem().getStatus().toUpperCase().equals(TableGameData.Status.WISHLISTED.toString()) || isLoading);
                            setStatusIgnored.setDisable(row.getItem().getStatus().toUpperCase().equals(TableGameData.Status.IGNORED.toString()) || isLoading);
                            setStatusNone.setDisable(row.getItem().getStatus().toUpperCase().equals(TableGameData.Status.NONE.toString()) || isLoading);
                        }
                    });

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

                    setStatusPurchased.setOnAction(actionEvent -> {
                        if (!row.isEmpty()) {
                            tableGameDataObservableList.stream()
                                    .filter(sg -> sg.equals(row.getItem()))
                                    .findFirst()
                                    .get()
                                    .setStatus(TableGameData.Status.PURCHASED.toString());
                        }
                        settings.saveGameIdByStatus(TableGameData.Status.PURCHASED, row.getItem());
                    });

                    setStatusWishlisted.setOnAction(actionEvent -> {
                        if (!row.isEmpty()) {
                            tableGameDataObservableList.stream()
                                    .filter(sg -> sg.equals(row.getItem()))
                                    .findFirst()
                                    .get()
                                    .setStatus(TableGameData.Status.WISHLISTED.toString());
                        }
                        settings.saveGameIdByStatus(TableGameData.Status.WISHLISTED, row.getItem());
                    });

                    setStatusIgnored.setOnAction(actionEvent -> {
                        if (!row.isEmpty()) {
                            tableGameDataObservableList.stream()
                                    .filter(sg -> sg.equals(row.getItem()))
                                    .findFirst()
                                    .get()
                                    .setStatus(TableGameData.Status.IGNORED.toString());
                        }
                        settings.saveGameIdByStatus(TableGameData.Status.IGNORED, row.getItem());
                    });

                    setStatusNone.setOnAction(actionEvent -> {
                        if (!row.isEmpty()) {
                            tableGameDataObservableList.stream()
                                    .filter(sg -> sg.equals(row.getItem()))
                                    .findFirst()
                                    .get()
                                    .setStatus(TableGameData.Status.NONE.toString());
                        }
                        settings.saveGameIdByStatus(TableGameData.Status.NONE, row.getItem());
                    });

                    contextMenu.getItems()
                            .addAll(copyName, copyID, copyCards, copyPrice, copyRating,
                                    new SeparatorMenuItem(), copySteamShopLink, copySteamDbLink,
                                    new SeparatorMenuItem(), setStatusPurchased, setStatusWishlisted, setStatusIgnored, setStatusNone);

                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(contextMenu)
                    );

                    return row;
                }
        );
    }

    private void initProgressionBar() {

        progressionBar.autosize();

    }

    private Double calcProgressDouble(Integer currentProgress, Integer fullProgress) {
        return ((double) currentProgress) / ((double) fullProgress);
    }

    private void initSearchField() {

        //filteredTableGameDataList = new FilteredList<>(tableGameDataObservableList, tableGameData -> true);

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.trim().isEmpty() || !newValue.trim().isBlank()) {

                String searchFieldText = newValue.trim().toLowerCase();

                Predicate<TableGameData> containsSearchText = item ->
                        item.getName().toLowerCase().contains(searchFieldText)
                                || item.getId().toString().toLowerCase().contains(searchFieldText)
                                || item.getCardsString().toLowerCase().contains(searchFieldText)
                                || item.getPrice().toLowerCase().contains(searchFieldText)
                                || item.getRating().toString().toLowerCase().contains(searchFieldText);

                filteredTableGameDataList.setPredicate(containsSearchText);

            } else {

                filteredTableGameDataList.setPredicate(null);

            }
        });
    }

    public void resetFilterAction(ActionEvent actionEvent) {
        searchTextField.setText("");
        filteredTableGameDataList.setPredicate(null);
    }

    private void deactivateWhileLoading() {
        loadDataButton.setDisable(true);
        countryCodeSearchComboBox.setDisable(true);
        searchTextField.setDisable(true);
        resetFilter.setDisable(true);
    }

    private void activateAfterLoading() {
        loadDataButton.setDisable(false);
        countryCodeSearchComboBox.setDisable(false);
        searchTextField.setDisable(false);
        resetFilter.setDisable(false);
    }
}