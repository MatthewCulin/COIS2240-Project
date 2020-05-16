package groupWorkFinal;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.*;
import java.util.*;

public class BreakdownController
{
    // DATA MEMBERS
    private Database dbConnection;

    /////////////////////////////////////////////////////////
    // Note: student id here is not the logged in student  //
    // It is the student we are looking at                 //
    /////////////////////////////////////////////////////////
    private int studentID;

    private String courseSelection;
    private String assignmentSelection;

    // For calculating the average stars
    private int starTotal;

    // To work around horrid listView implementation in java.
    private ArrayList<Integer> assignmentIds;
    private ArrayList<Integer> sectionIds;

    @FXML
    private ListView<String> lvCourseList;
    @FXML
    private ListView<String> lvAssignmentList;
    @FXML
    private ListView<String> lvReviewList;

    @FXML
    private Label studentName;
    @FXML
    private Label averageRating;


    /*
    |   +setDbConnection()
    |
    |   Sets the database connection for the current user
    */
    public void setDbConnection(Database dbConnection)
    {
        this.dbConnection = dbConnection;
    }//END OF SET DB CONNECTION


    /*
    |   +setText()
    |
    |   Sets the studentName label to the name of the student being viewed
    */
    public void setText(int studentSelected)
    {
        this.studentID = studentSelected;

        if(this.studentName != null)
        {
            this.studentName.setText(dbConnection.getStudentName(studentSelected));
        }
    }//END OF SET TEXT


    /*
    |   +initBreakdown()
    |
    |   Used to initialize the BreakdownController
    |   Populates the courses list with all courses the student is enrolled in
    */
    public void initBreakdown()
    {
        dbConnection.checkLogin();

        ArrayList<Map<String, String>> listOfCourses = dbConnection.getAllStudentCourses(studentID);
        ArrayList<String> courseList = new ArrayList<>();
        this.assignmentIds = new ArrayList<>();
        this.sectionIds = new ArrayList<>();

        for(Map<String, String> courses: listOfCourses)
        {
            courseList.add(courses.get("courseCode") + " - " + courses.get("semester"));
            //keep list of sectionIds
            this.sectionIds.add(Integer.parseInt(courses.get("sectionId")));
        }

        lvCourseList.setItems(FXCollections.observableArrayList(courseList));

        double averageStars = dbConnection.getStudentReviewAverage(studentID);
        averageRating.setText(Double.toString(averageStars));

        // Set our click events for the course list box
        lvCourseList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                viewAssignments();
            }
        });

        //set our click event on the assignment list
        lvAssignmentList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                viewReviews();
            }
        });

    }//END OF INIT BREAKDOWN


    /*
    |   +viewAssignments()
    |
    |   Determines the selected course and populates the assignment list with the assignments for the course
    */
    private void viewAssignments()
    {
        // Checks to see if the selection is empty
        if(lvCourseList.getSelectionModel().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Course Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a course.");
            alert.showAndWait();
            return;
        }

        courseSelection = lvCourseList.getSelectionModel().getSelectedItem();
        int selectedIndex = lvCourseList.getSelectionModel().getSelectedIndex();

        if(selectedIndex == -1)
        {
            return;
        }

        // Start fresh
        this.assignmentIds.clear();

        ArrayList<Map<String, String>> listOfAssignments = dbConnection.getAssignmentsBySection(this.sectionIds.get(selectedIndex));
        ArrayList<String> assignmentList = new ArrayList<>();

        for (Map<String, String> assignments: listOfAssignments)
        {
            assignmentList.add(assignments.get("name"));
            this.assignmentIds.add(Integer.parseInt(assignments.get("assignmentId")));
        }

        lvAssignmentList.setItems(FXCollections.observableArrayList(assignmentList));
    }//END OF VIEW ASSIGNMENTS


    /*
    |   +viewReviews()
    |
    |   Determines the selected assignment and displays the reviews for the assignment (if any) for the student
    |   Selects assignment to review
    */
    public void viewReviews()
    {
        if (lvAssignmentList.getSelectionModel().isEmpty())
        {
            return;
        }

        int assignmentId = this.assignmentIds.get(lvAssignmentList.getSelectionModel().getSelectedIndex());

        ArrayList<Map<String, String>> listOfReviews = dbConnection.getReviewsByAssignment(studentID, assignmentId);
        ArrayList<String> reviewsList = new ArrayList<>();

        for(Map<String, String> reviews: listOfReviews)
        {
            reviewsList.add(reviews.get("writtenBy") + " - " + reviews.get("numberOfStars") + ": " + reviews.get("reviewText"));
        }

        lvReviewList.setItems(FXCollections.observableArrayList(reviewsList));

    }//END OF VIEW REVIEWS


    /*
    |   +onClickClose()
    |
    |   Closes the BreakdownController and returns the user to the StudentController
    */
    public void onClickClose(ActionEvent e)
    {
        // Close the window
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }//END OF ON CLICK CLOSE

}//END OF STUDENT VIEW CONTROLLER
