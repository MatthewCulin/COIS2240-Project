package groupWorkFinal;

import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.*;
import java.io.IOException;
import java.util.*;
import javafx.beans.value.*;


/*
|   Main form controller when section opens.
|
*/
public class StudentController
{
    // DATA MEMBERS
    private Database dbConnection;
    private int studentID;
    private int sectionID;
    private int assignmentID;

    private String sectionSelection;
    private String courseSelection;
    private String assignmentSelection;
    private int groupNumberSelection;
    private int groupMemberIDSelection;
    private String groupMemberSelection;

    private ArrayList<Integer> sectionIds;
    private ArrayList<Integer> assignmentIds;
    private ArrayList<Integer> groupMemberIDs;

    @FXML
    private ComboBox<String> cboSectionList;
    @FXML
    private ComboBox<String> cboCourseList;
    @FXML
    private ComboBox<String> cboAssignmentList;

    @FXML
    private ListView<Integer> lvGroupList;
    @FXML
    private ListView<String> lvGroupMemberList;
    @FXML
    private Label lblErrorLabel;

    /*
    |   +setDbConnection()
    |
    |   Set the database connection for the current user
    */
    public void setDbConnection(Database dbConnection)
    {
        this.dbConnection = dbConnection;
    }


    /*
    |   +initStudent()
    |
    |   Initializes the UI
    |   Sets the connections, ID, initilizes the arraylists
    |   Runs resetSemesterList()
    |   Contains the Listener for lvGroupList
    */
    public void initStudent()
    {
        //dbConnection = new Database("roy@royorbison.com", "mysterygirl", true);
        dbConnection.checkLogin();                                                                                      // Verifies the
        studentID = dbConnection.getUserId();
        this.sectionIds = new ArrayList<>();
        this.assignmentIds = new ArrayList<>();
        resetSemesterList();

        //set our click events for the group list box
        lvGroupList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
            {
                viewGroup();
            }
        });


    }//END OF INITIALIZE


    /*
    |   +onClickBtnEnroll()
    |
    |   Loads the StudentEnrollmentController
    |   Calls:
    |           resetSemesterList()
    */
    public void onClickBtnEnroll(ActionEvent e) throws IOException{
        // Load Enrollment Controller
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StudentEnrollment.fxml"));
        Parent parent = fxmlLoader.load();

        StudentEnrollmentController dialogController = fxmlLoader.getController();

        // set the boolean in the controller
        dialogController.setDbConnection(dbConnection);
        dialogController.initEnrollement();

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        resetSemesterList();
    }

    /*
    |   +onClickSemesterDropdown()
    |
    |   Displays the list of available courses for the student
    */
    public void onClickSemesterDropdown(ActionEvent e)
    {
        //this shouldn't be necessary, but hey...
        if(cboSectionList.getSelectionModel().isEmpty())
        {
            //no values
            return;
        }

        //clear out other combo boxes
        cboCourseList.getItems().clear();
        cboAssignmentList.getItems().clear();

        sectionSelection = cboSectionList.getValue();

        //get our section Id out of the arrayList based on the index.
        sectionID = this.sectionIds.get(cboSectionList.getSelectionModel().getSelectedIndex());

        ArrayList<Map<String, String>> listOfCourses = dbConnection.getStudentCourses(studentID, sectionSelection);
        ArrayList<String> courseList = new ArrayList<>();

        for (Map<String, String> courses: listOfCourses)
        {
            courseList.add(courses.get("courseCode"));
        }

        cboCourseList.setItems(FXCollections.observableArrayList(courseList));
        cboCourseList.setValue("COURSES");

    }//END OF ON CLICK DROPDOWN

    /*
    |   +onClickSemesterDropdown()
    |
    |   Displays the list of available courses for the student
    */
    public void onClickCourseDropdown(ActionEvent e)
    {
        courseSelection = cboCourseList.getValue();

        //guard against default value.
        if(courseSelection == "COURSES")
        {
            return;
        }

        //clear out assignmentIds
        this.assignmentIds.clear();



        ArrayList<Map<String, String>> listOfAssignments = dbConnection.getAssignmentsBySection(sectionID);
        ArrayList<String> assignmentList = new ArrayList<>();

        for (Map<String, String> assignments: listOfAssignments)
        {
            assignmentList.add(assignments.get("name"));
            this.assignmentIds.add(Integer.parseInt(assignments.get("assignmentId")));
        }

        cboAssignmentList.setItems(FXCollections.observableArrayList(assignmentList));
        cboAssignmentList.setValue("ASSIGNMENTS");

    }//END OF ON CLICK DROPDOWN

    /*
    |   +onClickSemesterDropdown()
    |
    |   Displays a list of the assignments for the class
    */
    public void onClickAssignmentDropdown(ActionEvent e)
    {
        assignmentSelection = cboAssignmentList.getValue();
        //guard against first click
        if(assignmentSelection == "ASSIGNMENTS")
        {
            return;
        }

        int selectedIndex = cboAssignmentList.getSelectionModel().getSelectedIndex();
        if(selectedIndex == -1)
        {
            lvGroupList.getItems().clear();
            lvGroupMemberList.getItems().clear();
            return;

        }
        assignmentID = this.assignmentIds.get(selectedIndex);

        ArrayList<Integer> listOfGroups = dbConnection.getGroupsByAssignment(assignmentID);
        ArrayList<Integer> displayGroups = new ArrayList<>();

        for(int i = 0; i < listOfGroups.size(); i++)
        {
            if(!displayGroups.contains(listOfGroups.get(i)))
                displayGroups.add(listOfGroups.get(i));
        }



        lvGroupList.setItems(FXCollections.observableArrayList(displayGroups));

    }//END OF ON CLICK DROPDOWN


    /*
     |  +onClickEnrollInGroup()
     |
     |  Enrolls the student in a group
     |  Calls:
     |      viewGroup()
     */
    public void onClickEnrollInGroup(ActionEvent e)
    {
        //guard against no selection
        if(lvGroupList.getSelectionModel().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Group Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a group");
            alert.showAndWait();
            return;
        }
        groupNumberSelection = lvGroupList.getSelectionModel().getSelectedItem();

        // Set user group to selected group for selected assignment and course
        System.out.println("SID: " + studentID + " AID: " + assignmentID + " GN: " + groupNumberSelection);

        boolean inGroupAlready = dbConnection.checkForGroup(studentID, this.assignmentID);

        if(!inGroupAlready)
        {
            boolean isInGroup = dbConnection.assignToGroup(studentID, assignmentID, groupNumberSelection);

            if(isInGroup)
                lblErrorLabel.setText("ENROLLED IN GROUP");
            else
                lblErrorLabel.setText("ERROR ENROLLING IN GROUP");
        }

        else
            lblErrorLabel.setText("ERROR: ALREADY IN GROUP");

        // REFRESH GROUPS
        viewGroup();


    }//END OF ON CLICK ENROLL IN GROUP

    /*
     |  +onClickViewBreakdown()
     |
     |  Opens a new window to display the breakdown of a students review
     */
    public void onClickViewBreakdown(ActionEvent e)
    {
        //guard against no selection.
        if(lvGroupMemberList.getSelectionModel().isEmpty() ||
                lvGroupMemberList.getSelectionModel().getSelectedIndex() == -1)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Group Member Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a group member.");
            alert.showAndWait();
            return;
        }
        groupMemberSelection = lvGroupMemberList.getSelectionModel().getSelectedItem();
        groupMemberIDSelection = groupMemberIDs.get(lvGroupMemberList.getSelectionModel().getSelectedIndex());
        System.out.println("GMID: " + groupMemberIDSelection);

        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentBreakdown.fxml"));
            Parent studentBreakdown = loader.load();
            BreakdownController controller = loader.getController();

            controller.setDbConnection(dbConnection);
            controller.setText(groupMemberIDSelection);
            controller.initBreakdown();

            Stage breakdown = new Stage();
            breakdown.initModality(Modality.APPLICATION_MODAL);

            breakdown.setTitle("Student Breakdown");
            breakdown.setScene(new Scene(studentBreakdown, 600, 400));
            breakdown.setResizable(false);
            breakdown.showAndWait();
        }

        catch(IOException e1)
        {
            System.out.println("error opening student breakdown");
        }


    }//END OF ON CLICK VIEW BREAKDOWN

    /*
    |   +onClickBtnSignOut()
    |
    |   Signs out the current user and returns to the login screen
     */
    public void onClickBtnSignOut(ActionEvent e) throws IOException
    {
        Parent loginScreen = FXMLLoader.load(getClass().getResource("LoginNew.fxml"));
        Scene loginScreenScene = new Scene(loginScreen);

        // Close the current database connection
        dbConnection.destroy();

        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        window.setTitle("Login");
        window.setScene(loginScreenScene);

        window.show();

    }//END OF ON CLICK BTN SIGN OUT

    /*
    |   +onClickNewGroup
    |
    |   Creates a new group
    |   Calls:
    |       viewGroup()
     */
    public void onClickNewGrouo(ActionEvent e)
    {
        if(cboAssignmentList.getSelectionModel().isEmpty() ||
                cboAssignmentList.getSelectionModel().getSelectedIndex() == -1)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Assignment Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an assignment.");
            alert.showAndWait();
            return;
        }

        int selectedIndex = cboAssignmentList.getSelectionModel().getSelectedIndex();
        assignmentID = this.assignmentIds.get(selectedIndex);
        boolean result = dbConnection.createNewGroup(this.studentID, this.assignmentID);
        if(!result)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Group Created!");
            alert.setHeaderText(null);
            alert.setContentText("Error creating and adding to group!");
            alert.showAndWait();

        }
        else
        {
            //reload the groups window.
            onClickAssignmentDropdown(new ActionEvent());
            //select the new group.
            lvGroupList.getSelectionModel().selectLast();
            //populate the group members
            viewGroup();
        }
        return;
    }//end onClickNewGrouo

    /*
    |   +onClickWriteReview()
    |
    |   Creates a new review by loading the ReviewController
     */
    public void onClickWriteReview(ActionEvent e)
    {
        //guard against no selection.
        if(lvGroupMemberList.getSelectionModel().isEmpty() ||
                lvGroupMemberList.getSelectionModel().getSelectedIndex() == -1)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Group Member Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a group member.");
            alert.showAndWait();
            return;
        }
        groupMemberSelection = lvGroupMemberList.getSelectionModel().getSelectedItem();
        groupMemberIDSelection = groupMemberIDs.get(lvGroupMemberList.getSelectionModel().getSelectedIndex());

        System.out.println(groupMemberIDs);
        System.out.println(this.studentID);

        //check if we are in this group.
        if(!this.groupMemberIDs.contains(this.studentID))
        {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("You are not in a group with this student!");
            alert.setHeaderText(null);
            alert.setContentText("You are not in a group with this student!");
            alert.showAndWait();
            return;

        }
        boolean inGroup = dbConnection.checkForGroup(this.studentID, assignmentID);
        System.out.println("GMID: " + groupMemberIDSelection);

        try
        {
            FXMLLoader loadReviews = new FXMLLoader(getClass().getResource("SubmitReview.fxml"));
            Parent studentReview = loadReviews.load();
            ReviewController review = loadReviews.getController();

            review.initReview(studentID, groupMemberIDSelection, assignmentID, dbConnection);

            Stage reviews = new Stage();
            reviews.initModality(Modality.APPLICATION_MODAL);

            reviews.setTitle("Submit Review");
            reviews.setScene(new Scene(studentReview, 600, 400));
            reviews.setResizable(false);
            reviews.showAndWait();
        }

        catch(IOException e1)
        {
            //System.out.println("error opening submit review");
        }
    }//end onClickWriteReview



    /*
    |   -viewGroup()
    |
    |   Views the members of the group
     */
    private void viewGroup()
    {
        //guard against nothing selected.
        if(!lvGroupList.getSelectionModel().isEmpty())
        {
            groupNumberSelection = lvGroupList.getSelectionModel().getSelectedItem();
        }
        else
        {
            return;
        }


        System.out.println("AID: " + assignmentID + " GID: " + groupNumberSelection);

        ArrayList<Map<String, String>> listOfGroupMembers = dbConnection.getGroupPartners(assignmentID, groupNumberSelection);
        ArrayList<String> groupMemberList = new ArrayList<>();
        groupMemberIDs = new ArrayList<>();

        if(listOfGroupMembers == null)
        {
            lblErrorLabel.setText("NO GROUP MEMBERS");
            return;
        }

        for (Map<String, String> groupMembers: listOfGroupMembers)
        {
            groupMemberList.add(groupMembers.get("name"));
            groupMemberIDs.add(Integer.parseInt(groupMembers.get("studentId")));
        }

        lvGroupMemberList.setItems(FXCollections.observableArrayList(groupMemberList));
    }//END OF ON CLICK VIEW GROUP

    /*
    |   resetSemesterList()
    |
    |   Clears the combo box and grabs updated data
    |   from the database
     */
    private void resetSemesterList() {
        cboSectionList.getItems().clear();
        this.sectionIds.clear();

        ArrayList<Map<String, String>> listOfSemesters = dbConnection.getStudentSemesters(studentID);
        ArrayList<String> semesterList = new ArrayList<>();

        for (Map<String, String> semesters: listOfSemesters)
        {
            semesterList.add(semesters.get("semester"));
            this.sectionIds.add(Integer.parseInt(semesters.get("sectionId")));
        }

        cboSectionList.setItems(FXCollections.observableArrayList(semesterList));
        cboSectionList.setValue("SEMESTER");
    }// END OF RESET SEMESTER LIST

}//END OF STUDENT CONTROLLER



