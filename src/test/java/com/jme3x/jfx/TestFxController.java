/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import java.lang.String;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Heist
 */
public class TestFxController {

    @FXML
    private TableView<String> testTable;
    @FXML
    private TableColumn<String, String> testNameColumn;
    @FXML
    private TableColumn<String, String> testIdColumn;
    @FXML
    private TableColumn<String, String> testLengthColumn;
    @FXML
    private TextField saveFileField;
    @FXML
    private TableView<String> testTable2;
    @FXML
    private TableColumn<String, String> test2NameCol;
    @FXML
    private TableColumn<String, String> test2ValueCol;

    public void initialize() {
        initTables();
        testTable.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
            @Override
            public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                System.out.print("1 passiert");
            }
        });
    }

    private void initTables() {
        testIdColumn
                .setCellValueFactory(new PropertyValueFactory<String, String>(
                "CaptionTest1"));
        testNameColumn
                .setCellValueFactory(new PropertyValueFactory<String, String>(
                "CaptionTest2"));
        testLengthColumn
                .setCellValueFactory(new PropertyValueFactory<String, String>(
                "CaptionTest3"));
        test2NameCol
                .setCellValueFactory(new PropertyValueFactory<String, String>(
                "CaptionTest4"));
        test2ValueCol
                .setCellValueFactory(new PropertyValueFactory<String, String>(
                "CaptionTest5"));
        testTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        testTable2.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        saveFileField.setText("Hello world");
        testTable.setItems(createData());

    }

    @FXML
    private void handleTestButton() {
        Stage stage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.AZURE);

        Rectangle r = new Rectangle(25, 25, 250, 250);
        r.setFill(Color.BLACK);
        root.getChildren().add(r);

        stage.setScene(scene);
        stage.setTitle("My modal window");
        stage.initModality(Modality.WINDOW_MODAL);

        stage.show();
    }

    @FXML
    private void handleTestButton2() {
    }

    private ObservableList<String> createData() {
        String[] content = {"asd", "asd", "asd", "asd", "asd",
            "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd", "asd"};
        ObservableList<String> result = FXCollections
                .observableArrayList();
        result.addAll(Arrays.asList(content));
        return result;
    }
}
