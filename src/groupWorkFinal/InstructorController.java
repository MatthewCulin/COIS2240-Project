package groupWorkFinal;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class InstructorController
{
    // DATA MEMBERS

    private Database dbConnection;
    private String courseCode;
    private String semester;
    private String assignment;
    private int semesterId;
    private int assignmentId;
    private int groupNumber;
    private int groupMemberId;

    private ArrayList<Integer> listOfSemesterIds = new ArrayList<>();
    private ArrayList<Integer> listOfAssignmentIds = new ArrayList<>();
    private ArrayList<Integer> listOfGroupNumbers = new ArrayList<>();
    private ArrayList<Integer> listOfGroupMemberIds = new ArrayList<>();

    private ObservableList<String> listOfCourses;
    private ObservableList<String> listOfSectionsSemesters;
    private ObservableList<String> listOfSectionsAssignments;

    @FXML
    private ComboBox<String> cboCourses;
    @FXML
    private ComboBox<String> cboSemesters;
    @FXML
    private ComboBox<String> cboAssignmentList;
    @FXML
    private ListView<String> lvGroupList;
    @FXML
    private ListView<String> lvGroupMemberList;
    @FXML
    private Label lbCourseError;
    @FXML
    private Label lbSemesterError;
    @FXML
    private Label lbAssignmentError;


    /*
    |   +setDBConnection()
    |
    |   Sets the database connection for the current user logged in
    */
    public void setDbConnection(Database dbConnection)
    {
        this.dbConnection = dbConnection;
    }//END OF SET DB CONNECTION


    /*
    |   -getSemesterId()
    |
    |   Get the semester ID for the selected semester from the database
    */
    private void getSemesterId()
    {
        if (semester == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Semester Selected");
            alert.setHeaderText("Please Select Course before Deleting Semester");
            alert.setContentText("In order to delete a semester/session, you require one selected");

            alert.showAndWait();    //array list of semester id

            return;
        }

        semesterId = listOfSemesterIds.get(cboSemesters.getSelectionModel().getSelectedIndex());
        System.out.println("Found section is: " + semesterId);

    }//END OF GET SEMESTER ID


    /*
    |   -getAssignmentId()
    |
    |   Gets the assignment ID for the selected assignment from the database
    */
    private void getAssignmentId()
    {
        if (assignment == null)
        {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Assignment Selected");
            alert.setHeaderText("Please Select Course and Semester before Deleting Assignment");
            alert.setContentText("In order to delete a assignment, you require one selected");

            alert.showAndWait();//array list of assignment id

            return;
        }

        assignmentId = listOfAssignmentIds.get(cboAssignmentList.getSelectionModel().getSelectedIndex());
        System.out.println("Found assignment is: " + assignmentId);

    }//END OF GET ASSIGNMENT ID


    /*
    |   +initInstructor()
    |
    |   Initializes the UI
    |   Calls resetCourseList()
    */
    public void initInstructor()
    {
        //dbConnection = new Database("root", "verySecret", false);

        System.out.println(dbConnection.checkLogin());

        // TESTING GOOD RUN FOR SINGLE ENTRY AND TEST
        // COMMENT OUT AFTER FIRST RUN!
        // dbConnection.populateTestData();

        // Listen for change of selection in GroupNumber Listing
        lvGroupList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                viewGroup();
            }
        });

        resetCourseList();
    }//END OF INIT INSTRUCTOR


    /*
    |   +onSelectCourse()
    |
    |   Determines which course was selected
    |   Calls:
    |           resetErrorMsg()
    |           resetSemesterList()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
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
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();
    }//END OF ON SELECT COURSE


    /*
    |   +onSelectSemester()
    |
    |   Determines which semester was selected
    |   Calls:
    |           resetErrorMsg()
    |           resetSemesterId()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onSelectSemester(ActionEvent e)
    {
        semester = cboSemesters.getValue();

        // Return if change to null
        if (semester == null)
            return;

        System.out.println("Semester Selected: " + semester);

        resetErrorMsg();
        getSemesterId();
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();
    }//END OF ON SELECT SEMESTER


    /*
    |   +onSelectAssignment()
    |
    |   Determines which assignment was selected
    |   Calls:
    |           resetErrorMsg()
    |           resetAssignmentId()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onSelectAssignment(ActionEvent e)
    {
        assignment = cboAssignmentList.getValue();

        // Return if change to null
        if (assignment == null)
            return;

        System.out.println("Assignment Selected: " + assignment);

        resetErrorMsg();
        getAssignmentId();
        resetGroupList();
        resetGroupMemberList();

    }//END OF ON SELECT ASSIGNMENT


    /*
    |   +onClickRemoveFromGroup()
    |
    |   Determines which group member was selected and removes them from the group
    |   Calls:
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnRemoveFromGroup(ActionEvent e)
    {
        int memberSelectedIndex = lvGroupMemberList.getSelectionModel().getSelectedIndex();

        if (memberSelectedIndex < 0)
        {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Group Member Selected");
            alert.setHeaderText("GROUP MEMBER SELECTION REQUIRED");
            alert.setContentText("In order to delete a group member, you require one selected");

            alert.showAndWait();

            return;
        }

        groupMemberId = listOfGroupMemberIds.get(memberSelectedIndex);

        System.out.println("GMID DELETE: " + groupMemberId);

        if (!dbConnection.removeFromGroup(groupMemberId, assignmentId, groupNumber))
            System.out.println("FAILED DELETE");

        resetGroupList();
        resetGroupMemberList();
    }//END OF ON CLICK BTN REMOVE FROM GROUP


    /*
    |   +onClickBtnNewCourse()
    |
    |   Loads the NewCourseController
    |   Calls:
    |           resetCourseList()
    |           resetSemesterList()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnNewCourse(ActionEvent e) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewCourse.fxml"));
        Parent parent = fxmlLoader.load();
        NewCourseController dialogController = fxmlLoader.getController();

        dialogController.setdBConnection(dbConnection);

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("New Course");
        stage.setResizable(false);
        stage.showAndWait();

        courseCode = dialogController.getCourseCode();

        System.out.println("NEW COURSE NAME:" + courseCode);

        resetCourseList();
        resetSemesterList();
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();
    }//END OF ON CLICK BTN NEW COURSE


    /*
    |   +onClickBtnNewSemester()
    |
    |   Loads the NewSemesterController
    |   Calls:
    |           resetSemesterList()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnNewSemester(ActionEvent e) throws IOException
    {
        if (courseCode == null)
        {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Course Selected");
            alert.setHeaderText("COURSE SELECTION REQUIRED");
            alert.setContentText("In order to create a new semester/session, you require a course selected");

            alert.showAndWait();

            lbCourseError.setTextFill(Color.RED);
            lbCourseError.setText("Error: No Course Selected");
            lbCourseError.setVisible(true);

            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewSemester.fxml"));
        Parent parent = fxmlLoader.load();
        NewSemesterController dialogController = fxmlLoader.getController();

        dialogController.setdBConnection(dbConnection);
        dialogController.setCourseCode(courseCode);
        dialogController.setLbCourseCode(courseCode);

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("New Semester/Session");
        stage.setResizable(false);
        stage.showAndWait();

        semester = dialogController.getSemester();

        resetSemesterList();
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();

    }//END OF ON CLICK BTN NEW SEMESTER


    /*
    |   +onClickBtnNewAssignment()
    |
    |   Loads the NewAssignmentController
    |   Calls:
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnNewAssignment(ActionEvent e) throws IOException
    {
        if (courseCode == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Course Selected");
            alert.setHeaderText("COURSE SELECTION REQUIRED");
            alert.setContentText("In order to create a new semester/session, you require a course selected");

            alert.showAndWait();

            lbCourseError.setTextFill(Color.RED);
            lbCourseError.setText("Error: No Course Selected");
            lbCourseError.setVisible(true);

            return;
        }

        else if (semester == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Semester Selected");
            alert.setHeaderText("SEMESTER SELECTION REQUIRED");
            alert.setContentText("In order to create a new assignment, you require a semester selected");

            alert.showAndWait();

            lbSemesterError.setTextFill(Color.RED);
            lbSemesterError.setText("Error: No Semester Selected");
            lbSemesterError.setVisible(true);

            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewAssignment.fxml"));
        Parent parent = fxmlLoader.load();
        NewAssignmentController dialogController = fxmlLoader.getController();

        dialogController.setdBConnection(dbConnection);
        dialogController.setSectionSemesterId(semesterId);
        dialogController.setLbSemester(semester);

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("New Assignment");
        stage.setResizable(false);
        stage.showAndWait();

        assignment = dialogController.getAssignment();

        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();

    }//END OF CLICK BTN NEW ASSIGNMENT


    /*
    |   +onClickBtnDeleteCourse()
    |
    |   Determines which course was selected and deletes it
    |   Calls:
    |           resetCourseList()
    |           resetSemesterList()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnDeleteCourse(ActionEvent e)
    {
        if (courseCode == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Course Selected");
            alert.setHeaderText("COURSE SELECTION REQUIRED");
            alert.setContentText("In order to delete a course, you require one selected");

            alert.showAndWait();

            lbCourseError.setTextFill(Color.RED);
            lbCourseError.setText("Error: No Course Selected");
            lbCourseError.setVisible(true);

            return;
        }

        if (!dbConnection.deleteCourse(courseCode))
            System.out.println("FAILED DELETE");

        resetCourseList();
        resetSemesterList();
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();
    }//END OF ON CLICK BTN DELETE COURSE


    /*
    |   +onClickBtnDeleteSemester()
    |
    |   Determines which semester was selected and deletes it
    |   Calls:
    |           resetSemesterList()
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnDeleteSemester(ActionEvent e)
    {
        if (semester == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Semester Selected");
            alert.setHeaderText("SEMESTER SELECTION REQUIRED");
            alert.setContentText("In order to delete a semester, you require one selected");

            alert.showAndWait();//array list of semester id

            lbSemesterError.setTextFill(Color.RED);
            lbSemesterError.setText("Error: No Semester Selected");
            lbSemesterError.setVisible(true);

            return;
        }

        getSemesterId();

        if (!dbConnection.deleteSection(semesterId))
        {
            System.out.println("FAILED DELETE");
        }

        resetSemesterList();
        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();

    }//END OF ON CLICK BTN DELETE SEMESTER


    /*
    |   +onClickBtnDeleteAssignment()
    |
    |   Determines which assignment was selected and deletes it
    |   Calls:
    |           resetAssignmentList()
    |           resetGroupList()
    |           resetGroupMemberList()
    */
    public void onClickBtnDeleteAssignment(ActionEvent e)
    {
        if (assignment == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Assignment Selected");
            alert.setHeaderText("ASSIGNMENT SELECTION REQUIRED");
            alert.setContentText("In order to delete a group, you require one selected");

            alert.showAndWait();

            lbAssignmentError.setTextFill(Color.RED);
            lbAssignmentError.setText("Error: No Assignment Selected");
            lbAssignmentError.setVisible(true);

            return;
        }

        getAssignmentId();

        if (!dbConnection.deleteAssignment(assignmentId))
            System.out.println("FAILED DELETE");

        resetAssignmentList();
        resetGroupList();
        resetGroupMemberList();
    }//END OF ON CLICK BTN DELETE ASSIGNMENT


    /*
    |   +onClickBtnsignOut()
    |
    |   Signs out the current user
    |   Returns to LoginController
    */
    public void onClickBtnsignOut(ActionEvent e) throws IOException
    {
        FXMLLoader loader = new FXMLLoader((getClass().getResource("LoginNew.fxml")));
        Parent loginScreen = loader.load();

        Scene loginScreenScene = new Scene(loginScreen);

        // Close database connection
        dbConnection.destroy();

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setTitle("Login");
        window.setScene(loginScreenScene);
        window.show();

    }//END OF ON CLICK BTN SIGN OUT


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

        // Add all semesters available for the course selected
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
    |   -resetAssignmentList()
    |
    |   Resets the assignment list
    */
    private void resetAssignmentList()
    {
        cboAssignmentList.getItems().clear();
        listOfAssignmentIds.clear();
        assignmentId = -1;

        // Add all semesters available for the course selected
        ArrayList<Map<String, String>> listOfAssignments = dbConnection.getAssignmentsBySection(semesterId);
        ArrayList<String> assignmentList = new ArrayList<>();

        System.out.println("LIST OF AVAILABLE ASSIGNMENTS");

        // Add each entry to the assignmentList Array
        for (Map<String, String> assignments : listOfAssignments)
        {
            // TESTING PRINT TO SCREEN (Also including sectionID for testing)
            System.out.println(assignments.get("assignmentId") + ", " + assignments.get("name"));
            assignmentList.add(assignments.get("name"));
            listOfAssignmentIds.add(Integer.parseInt(assignments.get("assignmentId")));
        }

        listOfSectionsAssignments = FXCollections.observableArrayList(assignmentList);
        cboAssignmentList.setItems(listOfSectionsAssignments);

        if (assignment != null)
            cboAssignmentList.setValue(assignment);

    }//END OF RESET ASSIGNMENT LIST


    /*
    |   -resetGroupList()
    |
    |   Resets the group list
    */
    private void resetGroupList()
    {
        lvGroupList.setItems(null);
        listOfGroupNumbers.clear();
        groupNumber = -1;

        ArrayList<Integer> listOfGroups = dbConnection.getGroupsByAssignment(assignmentId);
        ArrayList<String> displayGroups = new ArrayList<>();

        for (int i = 0; i < listOfGroups.size(); i++)
        {
            if (!displayGroups.contains(listOfGroups.get(i).toString()))
            {
                displayGroups.add(listOfGroups.get(i).toString());
                listOfGroupNumbers.add(listOfGroups.get(i));
            }
        }

        lvGroupList.setItems(FXCollections.observableArrayList(displayGroups));
    }//END OF RESET GROUP LIST


    /*
    |   -resetGroupMemberListList()
    |
    |   Resets the group member list
    */
    private void resetGroupMemberList()
    {
        lvGroupMemberList.setItems(null);

        if (groupNumber == -1)
            return;

        System.out.println("AID: " + assignmentId + " GID: " + groupNumber);

        ArrayList<Map<String, String>> listOfGroupMembers = dbConnection.getGroupPartners(assignmentId, groupNumber);
        ArrayList<String> groupMemberList = new ArrayList<>();

        if (listOfGroupMembers == null)
        {
            //lblErrorLabel.setText("NO GROUP MEMBERS");
            return;
        }

        for (Map<String, String> groupMembers : listOfGroupMembers)
        {
            groupMemberList.add(groupMembers.get("name"));
            listOfGroupMemberIds.add(Integer.parseInt(groupMembers.get("studentId")));
            System.out.println("GMID:" + groupMembers.get("studentId"));
        }

        lvGroupMemberList.setItems(FXCollections.observableArrayList(groupMemberList));
    }//END OF RESET GROUP MEMBER LIST


    /*
    |   -viewGroup()
    |
    |   Resets the course list
    |   Calls resetGroupMemberList()
    */
    private void viewGroup()
    {
        int groupSelected = lvGroupList.getSelectionModel().getSelectedIndex();

        /*
        *    ONLY DURING RESET WILL VALUE BE < 0 (Just return)
        */
        if (groupSelected < 0)
        {
            System.out.println("ERR:Group Selected: " + groupSelected);
            return;
        }

        groupNumber = listOfGroupNumbers.get(groupSelected);

        resetGroupMemberList();
    }//END OF VIEW GROUP


    /*
    |   -resetErrorMsg()
    |
    |   Resets the error message label
    */
    private void resetErrorMsg()
    {
        lbCourseError.setVisible(false);
        lbSemesterError.setVisible(false);
        lbAssignmentError.setVisible(false);
    }//END OF RESET ERROR MSG

}//END OF INSTRUCTOR CONTROLLER
