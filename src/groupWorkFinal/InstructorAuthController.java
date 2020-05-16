package groupWorkFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class InstructorAuthController
{
    // DATA MEMBERS

    private boolean authInstructor;

    @FXML
    private TextField txtAuthPassword;
    @FXML
    private TextField txtAuthUsername;


    /*
    |   +isAuthInstructor()
    |
    |   Gets authentication
    */
    public boolean isAuthInstructor()
    {
        return authInstructor;
    }


    /*
    |   +setAuthInstructor()
    |
    |   Sets authentication
    */
    public void setAuthInstructor(boolean authInstructor)
    {
        this.authInstructor = authInstructor;
    }


    /*
    |   +onClickBtnAuthOK()
    |
    |   Verifies the login and allows an instructor to be created
    */
    public void onClickBtnAuthOK(ActionEvent e)
    {
        // Verify account
        if (authenticateInstructor())
        {
            authInstructor = true;
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }

        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Incorrect Username/Password");
            alert.setContentText("You have entered an incorrect username/password");

            alert.showAndWait();
        }

    }//END OF ON CLICK BTN AUTH OK


    /*
    |   +onClickBtnAuthCancel()
    |
    |   Cancels authentication and closes window
    */
    public void onClickBtnAuthCancel(ActionEvent e)
    {
        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN AUTH CANCEL


    /*
    |   -authenticateInstructor()
    |
    |   Authenticates instructor account
    */
    private Boolean authenticateInstructor()
    {
        String authUsername = txtAuthUsername.getText();
        String authPassword = txtAuthPassword.getText();

        Database dbAuthConnect = new Database(authUsername, authPassword, false);

        if (dbAuthConnect.checkLogin())
        {
            // Close dbAuthConnect somehow?
            dbAuthConnect.destroy();
            return authInstructor = true;
        }

        return authInstructor = false;
    }//END OF AUTHENTICATE INSTRUCTOR

}//END OF INSTRUCTOR AUTH CONTROLLER
