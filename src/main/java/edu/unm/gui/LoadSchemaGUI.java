/**
 * Author: Raju Nayak
 */

package edu.unm.gui;

import edu.unm.dao.ElectionGremlinDAO;

import edu.unm.entity.Ballot;
import edu.unm.entity.PaperBallot;
import edu.unm.service.BallotScanner;
import edu.unm.service.ElectionSetupScanner;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * The LoadSchemaGUI class is responsible for providing a graphical user interface
 * to load and set up an election schema. It handles file selection, schema parsing,
 * and initial configuration of the voting environment.
 * This class integrates with ElectionSetupScanner, BallotScanner, and ElectionGremlinDAO
 * for handling the election schema.
 */

public class LoadSchemaGUI {
    private GUIUtils guiUtils = new GUIUtils();
    private GridPane root;
    private Scene scene;
    private ElectionGremlinDAO dao;

    /**
     * Constructor to initialize the LoadSchemaGUI with a given scene.
     * It sets up the layout and the 'Load Election Schema' button.
     * @param scene the primary scene for this GUI component
     */
    public LoadSchemaGUI(Scene scene) {
        this.scene = scene;
        this.dao = new ElectionGremlinDAO();
        // Set up the layout
        root = guiUtils.createRoot(4, 3);

        // Load election schema button
        Button loadSchemaButton = new Button("Load Election Schema");
        guiUtils.createBtn(loadSchemaButton, 300, 100, 25);
        loadSchemaButton.setOnAction(e -> {
            loadSchema();
        });
        root.add(loadSchemaButton, 1, 1);
    }

    /**
     * Handles the loading of the election schema.
     * This method opens a file chooser to select the election schema file,
     * parses the file, and sets up the election environment, including ballot
     * configuration and DAO initialization.
     */
    private void loadSchema() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Election Schema File");
        File file = fileChooser.showOpenDialog(scene.getWindow());

        if (file != null) {
            try {
                // Parse the Ballot Schema
                ElectionSetupScanner scanner = new ElectionSetupScanner(file.getAbsolutePath());
                Ballot ballot = scanner.parseSchema();
                BallotScanner.setBallot(ballot);

                // Initialize DAO and Load Schema
                ElectionGremlinDAO dao = new ElectionGremlinDAO();
                dao.loadBallotSchema(ballot.getSchemaName(), ballot);

                //Paper Ballot setup
                PaperBallot paperBallot = new PaperBallot();
                try {
                    paperBallot.createPaperBallot();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Enable Voting and Show Confirmation
                Configuration.setGevEnabled(true);
                Configuration.setTabEnabled(true);
                showSuccessPopUp("Voting Enabled", "Ballot is now open, voting can begin.");
            } catch (IOException | SAXException | ParserConfigurationException e) {
                showErrorPopUp("Error Loading File", e.getMessage());
            }
        }
    }

    /**
     * Returns the root grid pane of this GUI.
     * @return GridPane the root layout pane of this GUI component
     */
    public GridPane getRoot() {
        return root;
    }

    /**
     * Displays a success pop-up with a custom title and content.
     * Used to inform the user of successful operations.
     * @param title   the title of the pop-up
     * @param content the content message of the pop-up
     */
    private void showSuccessPopUp(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays an error pop-up with a custom title and content.
     * Used to inform the user of errors or failed operations.
     * @param title   the title of the pop-up
     * @param content the content message of the pop-up
     */
    private void showErrorPopUp(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
