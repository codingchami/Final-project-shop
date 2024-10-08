package com.dev.pos.controller;

import com.dev.pos.Enum.BoType;
import com.dev.pos.bo.BoFactory;
import com.dev.pos.bo.custom.CustomerBo;
import com.dev.pos.dao.DatabaseAccessCode;
import com.dev.pos.dto.CustomerDTO;
import com.dev.pos.dto.tm.CustomerTm;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerFormController {

    public AnchorPane context;
    public JFXTextField txtEmail;
    public JFXTextField txtContact;
    public JFXTextField txtName;
    public JFXTextField txtSalary;
    public JFXButton btnSave;
    public JFXTextField txtSearch;
    public TableView<CustomerTm> tblCustomer;
    public TableColumn<CustomerTm,Integer> colNo;
    public TableColumn<CustomerTm,String> colName;
    public TableColumn<CustomerTm,String> colEmail;
    public TableColumn<CustomerTm,String> colContact;
    public TableColumn<CustomerTm,Double> colSalary;
    public TableColumn<CustomerTm, Button> colDelete;

    String SearchText ="";

    CustomerBo customerBo = BoFactory.getInstance().getBo(BoType.CUSTOMER);

    public void initialize(){
        colNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colDelete.setCellValueFactory(new PropertyValueFactory<>("button"));

        loadCustomer(SearchText);
        //listner for search bar
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            SearchText = newValue;
            loadCustomer(SearchText);
        });

        //listner for table
        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                setData(newValue);
            }
        });
    }

    private void setData(CustomerTm newValue) {
        txtEmail.setEditable(false);
        btnSave.setText("Update Customer");

        txtName.setText(newValue.getName());
        txtContact.setText(newValue.getContact());
        txtSalary.setText(String.valueOf(newValue.getSalary()));
        txtEmail.setText(newValue.getEmail());
    }


    public void btnBackToHome(ActionEvent actionEvent) throws IOException {
        setUI("DashboardForm");
    }

    public void btnSaveOnAction(ActionEvent actionEvent) {
        CustomerDTO dto = new CustomerDTO(
                txtEmail.getText(),
                txtName.getText(),txtContact.getText(),
                Double.valueOf(txtSalary.getText())
        );
try {
    if (btnSave.getText().equalsIgnoreCase("Save Customer")) {

//        CustomerDTO customer = new DatabaseAccessCode().findCustomer(txtEmail.getText());
        CustomerDTO customer = customerBo.findCustomer(txtEmail.getText());



        if (customer != null) {
            new Alert(Alert.AlertType.INFORMATION,"Customer is already saved!.......").show();
            }else{
                boolean isSaved = customerBo.saveCustomer(dto);

                if(isSaved){
                    new Alert(Alert.AlertType.INFORMATION,"Customer has been saved....!").show();
                    clearFields();
                    loadCustomer(SearchText);
                }else{
                    new Alert(Alert.AlertType.INFORMATION,"Something went wrong,Try again.....!").show();
                }
            }
        }else{
         boolean isUpdated =customerBo.updateCustomer(dto);
         if(isUpdated){
             new Alert(Alert.AlertType.INFORMATION,"Customer has been updated!...").show();
             txtEmail.setEditable(true);
             btnSave.setText("Save Customer");
             clearFields();
             loadCustomer(SearchText);
         }
    }
    } catch (ClassNotFoundException | SQLException e){
        e.printStackTrace();

    }

    }



    private void setUI(String location) throws IOException {
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.show();
        stage.centerOnScreen();
    }
    private void clearFields() {
        txtEmail.clear();
        txtName.clear();
        txtContact.clear();
        txtSalary.clear();
    }

    private void loadCustomer(String SearchText) {
        ObservableList<CustomerTm> oblist = FXCollections.observableArrayList();
        try{
            int counter = 1;
            for(CustomerDTO dto : (txtSearch.getLength()>0?customerBo.searchCustomer(SearchText):customerBo.findAllCustomers())){
                Button button = new Button("Delete");
                CustomerTm customerTm = new CustomerTm(
                        counter,
                        dto.getEmail(),
                        dto.getName(),
                        dto.getContact(),
                        dto.getSalary(),
                        button
                );
                counter++;
                oblist.add(customerTm);

                //Customer Delete
                button.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"Are you sure..?", ButtonType.YES,ButtonType.NO);
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if(buttonType.get().equals(ButtonType.YES)){
                        try{
                            boolean isDeleted = customerBo.deleteCustomer(dto.getEmail());
                            if(isDeleted){
                                new Alert(Alert.AlertType.INFORMATION,"Customer has been Deleted....!").show();
                                loadCustomer(SearchText);
                                clearFields();
                                btnSave.setText("Save Customer");

                            }
                        }catch (ClassNotFoundException | SQLException e){
                            e.printStackTrace();
                        }

                    }
                });

            }
            tblCustomer.setItems(oblist);

        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }

    }

}
