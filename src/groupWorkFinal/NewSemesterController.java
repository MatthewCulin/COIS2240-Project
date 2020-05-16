package groupWorkFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;

public class NewSemesterController
{
    // DATA MEMBERS
    private Database dBConnection;
    private String courseCode;
    private String semester;

    @FXML
    private TextField txtSemesterName;
    @FXML
    private TextField txtRoom;
    @FXML
    private TextField txtStartTime;
    @FXML
    private TextField txtEndTime;

    @FXML
    private CheckBox chkMonday;
    @FXML
    private CheckBox chkTuesday;
    @FXML
    private CheckBox chkWednesday;
    @FXML
    private CheckBox chkThursday;
    @FXML
    private CheckBox chkFriday;

    @FXML
    private Label lbSesionDates;
    @FXML
    private Label lbCourseCode;
    @FXML
    private Label lbErrorMsg;
    @FXML
    private Label lbSemesterName;
    @FXML
    private Label lbRoom;
    @FXML
    private Label lbStartTime;
    @FXML
    private Label lbEndTime;
    @FXML
    private Label lbMeetingTimes;


    /**
     *
     *      GETTERS AND SETTERS
     *
     */


    /*
    |   + setdBConnection()
    |
    |   Sets the database connection for the current user
    */
    public void setdBConnection(Database dBConnection) {
        this.dBConnection = dBConnection;
    }//END OF SET DB CONNECTION


    /*
    |   +setCourseCode()
    |
    |   Set the course code of the selected course
    */
    public void setCourseCode(String courseName) {
        this.courseCode = courseName;
    }//END OF SET COURSE CODE


    /*
    |   +getSemester()
    |
    |   Get the semester list for the course selected
    */
    public String getSemester() {
        return semester;
    }//END OF GET SEMESTER


    /*
    |   +setLbCourseCode()
    |
    |   Set the course code label to display the current course selected
    */
    public void setLbCourseCode(String courseCode) {
        this.lbCourseCode.setText(courseCode);
    }//END OF SET LB COURSE CODE


    /**
     *
     *      PUBLIC BUTTON ACTION EVENT METHODS
     *
     */


    /*
    |   +onClickBtnSubmit()
    |
    |   Submits the semester for the selected course
    */
    public void onClickBtnSubmit(ActionEvent e)
    {
        semester = txtSemesterName.getText();
        String room = txtRoom.getText();
        String meetingTimes;

        ArrayList<Map<String, String>> listOfSections = dBConnection.getSections(courseCode);
        ArrayList<String> semesterList = new ArrayList<>();

        // Check the current list of semester names
        for (Map<String, String> sections : listOfSections)
        {
            semesterList.add(sections.get("semester"));
        }

        // Check semester name
        if (semester.equals(""))
        {
            lbSemesterName.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Semester Name Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else if (semesterList.contains(semester))
        {
            lbSemesterName.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("Semester with that name exists");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check for room
        if (room.equals(""))
        {
            lbRoom.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No Room Entered");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check that start time is valid format
        if (!validateTime(txtStartTime.getText()))
        {
            lbStartTime.setTextFill(Color.RED);
            lbMeetingTimes.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("Start Time incorrect Format");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        // Check that end time is valid format
        if (!validateTime(txtEndTime.getText()))
        {
            lbEndTime.setTextFill(Color.RED);
            lbMeetingTimes.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("End Time incorrect Format");
            lbErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lbReset();
        }

        meetingTimes = computeMeetingTimes();

        if(meetingTimes == "NO DAYS"){
            lbSesionDates.setTextFill(Color.RED);
            lbErrorMsg.setTextFill(Color.RED);
            lbErrorMsg.setText("No day selected");
            lbErrorMsg.setVisible(true);
            return;
        }

        dBConnection.createSection(semester, room, meetingTimes, courseCode);

        //CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN SUBMIT


    /*
    |   +onClickBtnCancel()
    |
    |   Closes the NewSemesterController
    */
    public void onClickBtnCancel(ActionEvent e)
    {
        semester = null;

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
    |   -computeMeetingTimes
    |
    |   Computes the meeting times
    */
    private String computeMeetingTimes()
    {
        String days = "";
        String startTime, endTime;
        String result;
        int numDays=0;


        if (chkMonday.isSelected()) {
            days = "M" + days;
            numDays++;
        }
        if (chkTuesday.isSelected()) {
            days = days + "Tu";
            numDays++;
        }
        if (chkWednesday.isSelected()) {
            days = days + "W";
            numDays++;
        }
        if (chkThursday.isSelected()) {
            days = days + "Th";
            numDays++;
        }
        if (chkFriday.isSelected()) {
            days = days + "F";
            numDays++;
        }

        if(numDays ==0){
            return "NO DAYS";
        }

        startTime = txtStartTime.getText();

        endTime = txtEndTime.getText();

        result = days + " " + startTime + "-" + endTime;

        System.out.println(result);

        return result;
    }//END OF COMPUTE MEETING TIMES


    /*
    |   -validateTimes()
    |
    |   Validate times ([00-23]:[00-59])
    */
    private Boolean validateTime(String time)
    {
        return time.matches("^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$");
    }//END OF VALIDATE TIMES


    /*
    |   -lbReset()
    |
    |   Resets all the labels back to normal
    */
    private void lbReset()
    {
        lbSesionDates.setTextFill(Color.BLACK);
        lbSemesterName.setTextFill(Color.BLACK);
        lbRoom.setTextFill(Color.BLACK);
        lbStartTime.setTextFill(Color.BLACK);
        lbEndTime.setTextFill(Color.BLACK);
        lbMeetingTimes.setTextFill(Color.BLACK);
        lbErrorMsg.setVisible(false);
    }//END OF LB RESET

}//END OF NEW SEMESTER CONTROLLER

