package edu.unm.gui;

import edu.unm.dao.DAOFactory;
import edu.unm.dao.ElectorDAO;
import edu.unm.entity.Elector;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

/**
 * This class is just basic user input for when
 * someone wants to register as voter, still need
 * to talk to front end to see how this should communicate
 * with backend
 */
public class VoterReg {
    private final GridPane root;

    public VoterReg() {
        ElectorDAO electorDAO = DAOFactory.create(ElectorDAO.class);
        GUIUtils guiUtils = new GUIUtils();
        root = guiUtils.createRoot(6, 3);

        Label firstName = new Label("First Name: ");
        TextField firstNameField = new TextField();

        Label lastName = new Label("Last Name: ");
        TextField lastNameField = new TextField();

        Label DOB = new Label("DOB mm/dd/yyyy: ");
        TextField dobField = new TextField();

        Label social = new Label("Social Security #: ");
        PasswordField socialField = new PasswordField();

        Button registerButton = new Button("Submit");

        guiUtils.createLabel(firstName,250,100,25);
        guiUtils.createLabel(lastName,250,100,25);
        guiUtils.createLabel(DOB, 250, 100,  25);
        guiUtils.createLabel(social, 250, 100, 25);
        guiUtils.createTextField(firstNameField, 250, 100, 25);
        guiUtils.createTextField(lastNameField, 250, 100, 25);
        guiUtils.createTextField(dobField,250,100,25);
        guiUtils.createPasswordField(socialField,250,100);
        guiUtils.createBtn(registerButton,250,100,25);

        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        registerButton.setOnAction(e -> {
            // Handle register button click event
            String first = firstNameField.getText();
            String last = lastNameField.getText();
            String id = socialField.getText();
            Date dob;

            // VALIDATE D.O.B.
            try {
                dob = new Date(inputFormat.parse(dobField.getText()).getTime());
                String formattedDate = dateFormat.format(dob);
                dob = Date.valueOf(formattedDate);
            } catch (ParseException ex) {
                showPopup("Invalid Date of Birth", "Please enter a valid date in the format mm/dd/yyyy");
                return;
            }

            // VALIDATE SOCIAL SECURITY
            if (id.length() != 9) {
                showPopup("Invalid Social Security Number", "Please enter a 9-digit social security number");
                return;
            }

            // Make sure the person registering is not already registered
            Elector elector = new Elector(first, last, id, dob);
            Optional<Elector> electors;
            try {
                 electors = electorDAO.getElectorBySocial(elector.getId());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            if (electors.isPresent()){
                showPopup("Already Registered", "You have already registered.");
                return;
            }

            // If registrant is < 17 years old, they are ineligible to register
            if (!elector.isQualifiedToRegister()){
                showPopup("Ineligible Registration", "You must be at least 17 years old to register.");
                return;
            }

            // After validating age, social security & that registrant is not already registered, registration
            // can finally be concluded and elector info stored in elector database
            try {
                electorDAO.addElector(elector);
                showSuccessPopUp("Success", "You have successfully registered!");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        });

        root.add(firstName, 0, 1);
        root.add(firstNameField, 1, 1);
        root.add(lastName, 0, 2);
        root.add(lastNameField, 1, 2);
        root.add(DOB, 0, 3);
        root.add(dobField, 1, 3);
        root.add(social, 0, 4);
        root.add(socialField, 1, 4);
        root.add(registerButton, 2, 5);
    }

    private void showPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessPopUp(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public GridPane getRoot() {return root;}
}
