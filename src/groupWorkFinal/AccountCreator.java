package groupWorkFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class AccountCreator
{
    // DATA MEMBERS
    private Database dBConnect;
    private boolean instructorValidated;

    @FXML
    private Label lbName;
    @FXML
    private Label lbUsername;
    @FXML
    private Label lbAddress;
    @FXML
    private Label lbPostalCode;
    @FXML
    private Label lbPassword;
    @FXML
    private Label lbConfirmPassword;
    @FXML
    private Label lbErrorMsg;

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtStreetAddress;
    @FXML
    private TextField txtCity;
    @FXML
    private TextField txtProvince;
    @FXML
    private TextField txtPostalCode;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtConfirmPassword;

    @FXML
    private RadioButton rbStudent;

    /*
    |   +onClickBtnSubmit()
    |
    |   Creates an account for the user by passing all the appropriate information to the Database method createAccount()
    |   After account is created the appropriate screen is loaded
    |   StudentController if student
    |   InstructorController if instructor
    */
    public void onClickBtnSubmit(ActionEvent e) throws IOException
    {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String name = txtName.getText();
        String streetAddress = txtStreetAddress.getText();
        String city = txtCity.getText();
        String province = txtProvince.getText();
        String postalCode = txtPostalCode.getText();
        boolean isStudent = rbStudent.isSelected();                 //is false if instructor is selected
        instructorValidated = false;

        // Create a connection as the values entered (NOT VERIFIED)
        dBConnect = new Database(username, password, isStudent);

        /*
         *   Verify txtName is VALID
         */

        // Check to make sure name is not blank
        if (name.equals(""))
        {
            lbName.setTextFill(Color.RED);
            lbErrorMsg.setText("Name Invalid");
            lbErrorMsg.setVisible(true);
            return;
        }
        /*
        *   Verify txtUsername is VALID
        */

        // Check to make sure username is not blank or user exists
        if (username.equals(""))
        {
            lbUsername.setTextFill(Color.RED);
            lbErrorMsg.setText("Username Invalid");
            lbErrorMsg.setVisible(true);
            return;
        }

        else if (dBConnect.checkLogin())
        {
            lbUsername.setTextFill(Color.RED);
            lbErrorMsg.setText("Username Already Exists");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        /*
        *   Verify txtPostalCode is VALID format
        */

        // Check the Postal Code Format (thanks Assignment 2)
        if (!validatePostalCode(postalCode))
        {
            lbAddress.setTextFill(Color.RED);
            lbPostalCode.setTextFill(Color.RED);
            lbErrorMsg.setText("Incorrect Postal Code");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        /*
        *   Verify txtPassword is VALID
        */

        // Ensure passwords match (And not null)
        if (!password.equals(confirmPassword))
        {
            lbPassword.setTextFill(Color.RED);
            lbConfirmPassword.setTextFill(Color.RED);
            lbErrorMsg.setText("Passwords do not Match");
            lbErrorMsg.setVisible(true);
            return;
        }

        else if (password.equals(""))
        {
            lbPassword.setTextFill(Color.RED);
            lbConfirmPassword.setTextFill(Color.BLACK);
            lbErrorMsg.setText("Password Field is blank");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }


        /*
        *    If Instructor authenticate with another Instructor account
        */
        if (!isStudent)
        {
            // Load instructor Authentication Controller
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InstructorAuthReq.fxml"));
            Parent parent = fxmlLoader.load();
            InstructorAuthController dialogController = fxmlLoader.getController();

            // set the boolean in the controller
            dialogController.setAuthInstructor(instructorValidated);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

            instructorValidated = dialogController.isAuthInstructor();

            System.out.println("authenticated:" + instructorValidated);

            if (!instructorValidated)
            {
                lbErrorMsg.setText("Account could not be verified");
                lbErrorMsg.setVisible(true);
                return;
            }

            else
            {
                lbReset();
            }

        }

        /*
        *   All data fields are VALID, create account
        */

        // Successful creation Go to correct Scene
        if (dBConnect.createUser(name, username, streetAddress, city, province, postalCode, password, isStudent))
        {
            if (isStudent)
            {
                //TESTING
                System.out.println("STUDENT CREATED: " + name);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("StudentView.fxml"));

                // Load the Controller (Initialize)
                Parent studentView = loader.load();

                // Set database from login
                StudentController controller = loader.getController();
                controller.setDbConnection(dBConnect);
                controller.initStudent();

                Scene studentViewScene = new Scene(studentView);

                /* END CHANGES to push connection */

                Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
                window.setTitle("Student View");
                window.setScene(studentViewScene);
                window.show();
            }

            else
            {
                //TESTING
                System.out.println("INSTRUCTOR CREATED: " + name);

                // is Instructor
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("InstructorView.fxml"));

                // Load the Controller (Initialize)
                Parent instructorView = loader.load();

                // Set database from login
                InstructorController controller = loader.getController();
                controller.setDbConnection(dBConnect);
                controller.initInstructor();

                Scene instructorViewScene = new Scene(instructorView);

                /* END CHANGES to push connection */

                Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
                window.setTitle("Instructor View");
                window.setScene(instructorViewScene);
                window.show();
            }
        }
    }// END OF ON CLICK BTN SUBMIT


    /*
    |   +onClickBtnCancel()
    |
    |   Returns to the login screen
    */
    public void onClickBtnCancel(ActionEvent e) throws IOException
    {
        Parent loginScreen = FXMLLoader.load(getClass().getResource("LoginNew.fxml"));
        Scene loginScreenScene = new Scene(loginScreen);

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setTitle("Login");
        window.setScene(loginScreenScene);
        window.show();
    }//END OF ON CLICK BTN CANCEL

    /*
    |   -validatePostalCode()
    |
    |   Validates the postal code supplied (Char/Dig/Char Dig/Char/Dig)
    */
    private Boolean validatePostalCode(String userPostalCode)
    {
        return userPostalCode.matches("[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d");
    }//END OF VALIDATE POSTAL CODE


    /*
    |   -lbReset()
    |
    |   Reset labels after correct entry
    */
    private void lbReset()
    {
        lbName.setTextFill(Color.BLACK);
        lbUsername.setTextFill(Color.BLACK);
        lbAddress.setTextFill(Color.BLACK);
        lbPostalCode.setTextFill(Color.BLACK);
        lbPassword.setTextFill(Color.BLACK);
        lbConfirmPassword.setTextFill(Color.BLACK);
        lbErrorMsg.setVisible(false);
    }//END OF LB RESET

}//END OF ACCOUNT CREATOR
