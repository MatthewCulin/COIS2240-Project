package groupWorkFinal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.event.ActionEvent;
import javafx.stage.*;
import java.io.IOException;

public class LoginController
{
    // DATA MEMBERS

    private Database dBConnect;
    private int loginAttempt = 0;

    @FXML
    private Label lblStatus;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private CheckBox chkInstructor;

    /*
    |   +onClickBtnLogin
    |
    |   Check with the Database which user is logging in and open the appropriate screen
    |   StudentContoller if student
    |   InstructorController if instructor
    */
    public void onClickBtnLogin(ActionEvent e) throws IOException
    {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        boolean isStudent = !chkInstructor.isSelected();

        dBConnect = new Database(username, password, isStudent);

        // FOR TESTING ONLY RUN ONCE
        //dBConnect.populateTestData();

        // Check user credentials
        if (dBConnect.checkLogin())
        {
            loginAttempt = 0;

            // Login is a Student
            if (isStudent)
            {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("StudentView.fxml"));
                Parent studentView = loader.load();

                Scene studentViewScene = new Scene(studentView);

                //Set database from login
                StudentController controller = loader.getController();
                controller.setDbConnection(dBConnect);

                // Initialize the controller with the database
                controller.initStudent();

                /* END CHANGES to push connection */

                Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
                window.setTitle("Student View");
                window.setScene(studentViewScene);
                window.show();
            }

            else
            {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("InstructorView.fxml"));
                Parent instructorView = loader.load();

                Scene instructorViewScene = new Scene(instructorView);

                //Set database from login
                InstructorController controller = loader.getController();
                controller.setDbConnection(dBConnect);

                // Initialize the controller with the database
                controller.initInstructor();

                /* END CHANGES to push connection */

                Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
                window.setTitle("Instructor View");
                window.setScene(instructorViewScene);
                window.show();
            }
        }

        else
        {
            if (!txtUsername.getText().equals(""))
            {
                lblStatus.setText("Incorrect username/Password");
                lblStatus.setVisible(true);
            }

            else
            {
                lblStatus.setText("Login Failure");
                lblStatus.setVisible(true);
            }

            loginAttempt++;
        }

        // Close Program after 5 incorrect attempts
        if (loginAttempt >= 5)
        {
            System.exit(0);
        }
    }// END OF ON CLICK BTN LOGIN


    /*
    |   +onClickBtnCreateAccount()
    |
    |   When Create Account button is clicked load the CreateAccountForm.fxml
    |   AccountCreator class is controller
    */
    public void onClickBtnCreateAccount(ActionEvent e) throws IOException
    {
        Parent createAccount = FXMLLoader.load(getClass().getResource("CreateAccountForm.fxml"));

        Scene createAccountScene = new Scene(createAccount);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setTitle("New Account");
        window.setScene(createAccountScene);
        window.show();
    }//END OF ON CLICK BTN CREATE ACCOUNT

}//END OF LOGIN CONTROLLER
