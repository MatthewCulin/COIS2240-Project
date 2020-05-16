package groupWorkFinal;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class ReviewController
{
    // DATA MEMBERS
    private Database dbConnection;
    private boolean inGroupWithPartner;
    private int studentID;
    private int partnerID;
    private int assignmenmtID;
    private int numberOfStars;
    private String review;
    private boolean isReviewSubmitted;

    @FXML
    private Label lblPartnerName;
    @FXML
    private Label lblStars;
    @FXML
    private Label lblReview;
    @FXML
    private Label lblErrorMsg;

    @FXML
    private TextField tfReviewBox;

    @FXML
    private TextArea taCommentBox;


    /*
    |   +initReview()
    |
    |   Sets all ID's and database connection
    */
    public void initReview(int studentID, int partnerID, int assignmentID, Database dbConnection)
    {
        this.dbConnection = dbConnection;
        this.studentID = studentID;
        this.partnerID = partnerID;
        this.assignmenmtID = assignmentID;
        this.lblPartnerName.setText(dbConnection.getStudentName(partnerID));
    }//END OF INIT REVIEW


    /*
    |  + onClickBtnSubmitReview()
    |
    |   Submits the review created for the student
    |   Closes the ReviewController and returns the user to the StudentController
    */
    public void onClickBtnSubmitReview(ActionEvent e)
    {
        review = taCommentBox.getText();

        // CHECK RATING INPUT
        if (tfReviewBox.getText().matches("[0-5]"))
        {
            // Single Digit ONLY range from 0-5
            numberOfStars = Integer.parseInt(tfReviewBox.getText());
            lblStars.setTextFill(Color.BLACK);
            lblErrorMsg.setVisible(false);
            // MOVED review = -> up to top
        }

        else
        {
            lblStars.setTextFill(Color.RED);
            lblErrorMsg.setText("Rating must be between 0 - 5");
            lblErrorMsg.setVisible(true);
            return;
        }

        // CHECK TEXT INPUT
        if (review.equals(""))
        {
            lblReview.setTextFill(Color.RED);
            lblErrorMsg.setText("Must submit comments");
            lblErrorMsg.setVisible(true);
            return;
        }

        else
        {
            lblErrorMsg.setVisible(false);
            lblErrorMsg.setTextFill(Color.BLACK);

            isReviewSubmitted = dbConnection.writeReview(studentID, partnerID, assignmenmtID, review, numberOfStars);
        }

        if (isReviewSubmitted)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Review recorded");
            alert.setHeaderText(null);
            alert.setContentText("Your review was submitted.");
            alert.showAndWait();

            //close the window
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        }

        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("review failed");
            alert.setHeaderText(null);
            alert.setContentText("The review could not be recorded.  It may already exist.");
            alert.showAndWait();
        }

    }//END OF ON CLICK SUBMIT REVIEW


    /*
    |   +onClickBtnCancel
    |
    |   Closes the ReviewController and returns the user to the StudentController
    */
    public void onClickBtnCancel(ActionEvent e)
    {
        // CLOSE THIS WINDOW
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }// END OF ON CLICK BTN CANCEL

}//END OF REVIEW CONTROLLER
