package com.stocksim.ui;

import com.stocksim.core.MarketNode;
import com.stocksim.data.SystemState;
import com.stocksim.data.Trade;
import com.stocksim.net.MarketNodeRemote;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainUI extends Application {

    private MarketNodeRemote marketNode;
    private ScheduledExecutorService scheduler;
    private final ObservableList<Trade> tradeData = FXCollections.observableArrayList();
    private final ObservableList<String> agentStatusData = FXCollections.observableArrayList();
    private Label marketStatusLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        try {
            this.marketNode = (MarketNodeRemote) Naming.lookup("//localhost/" + MarketNode.RMI_NAME);
        } catch (Exception e) {
            this.marketNode = null;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Distributed Stock Trading Simulation");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(createTradeTable());
        root.setRight(createStatusPanel());
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
        startUiUpdateTask();
    }

    private TableView<Trade> createTradeTable() {
        TableView<Trade> table = new TableView<>(tradeData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Trade, Long> ltCol = new TableColumn<>("Lamport Time");
        ltCol.setCellValueFactory(new PropertyValueFactory<>("lamportTimestamp"));
        TableColumn<Trade, String> agentCol = new TableColumn<>("Agent ID");
        agentCol.setCellValueFactory(new PropertyValueFactory<>("agentId"));
        TableColumn<Trade, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getQuantity() > 0 ? "BUY" : "SELL"));
        TableColumn<Trade, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("stockSymbol"));
        TableColumn<Trade, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Trade, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("%.2f", price));
            }
        });
        TableColumn<Trade, String> timeCol = new TableColumn<>("Real Time");
        timeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(new SimpleDateFormat("HH:mm:ss").format(new Date(cell.getValue().getSystemTimeMillis()))));
        table.getColumns().setAll(ltCol, agentCol, typeCol, symbolCol, qtyCol, priceCol, timeCol);
        return table;
    }

    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(0, 0, 0, 10));
        statusPanel.setAlignment(Pos.TOP_CENTER);
        statusPanel.setMinWidth(200);
        Label marketTitle = new Label("Market Node Status");
        marketTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        marketStatusLabel = new Label("UNKNOWN");
        marketStatusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label agentTitle = new Label("Agent Statuses");
        agentTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        ListView<String> agentStatusList = new ListView<>(agentStatusData);
        statusPanel.getChildren().addAll(marketTitle, marketStatusLabel, new Separator(), agentTitle, agentStatusList);
        return statusPanel;
    }

    private void startUiUpdateTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (marketNode == null) throw new RemoteException("MarketNode not connected.");
                SystemState state = marketNode.getState();
                Platform.runLater(() -> updateUiComponents(state));
            } catch (RemoteException e) {
                Platform.runLater(this::setMarketNodeDownStatus);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateUiComponents(SystemState state) {
        tradeData.setAll(state.getRecentTrades());
        agentStatusData.setAll(state.getAgentStatuses().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .sorted().collect(Collectors.toList()));
        marketStatusLabel.setText(state.getMarketNodeStatus());
        marketStatusLabel.setTextFill("UP".equalsIgnoreCase(state.getMarketNodeStatus()) ? Color.GREEN : Color.RED);
    }

    private void setMarketNodeDownStatus() {
        marketStatusLabel.setText("DOWN");
        marketStatusLabel.setTextFill(Color.RED);
        agentStatusData.clear();
    }

    @Override
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
    }
}
