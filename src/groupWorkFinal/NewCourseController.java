package groupWorkFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class NewCourseController
{
    // DATA MEMBERS
    private Database dBConnection;
    private String courseCode;

    @FXML
    private TextField txtCourseCode;
    @FXML
    private TextField txtCourseName;
    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lbCourseCode;
    @FXML
    private Label lbCourseName;
    @FXML
    private Label lbDescription;
    @FXML
    private Label lbErrorMsg;

    @FXML
    private RadioButton rbFullYear;
    @FXML
    private RadioButton rbHalfYear;

    
    /**
     *      GETTERS AND SETTERS
     */


    /*
    |   +setdBConnection()
    |
    |   Set the database connection for the current user
    */
    public void setdBConnection(Database dBConnection) {
        this.dBConnection = dBConnection;
    }//END OF SET DB CONNECTION


    /*
    |   +getCourseCode()
    |
    |   Get the course code
    */
    public String getCourseCode() {
        return courseCode;
    }//END OF GET COURSE CODE


    /**
     *
     *      PUBLIC BUTTON ACTION EVENT METHODS
     *
     */


    /*
    |   +onClickBtnSubmit()
    |
    |   Checks all conditions for course creation
    |   Submits the new course to the database
    |   Closes the NewCourseController and returns to the InstructorController
    |   Calls:
    |           validateCourseCode()
    |           lbReset()
    */
    public void onClickBtnSubmit(ActionEvent e)
    {
        courseCode = txtCourseCode.getText().toUpperCase();
        String courseName = txtCourseName.getText();
        String courseDesc = txtDescription.getText();
        double numCredits;

        // Get list of current courses
        ArrayList<String> courseList;

        if ((courseList = dBConnection.getCourses()) != null) ;

        // Set the number of credits
        if (rbFullYear.isSelected())
            numCredits = 1.0;

        else
            numCredits = 0.5;

        // Check for course code
        if (courseCode.equals("") || !validateCourseCode(courseCode))
        {
            lbCourseCode.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Course Code Entered/ Invalid Format");
            lbErrorMsg.setVisible(true);
            return;
        }

        else if (courseList.contains(courseCode))
        {
            lbCourseCode.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("Course Code already Exists");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check for course name
        if (courseName.equals(""))
        {
            lbCourseName.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Course Title Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check for course description (OPTIONAL?)
        if (courseDesc.equals(""))
        {
            lbDescription.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Course Description Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        dBConnection.createCourse(courseCode, courseName, courseDesc, numCredits);

        //CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN SUBMIT


    /*
    |   +onClickBtnCancel()
    |
    |   Closes the NewCourseController and returns to the InstructorController
    */
    public void onClickBtnCancel(ActionEvent e)
    {
        // Reset value of string
        courseCode = null;

        //CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN CANCEL


    /**
     *
     *      PRIVATE METHODS
     *
     */


    /*
    |   -validateCourseCode()
    |
    |   Checks course code entry [A-Z]x4[0-9]x4 or [A-Z]x4-[0-9]x4
    */
    private Boolean validateCourseCode(String courseCode)
    {
        boolean valid = courseCode.matches("[A-Za-z]{4}\\d{4}|[A-Za-z]{4}-\\d{4}");

        if (courseCode.length() == 8 && valid)
        {
            this.courseCode = courseCode.substring(0, 4) + "-" + courseCode.substring(4);
        }

        return valid;
    }//END OF VALIDATE COURSE CODE


    /*
    |   -lbReset()
    |
    |   Resets all the labels back to normal
    */
    private void lbReset()
    {
        lbCourseCode.setTextFill(Color.BLACK);
        lbCourseName.setTextFill(Color.BLACK);
        lbDescription.setTextFill(Color.BLACK);
        lbErrorMsg.setVisible(false);
    }//END OF LB RESET

}//END OF NEW COURSE CONTROLLER