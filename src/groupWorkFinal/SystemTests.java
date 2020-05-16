package groupWorkFinal;

import org.junit.Test;
import static org.junit.Assert.*;
import javax.xml.crypto.Data;
import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class SystemTests
{
    private Database testDatabase;


    /*
    |   +SystemTests()
    |
    |   Constructor for the tests.  Logs into the database and populates test data.
    */
    public SystemTests() {
        this.testDatabase = new Database("root", "verySecret", false);
        this.testDatabase.populateTestData();
    }

    /*
    |   +testDatabaseCreation()
    |
    |   This checks that all the tables that should be in the database exits.
    */

    @Test
    public void testDatabaseCreation() {

        //check if tables exist
        try {
            Connection connection = DriverManager.getConnection(testDatabase.getDatabaseName());
            Statement statement = connection.createStatement();

            String[] tables = {"Instructors", "Courses", "Prerequisites", "Sections", "Students", "Reviews",
                    "Enrollments", "Assignments", "Groups"};
            for (String tableName : tables) {
                ResultSet result = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';");

                if (!result.next() || !result.getString("name").equals(tableName)) {
                    fail("There is no " + tableName + " table in the  database!");
                }
                result.close();
            }//end for

            statement.close();

        } catch (SQLException e) {
            fail("error accessing database!");
        }


    }//end testDatabaseCreation

    /*
    |   +testStudentCreation()
    |
    |   This creates a user in the database and queries the database to make sure that all the values
    |   come back correctly.
    */
    @Test
    public void testStudentCreation() {



        String name = "Bill Shatner";
        String email = "Bill@bill.com";
        String address = "123 Sesame Street";
        String city = "Peterborough";
        String province = "Ontario";
        String postalCode = "K9H7S9";
        String password = "StarTrek";

        //delete the user so that we're in a known state
        boolean success = testDatabase.deleteUser(email);
        if (!success) {
            System.out.println("Failed to delete user");
        }

        boolean result = testDatabase.createUser(name, email, address, city, province, postalCode, password, true);
        if (!result) {
            fail("error creating user!");
        }

        //check that the credentials are now valid.
        result = testDatabase.checkLogin();
        if (!result) {
            fail("error creating user - checkLogin failed!");
        }

        Database newDatabase = new Database(email, password, true);

        Map<String, String> userMap = newDatabase.getUser();

        if (!userMap.get("name").equals(name)) {
            fail("error creating user: wrong name returned!");
        }
        if (!userMap.get("address").equals(address)) {
            fail("error creating user: wrong address returned!");
        }
        if (!userMap.get("city").equals(city)) {
            fail("error creating user: wrong city returned!");
        }
        if (!userMap.get("province").equals(province)) {
            fail("error creating user: wrong province returned!");
        }
        if (!userMap.get("postalCode").equals(postalCode)) {
            fail("error creating user: wrong postalCode returned!");
        }


    }//end testStudentCreation

    /*
    |   +testCreateCourse()
    |
    |   This creates a course in the database and queries the database to make sure that all the values
    |   come back correctly from getCourseDetails().
    */

    @Test
    public void testCreateCourse() {
        //first delete course if there.
        String course = "COIS1010";
        String name = "My Digital World";
        String description = "really should have ben cancelled instead of digital security";
        boolean result = testDatabase.deleteCourse(course);

        //create the course
        result = testDatabase.createCourse(course, name, description, .5);
        if (!result) {
            fail();
        }

        //see if it is in the list
        ArrayList<String> courses = testDatabase.getCourses();
        if (!courses.contains(course)) {
            //it's not there.
            fail();
        }

        //see if the details work.
        Map<String, String> details = testDatabase.getCourseDetails(course);
        if (!details.get("courseName").equals(name)) {
            //The name doesn't match.
            fail("The name doesn't match");
        }

        if (!details.get("description").equals(description)) {
            fail("The description doesn't match");
        }

        if (!details.get("numberOfCredits").equals(Double.toString(.5))) {
            fail("The number of credits doesn't match");
        }


    }//end testCreateCourse

    /*
    |   +testCreateSection()
    |
    |   This creates a course followed by a section in the database and queries the database to make sure that all the values
    |   come back correctly from getSections().
    */
    @Test
    public void testCreateSection() {
        String courseCode = "COIS1010";

        testDatabase.createCourse(courseCode, "My Digital World", "oh boy.", .5);

        ArrayList<Map<String, String>> sections = testDatabase.getSections(courseCode);
        //start clean
        for (Map<String, String> map : sections) {
            testDatabase.deleteSection(Integer.parseInt(map.get("sectionId")));
        }
        String semester = "Fall 2018";
        String room = "hidden 123";
        String meetingTimes = "MWF 10:00-11:00 AM";


        int sectionId = testDatabase.createSection(semester, room, meetingTimes, courseCode);
        if (sectionId == -1) {
            fail("createSections failed to return a section ID");
        }

        sections = testDatabase.getSections(courseCode);
        Map<String, String> sectionInfo = sections.get(0);
        if (!sectionInfo.get("semester").equals(semester)) {
            fail("semester data was not read back properly!");
        }

        if (!sectionInfo.get("room").equals(room)) {
            fail("room data was not read back properly!");
        }
        if (!sectionInfo.get("meetingTimes").equals(meetingTimes)) {
            fail("meeting time data was not read back properly!");
        }
        if (!sectionInfo.get("sectionId").equals(Integer.toString(sectionId))) {
            fail("sectionId data was not read back properly!");
        }


    }//end testCreateSection

    /*
   |   +testAddToSection()
   |
   |   This creates a course followed by a section followed by creating two students in the database
   |   and queries the database using getStudentsInSection to make sure the students are actually there.
   |   It then removes the students using removeFromSection() and checks that they are gone
   |   by calling getStudentsInSection().
   |   come back correctly from getSections().
   */
    @Test
    public void testAddToSection() {
        String course = "COIS1010";
        //this are created as test data
        String student1 = "Roy Orbison";
        String email1 = "roy@royorbison.com";
        String password1 = "mysterygirl";

        String student2 = "Buddy Holly";
        String email2 = "buddy@buddyholly.com";
        String password2 = "crickets";

        //the user here is an instructor and can add and remove students from courses.
        testDatabase.createCourse(course, "My Digital World", "Why me", .5);
        int sectionId = testDatabase.createSection("Now", "a big room", "MWF at 10", course);
        if (sectionId == -1) {
            fail("testAddToSection failed to create section!");
        }

        int studentId = testDatabase.getStudentIdFromEmail(email1);
        if (studentId == -1) {
            fail("failed to get student Id!");
        }
        boolean result = testDatabase.addToSection(studentId, sectionId);
        if (!result) {
            fail("failed to add student to section");
        }

        ArrayList<Map<String, String>> students = testDatabase.getStudentsInSection(sectionId);
        //set this to true if we find the student we just added in the section
        boolean found = false;
        for (Map<String, String> student : students) {
            if (Integer.parseInt(student.get("studentId")) == studentId
                    && student.get("name").equals(student1)) {
                found = true;
            }
        }
        if (!found) {
            fail("We didn't find our student in the section.");
        }

        //try removing a student from a section
        boolean removeResult = testDatabase.removeFromSection(studentId, sectionId);
        if (!removeResult) {
            fail("We failed to remove our student from the section!");
        }

        //make sure they are actually gone
        students = testDatabase.getStudentsInSection(sectionId);
        //set this to true if we find the student we just added in the section
        found = false;
        for (Map<String, String> student : students) {
            if (Integer.parseInt(student.get("studentId")) == studentId) {
                found = true;
            }
        }
        if (found) {
            fail("We removed a student from a section and they were still there!");
        }

    }//end test addToSection

    /*
    |   +testAssignments()
    |
    |   This creates a an assignment using createAssignment(), adding it to an existing section.  It
    |   then calls getAssignmentsBySection() to confirm that the assignment was created properly.
    */
    @Test
    public void testAssignments() {
        ArrayList<Map<String, String>> sections = this.testDatabase.getSections("COIS2240");
        if (sections == null) {
            fail("We failed to get a list of sections!");
        }
        //we just need a section - we don't care which one.
        Map<String, String> sectionInfo = sections.get(0);
        int sectionId = Integer.parseInt(sectionInfo.get("sectionId"));
        if (sectionId == -1) {
            fail("We failed to get a valid sections id!!");
        }

        //create an assignment (String name, String description, String dueDate, int sectionId)
        String assignmentName = "testAsignment";
        String assignmentDescription = "test description";
        String dueDate = "Now!!!";

        int assignmentId = testDatabase.createAssignment(assignmentName, assignmentDescription, dueDate, sectionId);
        if (assignmentId == -1) {
            fail("We failed to get a valid assignment id!!");
        }

        //check the values
        boolean match = false;
        ArrayList<Map<String, String>> assignments = testDatabase.getAssignmentsBySection(sectionId);
        for (Map<String, String> assignment : assignments) {
            int thisAssignmentId = Integer.parseInt(assignment.get("assignmentId"));
            if (thisAssignmentId == assignmentId) {
                String thisName = assignment.get("name");
                String thisDescription = assignment.get("description");
                String thisdueDate = assignment.get("dueDate");
                if (thisName.equals(assignmentName) &&
                        thisDescription.equals(assignmentDescription) &&
                        thisdueDate.equals(dueDate)) {
                    match = true;
                }
            }
        }
        if (!match) {
            fail("our assignment didn't make it into the database!");
        }
        //clean up
        boolean deleteResult = testDatabase.deleteAssignment(assignmentId);
        if (!deleteResult) {
            fail("failed to delete the assignment!");
        }

    }


    /*
    |   +testReviews()
    |
    |  This gets two students from the test data, and assignment, and attempts to write reviews using
    |  writeReview().  It then calls getReviews() to confirms that all the expected values are there.  It also
    |   checks that delteReview() works as expected.
    */
    //this depends on the test data being populated.
    @Test public void testReviews()
    {
        String email1 = "roy@royorbison.com";
        String email2 = "buddy@buddyholly.com";

        int studentId1 = testDatabase.getStudentIdFromEmail(email1);
        int studentId2 = testDatabase.getStudentIdFromEmail(email2);
        if(studentId1 == -1 || studentId2 == -1)
        {
            fail("failed to get the student Ids from the database.");
        }

        int sectionId = -1;

        ArrayList<Map<String, String>> courses =  testDatabase.getStudentCourses(studentId1, "Winter 2019");

        for(Map<String, String> course: courses)
        {
            sectionId = Integer.parseInt(course.get("sectionId"));
        }

        if(sectionId == -1)
        {
            fail("we failed to get a sectionId!");
        }

        int assignmentId = -1;
        //get the assignments for the section of the class that we are in.
        ArrayList<Map<String, String>> assignments = testDatabase.getAssignmentsBySection(sectionId);
        if(assignments == null)
        {
            fail("we failed to get our assignments!");
        }
        for(Map<String, String> assignment: assignments)
        {
            //get an assignment
            assignmentId = Integer.parseInt(assignment.get("assignmentId"));
        }
        if(assignmentId == -1)
        {
            fail("we failed to get an assignment!");
        }

        //start clean
        testDatabase.deleteReview(studentId1, studentId2, assignmentId);

        boolean reviewResult = testDatabase.writeReview(studentId1, studentId2, assignmentId, "Great!", 5 );
        if(!reviewResult)
        {
            fail("we failed to submit our review!");
        }

        //check the contents of our review
        boolean matched = false;
        ArrayList<Map<String, String>> reviews = testDatabase.getReviews(studentId2);
        for(Map<String, String> review: reviews)
        {
            if(Integer.parseInt(review.get("writtenById")) == studentId1 &&
                    review.get("reviewText").equals("Great!") &&
                    Integer.parseInt(review.get("numberOfStars")) == 5
            )
            {
                matched = true;
            }
        }

        if(!matched)
        {
            fail("we couldn't locate our review!");
        }

    }

}
