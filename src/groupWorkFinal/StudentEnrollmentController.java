package groupWorkFinal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Map;

public class StudentEnrollmentController
{
    // DATA MEMBERS
    private Database dbConnection;
    private String courseCode;
    private String semester;
    private int semesterId;

    private ArrayList<Integer> listOfSemesterIds = new ArrayList<>();

    private ObservableList<String> listOfCourses;
    private ObservableList<String> listOfSectionsSemesters;

    @FXML
    private ComboBox<String> cboCourses;
    @FXML
    private ComboBox<String> cboSemesters;

    @FXML
    private Label lbCourseError;
    @FXML
    private Label lbSemesterError;


    /**
     *      GETTER AND SETTERS
     */


    /*
    |   +setDbConnection()
    |
    |   Sets the database connection for the current user logged in
    */
    public void setDbConnection(Database dbConnection) {
        this.dbConnection = dbConnection;
    }//END OF SET DB CONNECTION


    /*
    |   +initEnrollement()
    |
    |   Calls resetCourseList
    */
    public void initEnrollement()
    {
        resetCourseList();
    }


    /**
     *      PUBLIC CBO SELECTION ACTION EVENT METHODS
     */

    /*
    |   +onSelectedCourse()
    |
    |   Determines which course was selected
    |   Calls:
    |           resetErrorMsg()
    |           resetSemesterList()
    */
    public void onSelectCourse(ActionEvent e)
    {
        courseCode = cboCourses.getValue();

        // Return if change to null
        if (courseCode == null)
            return;

        System.out.println("Course Selected: " + courseCode);

        resetErrorMsg();

        resetSemesterList();
    }//END OF ON SELECT COURSE


    /*
    |   +onSelectSemester
    |
    |   Determines which course was selected
    |   Calls resetErrorMsg()
    */
    public void onSelectSemester(ActionEvent e)
    {
        semesterId = listOfSemesterIds.get(cboSemesters.getSelectionModel().getSelectedIndex());

        // Return if change to null
        if (semesterId < 0)
            return;

        System.out.println("Semester Selected: " + semester);

        System.out.println("SemesterID Selected: " + semesterId);

        resetErrorMsg();
    }//END OF ON SELECT SEMESTER


    /**
     *      PUBLIC BUTTON ACTION EVENT METHODS
     */


    /*
    |   +onClickBtnAuthEnroll
    |
    |   Determines which course was selected and enrolls the student in that course
    |   Closes the StudentEnrollmentController and returns the user to the StudentController
    */
    public void onClickBtnAuthEnroll(ActionEvent e)
    {
        if (courseCode == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Course Selected");
            alert.setHeaderText("COURSE SELECTION REQUIRED");
            alert.setContentText("In order to enroll, you require a course selected");

            alert.showAndWait();

            lbCourseError.setTextFill(Color.RED);
            lbCourseError.setText("Error: No Course Selected");
            lbCourseError.setVisible(true);

            return;
        }

        else if (semesterId < 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Semester Selected");
            alert.setHeaderText("SEMESTER SELECTION REQUIRED");
            alert.setContentText("In order to enroll, you require a semester selected");

            alert.showAndWait();


            lbSemesterError.setTextFill(Color.RED);
            lbSemesterError.setText("Error: No Semester Selected");
            lbSemesterError.setVisible(true);

            return;
        }

        else if (checkEnrollment())
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Already Enrolled");
            alert.setHeaderText("ALREADY ENROLLED IN COURSE");
            alert.setContentText("You have already been enrolled in this course/semester.");

            alert.showAndWait();

            return;
        }

        dbConnection.addToSection(semesterId);
        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();

    }//END OF ON CLICK BTN AUTH ENROLL


    /*
    |   +onClickBtnCancel()
    |
    |   Closes the StudentEnrollmentController and returns the user to the StudentController
    */
    public void onClickBtnCancel(ActionEvent e)
    {
        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK BTN CANCEL


    /**
     *      PRIVATE METHODS
     */


    /*
    |   -resetCourseList()
    |
    |   Resets the course list
    */
    private void resetCourseList()
    {
        cboCourses.getItems().clear();

        ArrayList<String> courseList;

        if ((courseList = dbConnection.getCourses()) != null)
        {
            listOfCourses = FXCollections.observableArrayList(courseList);
            cboCourses.setItems(listOfCourses);
        }

        System.out.println("RESET COURSE: " + courseCode);
        if (courseCode != null)
            cboCourses.setValue(courseCode);
    }//END OF RESET COURSE LIST


    /*
    |   -resetSemesterList()
    |
    |   Resets the semester list
    */
    private void resetSemesterList()
    {
        cboSemesters.getItems().clear();
        listOfSemesterIds.clear();
        semesterId = -1;

        // Add All Semesters Available for the Course Selected
        ArrayList<Map<String, String>> listOfSections = dbConnection.getSections(courseCode);
        ArrayList<String> semesterList = new ArrayList<>();

        System.out.println("LIST OF SEMESTERS");

        // Add each entry to the semesterList Array
        for (Map<String, String> sections : listOfSections)
        {
            // TESTING PRINT TO SCREEN (Also including sectionID for testing)
            System.out.println(sections.get("semester"));
            semesterList.add(sections.get("semester"));
            listOfSemesterIds.add(Integer.parseInt(sections.get("sectionId")));
        }

        listOfSectionsSemesters = FXCollections.observableArrayList(semesterList);
        cboSemesters.setItems(listOfSectionsSemesters);

        if (semester != null)
            cboSemesters.setValue(semester);
    }//END OF RESET SEMESTER LIST


    /*
    |   -resetErrorMsg()
    |
    |   Resets the error message labels
    */
    private void resetErrorMsg()
    {
        lbCourseError.setVisible(false);
        lbSemesterError.setVisible(false);
    }//END OF RESET ERROR MSG


    /*
    |   -checkEnrollment()
    |
    |   Checks the enrollment status for the student enrolling in the course
    */
    private boolean checkEnrollment()
    {
        boolean enrolled = false;

        ArrayList<Map<String, String>> listOfStudentIds = dbConnection.getStudentsInSection(semesterId);
        ArrayList<Integer> studentIdList = new ArrayList<>();

        System.out.println("LIST OF StudentIDs");

        // Add each entry to the semesterList Array
        for (Map<String, String> sections : listOfStudentIds)
        {
            // TESTING PRINT TO SCREEN (Also including sectionID for testing)
            System.out.println(sections.get("studentId"));
            studentIdList.add(Integer.parseInt(sections.get("studentId")));
        }

        if(studentIdList.contains(dbConnection.getUserId()))
        {
            enrolled = true;
        }

        return enrolled;
    }// END OF CHECK ENROLLMENT

}//END OF STUDENT ENROLLMENT CONTROLLER
