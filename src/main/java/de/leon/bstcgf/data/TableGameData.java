package de.leon.bstcgf.data;

import de.leon.bstcgf.data.steam.SteamGame;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeGameData;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Data;

@Data
public class TableGameData {

    private final SimpleStringProperty name;
    private final SimpleIntegerProperty id;
    private int cards;
    private final SimpleStringProperty cardsString;
    private final SimpleStringProperty price;
    private final SimpleDoubleProperty rating;

    public TableGameData(SteamGame steamGame, SteamCardExchangeGameData steamCardExchangeGameData) {
        this.name = new SimpleStringProperty(steamCardExchangeGameData.getName());
        this.id = new SimpleIntegerProperty(steamGame.getId());
        this.cards = steamCardExchangeGameData.getTradingCards();
        this.price = new SimpleStringProperty(
            steamGame.getData().getSteamPriceOverview().getFinalPriceFormatted());
        this.rating = new SimpleDoubleProperty(
            calcRating(steamGame.getData().getSteamPriceOverview().getFinalPrice(),
                steamCardExchangeGameData.getTradingCards()));

        StringBuilder cardsStringBuilder = new StringBuilder()
            .append(steamCardExchangeGameData.getTradingCards())
            .append(" (")
            .append(calcObtainableCards(steamCardExchangeGameData.getTradingCards()))
            .append(")");

        this.cardsString = new SimpleStringProperty(cardsStringBuilder.toString());

    }

    private Double calcRating(Integer price, Integer cards) {
        return (double) price / calcObtainableCards(cards);
    }

    private int calcObtainableCards(Integer cards) {
        return (int) Math.ceil((double) cards / 2);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getCards() {
        return cards;
    }

    public void setCards(int cards) {
        this.cards = cards;
    }

    public String getCardsString() {
        return cardsString.get();
    }

    public SimpleStringProperty cardsStringProperty() {
        return cardsString;
    }

    public void setCardsString(String cardsString) {
        this.cardsString.set(cardsString);
    }

    public String getPrice() {
        return price.get();
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public void setPrice(String price) {
        this.price.set(price);
    }

    public double getRating() {
        return rating.get();
    }

    public SimpleDoubleProperty ratingProperty() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }
}
