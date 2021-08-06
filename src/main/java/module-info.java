module gameOfLifeAnimals {

    requires javafx.fxml;
    requires javafx.controls;
    requires json.simple;

//    opens pl.game.visualization to javafx.fxml;
//    exports pl.game.visualization;
    exports pl.game;
    opens pl.game to javafx.fxml;
}