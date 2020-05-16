package groupWorkFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class NewAssignmentController
{
    // DATA MEMBERS
    private Database dBConnection;
    private int sectionSemesterId;
    private String assignment;

    @FXML
    private Label lbErrorMsg;
    @FXML
    private Label lbSemester;
    @FXML
    private Label lbDescription;
    @FXML
    private Label lbAssignmentName;
    @FXML
    private Label lbDDate;

    @FXML
    private TextField txtDescription;
    @FXML
    private TextField txtAssignmentName;
    @FXML
    private TextField txtDDate;


    /**
     *      GETTER AND SETTERS
     */


    /*
    |     +setdBConnections
    |
    |       Sets the database connectiomn for the current user
     */
    public void setdBConnection(Database dBConnection)
    {
        this.dBConnection = dBConnection;
    }//END OF SET DB CONNECTION


    /*
    |   +setSectionSemesterId()
    |
    |   Sets the section ID
     */
    public void setSectionSemesterId(int sectionSemesterId) {
        this.sectionSemesterId = sectionSemesterId;
    }//END OF SET SECTION SEMESTER ID


    /*
    |   +getAssignment()
    |
    |   Gets the assignment
    */
    public String getAssignment() {
        return assignment;
    }


    /*
    |   +setLbSemester()
    |
    |   Sets the semester label to the selected semester
    */
    public void setLbSemester(String semester) {
        this.lbSemester.setText(semester);
    }


    /**
     *      PUBLIC BUTTON ACTION EVENT METHODS
     */

    /*
    |   +onClickBtnSubmit()
    |
    |   Creates a new assignment and closes the NewAssignmentController
    |   Returns user to the InstructorController
    */
    public void onClickBtnSubmit(ActionEvent e)
    {
        assignment = txtAssignmentName.getText();
        String description = txtDescription.getText();
        String dueDate = txtDDate.getText();

        // Check for Assignment Name
        if (assignment.equals(""))
        {
            lbAssignmentName.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Assignment Name Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check for description
        if (description.equals(""))
        {
            lbDescription.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Description Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        dBConnection.createAssignment(assignment, description, dueDate, sectionSemesterId);

        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }// END OF ON CLICK BTN SUBMIT


    /*
    |   +onClickBtnCancel()
    |
    |   Closes the NewAssignmentController
    |   Returns the user to the InstructorController
    */
    public void onClickBtnCancel(ActionEvent e)
    {
        assignment = null;

        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN CANCEL


    /**
     * PRIVATE METHODS
     */


    /*
    |   -lbReset()
    |
    |   Resets all the labels back to normal
    */
    private void lbReset()
    {
        lbAssignmentName.setTextFill(Color.BLACK);
        lbDescription.setTextFill(Color.BLACK);
        lbDDate.setTextFill(Color.BLACK);
        lbErrorMsg.setVisible(false);
    }//END OF LB RESET

}//END OF NEW ASSIGNMENT CONTROLLER