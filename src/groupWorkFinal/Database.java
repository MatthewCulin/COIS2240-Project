package groupWorkFinal;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.sqlite.SQLiteConfig;

public class Database
{
    // DATA MEMBERS

    private String username;                                    //user name of student or instructor
    private String password;                                    //password of student or instructor
    private boolean isStudent;
    private  String hashedPassword;
    private Connection connection;
    private  String databaseName = "jdbc:sqlite:database.db";
    private int userId;                                         //might be studentId ot instructorId

    /*
    |   +Database()
    |
    |   Set the parameters to the passed variables from the LoginController
    */
    public Database(String username, String password, boolean isStudent)
    {
        this.username = username;
        this.password = password;
        this.isStudent = isStudent;
        this.hashedPassword = null;

        try
        {
            // Because sqlite turns of constraints by default
            // this doesn't solve the problem. SQLite sucks. On Cascade not working.
            SQLiteConfig configuration = new SQLiteConfig();
            configuration.enforceForeignKeys(true);
            this.connection = DriverManager.getConnection(databaseName);

            // invalid
            this.userId = -1;

            // Check to see if our connection is good and create default tables
            if(this.connection.isValid(10))
            {
                // Create our tables if they aren't there.
                this.createTables();
            }
        }

        catch(SQLException e)
        {
            System.out.println("Error opening SQL database!");
        }

    }//END OF DATABASE CONSTRUCTOR


    /*
    |   +destroy()
    |
    |   Closes the connection to the database upon logout
    */
    public void destroy()
    {
        try
        {
            this.connection.close();
        }

        catch(SQLException e)
        {
            System.out.println("error closing SQL connection.");
        }

        this.hashedPassword = "";
        this.password = "";
        this.userId = -1;
        this.username = "";
    }//END OF DESTROY


    /*
    |   getNewSalt()
    |
    |   returns a new salt for salting a password
    */
    String getNewSalt()
    {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }//END OF GET NEW SALT


    /*
    |   +createUser()
    |
    |   Creates a user based on username (email) and password passed in to the database object.
    |   Called to create a new user.
    |   This might be an instructor ot a student.
    |   Returns true on success and false on failure
    */
    public boolean createUser(String name, String email, String address, String city,
                              String province, String postalCode, String password, boolean isStudent)
    {

        // Create our salt and store it in the database
        String salt = this.getNewSalt();

        // Hash the password
        String hashed = this.hashPassword(password, salt);

        if(hashed == null)
        {
            // Failed to hash!
            return false;
        }

        try
        {
            // Populate the database
            String table;

            if(isStudent)
            {
                table = "Students";
            }

            else
            {
                table = "Instructors";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "INSERT INTO " + table + " (email, name, email, address, city, province, postalCode, " +
                    "salt, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, email);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, address);
            statement.setString(5, city);
            statement.setString(6, province);
            statement.setString(7, postalCode);
            statement.setString(8, salt);
            statement.setString(9, hashed);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("createUser: failed to create the user in the database!");
                statement.close();
                return false;
            }

            // Get the newly created ID
            ResultSet resultSet= statement.getGeneratedKeys();

            // Close the set or we get locked errors.
            resultSet.close();
            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("createUser - error creating user! " + e.toString());
        }

        return true;

    }//END OF CREATE USER


    /*
    |   getUser()
    |
    |   Gets a users details wrapped in a map: name, address, city, province, postalCode.
    |   Takes the username (email) as a parameter.
    */
    Map<String, String> getUser()
    {
        // Check the user credentials
        if(!checkCredentials())
        {
            //no can do!
            return null;
        }

        Map<String, String> user = new HashMap<String, String>();

        try
        {
            String table;

            if(this.isStudent)
            {
                table = "Students";
            }

            else
            {
                table = "Instructors";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "SELECT name, address, city, province, postalCode FROM " + table + " WHERE email is ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the user name passed in
            statement.setString(1, this.username);

            // Execute the query to get the user data
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                user.put("name", result.getString("name"));
                user.put("address", result.getString("address"));
                user.put("city", result.getString("city"));
                user.put("province", result.getString("province"));
                user.put("postalCode", result.getString("postalCode"));
            }

            else
            {
                // Not there.  Bail.
                System.out.println("getUser: failed to get the user from the database!");
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getUser: error getting our user from the database!");
            return null;
        }

        return user;
    }//END OF GET USER


    /*
    |   +deleteUser()
    |
    |   Deletes the current user from the database and returns true on success and false on failure
    */
    public boolean deleteUser(String email)
    {
        // Check to ensure the user is not a student
        if(!checkCredentials() || this.isStudent)
        {
            //no can do
            return false;
        }

        try
        {
            // Populate the database
            String table;
            String id;

            if(this.isStudent)
            {
                table = "Students";
            }

            else
            {
                table = "Instructors";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "DELETE FROM " + table + " WHERE email = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, email);

            // Execute the query
            int affectedRows = statement.executeUpdate();
            statement.close();

            if(affectedRows == 0)
            {
                //something went wrong..  Bail.
                return false;
            }

            return true;
        }

        catch(SQLException e)
        {
            System.out.println("deleteUser: error deleting user!" + e.toString());
            return false;
        }

    }//END OF DELETE USER


    /*
    |   +createCourse()
    |
    |   Called to create class.
    |   Fails if logged in user isn't an instructor.
    |   parameters: courseCode, CourseName, description, and number of credits
    |   courseCode becomes the unique ID
    |   Return true on success and false on failure.
    */
    public boolean createCourse(String courseCode, String courseName, String description, double numberOfCredits )
    {
        boolean success = false;

        // Check to ensure the user is not a student
        if(!this.checkCredentials() || this.isStudent)
        {
            //no can do
            return false;
        }

        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement - get the salt for our password
            String query = "INSERT INTO courses (courseCode, courseName, description, numberOfCredits) VALUES (?, ?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, courseCode);
            statement.setString(2, courseName);
            statement.setString(3, description);
            statement.setDouble(4, numberOfCredits);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("createCourse: failed to create the courses in the database!");
                statement.close();
                return false;
            }

            else
            {
                success = true;
            }

            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("createCourse - error creating course! " + e.toString());
            success = false;
        }

        return success;

    }//END OF CREATE COURSES


    /*
    |   +deleteCourse()
    |
    |   Called to delete a course.
    |   Fails if logged in user isn't an instructor.
    |   parameters: courseCode
    |   return true on success and false on failure.
    */
    public boolean deleteCourse(String courseCode)
    {
        // Check to ensure the user is not a student
        if(!this.checkCredentials() || this.isStudent)
        {
            //no can do
            return false;
        }

        boolean success = false;
        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement
            String query = "DELETE FROM courses WHERE courseCode = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, courseCode);

            // Execute the query
            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("deleteCourse: failed to delete the course in the database!");
                statement.close();
                return false;
            }

            statement.close();
            success = true;
        }

        catch (SQLException e)
        {
            System.out.println("deleteCourse - error deleting class! " + e.toString());
            success = false;
        }

        return success;
    }//END OF DELETE COURSE


    /*
    |   +getCourses()
    |
    |   Used to get a list of the courses currently offered.
    |   Both students and instructors can access this list
    |   returns an array list course codes
    */
    public ArrayList<String> getCourses()
    {
        // Check the user credentials
        if(!this.checkCredentials())
        {
            //no can do
            return null;
        }

        ArrayList<String> courses = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT courseCode FROM courses";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                courses.add( result.getString("courseCode"));
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getCourses: error getting our courses from the database!");
            return null;
        }

        return courses;
    }//END OF GET COURSES


    /*
    |   getCourseDetails()
    |
    |   This takes a course code as a parameter and returns the details of a course in a map.
    */
    Map<String, String> getCourseDetails(String courseCode)
    {
        // Check the user credentials
        if(!this.checkCredentials())
        {
            //no can do
            return null;
        }

        Map<String, String> courseDetails = new HashMap<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT courseName, description, numberOfCredits FROM courses WHERE courseCode = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);
            //escape the parameters
            statement.setString(1, courseCode);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                courseDetails.put("courseName",  result.getString("courseName"));
                courseDetails.put("description",  result.getString("description"));

                // Pass this as a string so we can use the map.
                courseDetails.put("numberOfCredits", Double.toString(result.getDouble("numberOfCredits")));
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getCourseDetails: error getting our course details from the database!");
            return null;
        }

        return courseDetails;
    }// END OF GET COURSE DETAILS


    /*
    |   +createSection()
    |
    |   Called to create a section of a class.
    |   Fails if logged in user isn't an instructor.
    |   parameters: semester, room, meetingTimes, and courseCode
    |   Uses the current users Id as instructorID.
    |   Return sectionId on success and -1 on failure.
    */
    public int createSection(String semester, String room, String meetingTimes, String courseCode)
    {
        int sectionId = -1;

        // Check to ensure the user is not a student
        if(! this.checkCredentials() || this.isStudent)
        {
            //no can do
            return sectionId;
        }

        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement - get the salt for our password
            String query = "INSERT INTO sections (semester, room, meetingTimes, instructorId, courseCode) VALUES (?, ?, ?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, semester);
            statement.setString(2, room);
            statement.setString(3, meetingTimes);
            statement.setInt(4, this.userId);
            statement.setString(5, courseCode);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("createSection: failed to create the section in the database!");
                statement.close();
                return sectionId;
            }

            // Get the newly created ID
            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next())
            {
                sectionId = resultSet.getInt(1);
            }

            else
            {
                System.out.println("createUser: no userId returned!");
                resultSet.close();
                statement.close();
                return sectionId;
            }

            // Close the set or we get locked errors.
            resultSet.close();
            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("createSection - error creating section! " + e.toString());
        }

        return sectionId;
    }//END OF CREATE SECTION


    /*
    |   +deleteSection()
    |
    |   Called to delete a section.
    |   Fails if logged in user isn't an instructor.
    |   parameters: sectionId
    |   Return true on success and false on failure.
    */
    public boolean deleteSection(int sectionId)
    {
        boolean success = false;
        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement - get the salt for our password
            String query = "DELETE FROM sections WHERE sectionId = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, sectionId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("deleteSection: failed to delete the section in the database!");
                statement.close();
                return false;
            }

            statement.close();

            success = true;
        }

        catch (SQLException e)
        {
            System.out.println("deleteSection - error deleting section! " + e.toString());
            success = false;
        }

        return success;
    }//END OF DELETE SECTION


    /*
    |   +getSections()
    |
    |   Returns all the info related to the sections of a course in an arraylist of Maps.
    |   The map contains sectionId (as a String), semester, room, meetingTimes, and InstructorName
    |   Takes a course code as a parameter
    */
    public ArrayList<Map<String, String>> getSections(String courseCode)
    {
        // Check the user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> sections = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT sectionId, semester, room, meetingTimes, instructors.name as instructorName " +
                    "FROM Sections INNER JOIN Courses ON Sections.courseCode = Courses.courseCode " +
                    "INNER JOIN Instructors ON Sections.instructorId = Instructors.instructorId " +
                    "WHERE Sections.courseCode = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, courseCode);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> sectionDetail = new HashMap<>();

                // Use a string for the map.
                sectionDetail.put("sectionId",  Integer.toString(result.getInt("sectionId")));
                sectionDetail.put("semester",  result.getString("semester"));
                sectionDetail.put("room",  result.getString("room"));
                sectionDetail.put("meetingTimes",  result.getString("meetingTimes"));
                sectionDetail.put("instructorName",  result.getString("instructorName"));
                sections.add(sectionDetail);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getSections: error getting our sections from the database!");
            return null;
        }

        return sections;
    }//END OF GET SECTIONS


    /*
    |   +addToSection()
    |
    |   Adds a student currently logged in to a section of a course
    |   Takes the sectionId as an integer.
    |   Calls the private version to do the work.
    |   Return true on success and false on failure
    */
    public boolean addToSection(int sectionId)
    {
        // Check to ensure the user is a student
        if(this.checkCredentials() && this.isStudent)
        {
            return this.addToSection(this.userId, sectionId);
        }

        else
        {
            return false;
        }
    }// END OF ADD TO SECTION


    /*
    |   +getStudentIdFromEmail()
    |
    |   Gets the unique key from the students table given the email address
    |   This is a utility function called by addToSection.
    |   Returns the studentId if successful and -1 if it fails.
     */
    public int getStudentIdFromEmail(String email)
    {
        // Ensures the current user is not a student
        if(!this.checkCredentials() || this.isStudent)
        {
            // Instructors only
            // No can do
            return -1;
        }

        int studentId = -1;

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT studentId FROM students WHERE email = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, email);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if (result.next())
            {
                studentId = result.getInt("studentId");
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            // This is very bad indeed. But do nothing.  -1 will be returned.
        }

        return studentId;
    }//END OF GET STUDENT ID FROM EMAIL


    /*
    |   +addToSection()
    |
    |   Takes an email address and a sectionId and enrolls the student in the course
    |   Returns true on success and false on failure.
    |   Mostly for testing. Also called by public version that adds the currently logged in student.
     */
    public boolean addToSection(int studentId , int sectionId)
    {
        // Instructors and students adding themselves only
        if(!this.checkCredentials() || (this.isStudent && studentId != this.userId))
        {
            // No can do
            return false;
        }

        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement
            String query = "INSERT INTO Enrollments (studentId, sectionId) VALUES (?, ?)";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("addToSection: failed to create the Enrollment entry in the database!");
                statement.close();
                return false;
            }

            // Close the set or we get locked errors.
            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("addToSection - error creating Enrollment! " + e.toString());
        }

        return true;
    }// END OF ADD TO SECTION


    /*
    |   +removeFromSection()
    |
    |   Deletes an enrollment from a section
    |   Return true on success and false on failure
    |   An instructor can do this and a logged in student can remove themselves.
    */
    public boolean removeFromSection(int userId, int sectionId)
    {
        // Ensure the user is not a student
        if(this.isStudent && this.userId != userId)
        {
            // No can do.
            return false;
        }

        boolean success = false;
        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement
            String query = "DELETE FROM Enrollments WHERE studentId = ? AND sectionId = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, userId);
            statement.setInt(2, sectionId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("removeFromSection: failed to delete the Enrollemnt in the database!");
                statement.close();
                return false;
            }

            else
            {
                success = true;
            }

            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("removeFromSection - error deleting Enrollment! " + e.toString());
            success = false;
        }

        return success;
    }//END OF REMOVE FROM SECTION


    /*
    |   +getStudentCourses()
    |
    |   Gets a list of courses that a student is enrolled in
    |   Takes a studentId and a semester string
    |   Returns an array list of a map that contains the course information (code, sectionId (as string), name, description)
    */
    public ArrayList<Map<String, String>> getStudentCourses(int studentId, String semester)
    {
        // Check user credentials
        if(!this.checkCredentials() || (this.isStudent && this.userId != studentId))
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> courses = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT Courses.courseCode, courseName, description, Sections.sectionId FROM Courses " +
                    "INNER JOIN Sections ON Courses.courseCode = Sections.courseCode " +
                    "INNER JOIN Enrollments ON Sections.sectionId = Enrollments.sectionId " +
                    "WHERE Enrollments.studentId = ? AND Sections.semester = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setString(2, semester);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> course = new HashMap<>();

                // Use a string for the map.
                course.put("courseCode",  result.getString("courseCode"));
                course.put("courseName",  result.getString("courseName"));
                course.put("description",  result.getString("description"));
                course.put("sectionId", Integer.toString(result.getInt("sectionId")));
                courses.add(course);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getStudentCourses: error getting our courses from the database!");
            return null;
        }

        return courses;
    }//END OF GET STUDENT COURSES


    /*
    |   +getStudentsInSection()
    |
    |   Gets a list of the course sections that a student is enrolled in
    |   Takes a sectionId as a parameter.
    |   Returns an array list of a map where the first value is the courseCode and the second is the name.
    */
    public ArrayList<Map<String, String>> getStudentsInSection(int sectionId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> students = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT students.studentId, students.name FROM Students INNER JOIN Enrollments " +
                    "ON Students.studentId = Enrollments.studentId " +
                    "WHERE Enrollments.sectionId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, sectionId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> student = new HashMap<>();

                // Use a string for the map.
                student.put("studentId",  Integer.toString(result.getInt("studentId")));
                student.put("name",  result.getString("name"));
                students.add(student);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getSections: error getting our sections from the database!");
            return null;
        }

        return students;
    }//END OF GET STUDENTS IN SECTION


    /*
    |   +createAssignment()
    |
    |   Called to create an assignment for a section.
    |   Fails if logged in user isn't an instructor.
    |   parameters: name, description, dueDate, sectionId
    |   Return assignmentId on success and -1 on failure.
    */
    public int createAssignment(String name, String description, String dueDate, int sectionId)
    {
        int assignmentId = -1;

        // Check to ensure the user is not a student
        if(! this.checkCredentials() || this.isStudent)
        {
            // No can do
            return assignmentId;
        }

        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement - get the salt for our password
            String query = "INSERT INTO Assignments (name, description, dueDate, sectionId) VALUES (?, ?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, dueDate);
            statement.setInt(4, sectionId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("createAssignment: failed to create the assignment in the database!");
                statement.close();
                return assignmentId;
            }

            // Get the newly created ID
            ResultSet resultSet= statement.getGeneratedKeys();

            if (resultSet.next())
            {
                assignmentId = resultSet.getInt(1);
            }

            else
            {
                System.out.println("createAssignment: no assignmentId returned!");
                resultSet.close();
                statement.close();
                return assignmentId;
            }

            // Close the set or we get locked errors.
            resultSet.close();
            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("createAssignment - error creating assignment! " + e.toString());
        }

        return assignmentId;
    }//END OF CREATE ASSIGNMENT


    /*
    |  +getAssignmentsBySection()
    |
    |  Gets a list of assignments for a section of a course
    |  Takes a sectionId as a parameter.
    |  Returns an array list of maps where the map contains the assignmentId, name, description, and due date.
    |  The assignmentId is an int encoded as a string.
    */
    public ArrayList<Map<String, String>> getAssignmentsBySection(int sectionId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> assignments = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT assignmentId, name, description, dueDate FROM assignments " +
                    "INNER JOIN sections ON Assignments.sectionId = Sections.sectionId " +
                    "WHERE Assignments.sectionId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, sectionId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> assignment = new HashMap<>();

                // Use a string for the map.
                assignment.put("assignmentId",  Integer.toString(result.getInt("assignmentId")));
                assignment.put("name",  result.getString("name"));
                assignment.put("description",  result.getString("description"));
                assignment.put("dueDate",  result.getString("dueDate"));
                assignments.add(assignment);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getAssignments: error getting our assignment from the database!");
            return null;
        }

        return assignments;
    }//END GET ASSIGNMENT BY SECTION


    /*
    |   +deleteAssignment()
    |
    |   Deletes an assignment from the assignments table
    |   Return true on success and false on failure
    |   An instructor can do this.
    */
    public boolean deleteAssignment(int assignmentId)
    {
        // Check user credentials
        if(this.isStudent || !checkCredentials())
        {
            // No can do.
            return false;
        }

        boolean success = false;
        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement
            String query = "DELETE FROM Assignments WHERE assignmentId = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, assignmentId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("deletAssignment: failed to delete the assignment in the database!");
                statement.close();
                return false;
            }

            else
            {
                success = true;
            }

            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("deleteAssignment - error deleting Assignment! " + e.toString());
            success = false;
        }

        return success;
    }//END OF DELETE ASSIGNMENT


    /*
    |   assignToGroup()
    |
    |   This method assigns a student to a group number for an assignment.
    |   parameters: studentId, assignmentId, groupNumber
    |   This must be done by an instructor.
    |   Students will only be able to submit reviews for other students who match their group number.
    |   Returns true on success and false on failure.
    */
    boolean assignToGroup(int studentId, int assignmentId, int groupNumber)
    {
        // Check user credentials
        if(! this.checkCredentials())
        {
            //no can do
            return false;
        }

        PreparedStatement statement;

        try
        {
            // See if we are already assigned.
            String query = "SELECT groupNumber FROM Groups " +
                    "WHERE studentId = ? and assignmentId = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                // There is already a group in there
                result.close();
                statement.close();
                return false;
            }

            // Make our query for the prepared statement
            query = "INSERT INTO groups (studentId, assignmentId, groupNumber) VALUES (?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);
            statement.setInt(3, groupNumber);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("assignToGroup: failed to create the group assignment in the database!");
                statement.close();
                return false;
            }

            // Close or we get locked errors.
            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("assignToGroup - error creating group assignment! " + e.toString());
            return false;
        }

        return true;
    }//END OF ASSIGN TO GROUP


    /*
    |   +createNewGroup()
    |
    |   This takes a studentId, an assignmentId and creates a new group with a
    |   Number one more than what was there already
    |   Called from StudentController()
    |   parameters: studentId, assignmentId
    */
    public boolean createNewGroup(int studentId, int assignmentId)
    {
        // Check user credentials
        if(!checkCredentials())
        {
            return false;
        }

        int newGroupNumber;

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT Max(groupNumber) from Groups WHERE assignmentId = ?";

            // Get the prepared statement so we can escape input
            PreparedStatement statement = this.connection.prepareStatement(query);
            // Escape the parameters
            statement.setInt(1, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                // One more than the largest
                newGroupNumber = result.getInt(1) + 1;

            }

            else
            {
                newGroupNumber = 1;
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getAssignments: error getting our max group number!");
            return false;
        }

        return assignToGroup(studentId, assignmentId, newGroupNumber);
    }//END OF CREATE GROUP


    /*
    |   +getGroupPartners()
    |
    |   Gets the group partners for a student so they know who they can review
    |   parameters: assignmentId, groupNumber
    |   Returns an arrayList of Maps that contain studentId (as a String) and studentName
    */
    public ArrayList<Map<String, String>> getGroupPartners(int assignmentId, int groupNumber)
    {
        // Check user credentials
        if(!checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> group = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT Students.studentId, name FROM students " +
                    "INNER JOIN groups ON Students.studentId = Groups.studentId " +
                    "WHERE Groups.assignmentId = ? AND Groups.groupNumber = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);
            // Escape the parameters
            statement.setInt(1, assignmentId);
            statement.setInt(2, groupNumber);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> partner = new HashMap<>();

                // Use a string for the map.
                partner.put("studentId",  Integer.toString(result.getInt("studentId")));
                partner.put("name",  result.getString("name"));
                group.add(partner);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getGroupPartners: error getting our group from the database!");
            return null;
        }

        return group;
    }//END OF GET GROUP PARTNERS


    /*
    |   +removeFromGroup()
    |
    |   This can be done by an instructor
    |   Parameters: studentId, assignmentId, and groupNumber
    |   Returns true on success and false on failure.
    */
    public boolean removeFromGroup(int studentId, int assignmentId, int groupNumber)
    {
        // Check user credentials
        if(this.isStudent || !checkCredentials())
        {
            // No can do.
            return false;
        }

        boolean success = false;
        PreparedStatement statement;

        try
        {
            // Make our query for the prepared statement
            String query = "DELETE FROM Groups WHERE studentId = ? AND assignmentId = ? AND groupNumber = ?";

            // Get the prepared statement so we can escape the email argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);
            statement.setInt(3, groupNumber);

            // Execute the query
            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("removeFromGroup: failed to delete the group membership in the database!");
                statement.close();
                return false;
            }

            else
            {
                success = true;
            }

            statement.close();
        }

        catch (SQLException e)
        {
            System.out.println("removeFromGroup- error deleting from Group! " + e.toString());
            success = false;
        }

        return success;
    }//END OF REMOVE FROM GROUP


    /*
    |   +writeReview()
    |
    |   This allows a student write a review.
    |   parameters: studentId, otherStudentsId, asignmentId, review, and numberOfStars
    |   Returns true on success or false on failure.
    */
    public boolean writeReview(int studentId, int otherStudentsId, int assignmentId, String review, int numberOfStars)
    {
        // Check user credentials
        if(!this.checkCredentials() || (this.isStudent && this.userId != studentId ))
        {
            // No can do!
            return false;
        }

        // Verify student is not attempting to review themselves
        if(studentId == otherStudentsId)
        {
            // Can't review yourself!
            return false;
        }

        // Check if its already there.

        // Get the group number (if any) of the student for this assignment
        int myGroupNumber = -1;

        // Get the group number of the other student (if any) for this assignment.
        int otherGroupNumber = -2;

        boolean success = false;

        // If they match, record the review.
        try
        {
            // Guard against repeating the review
            // Make our query for the prepared statement
            String query = "SELECT reviewId FROM Reviews " +
                    "WHERE writtenById = ? and writtenAboutId = ? AND assignmentId = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, otherStudentsId);
            statement.setInt(3, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                // Already there.
                result.close();
                statement.close();
                return false;
            }

            result.close();
            statement.close();

            // Make our query for the prepared statement
            query = "SELECT groupNumber FROM Groups " +
                    "WHERE studentId = ? and assignmentId = ?";

            // Get the prepared statement so we can escape argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);

            // Execute the query to get the data
            result = statement.executeQuery();

            if(result.next())
            {
                myGroupNumber = result.getInt("groupNumber");
            }

            result.close();
            statement.close();

            query = "SELECT groupNumber FROM Groups " +
                    "WHERE studentId = ? and assignmentId = ?";

            // Get the prepared statement so we can escape argument
            statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, otherStudentsId);
            statement.setInt(2, assignmentId);

            // Execute the query to get the data
            result = statement.executeQuery();

            if(result.next())
            {
                otherGroupNumber = result.getInt("groupNumber");
            }

            result.close();
            statement.close();

            if(myGroupNumber == otherGroupNumber)
            {
                // Make our query for the prepared statement
                query = "INSERT INTO Reviews (reviewText, numberOfStars, writtenById, writtenAboutId, assignmentId) VALUES (?, ?, ?, ?, ?)";

                // Get the prepared statement so we can escape the email argument
                statement = this.connection.prepareStatement(query);

                // Escape the parameters
                statement.setString(1, review);
                statement.setInt(2, numberOfStars);
                statement.setInt(3, studentId);
                statement.setInt(4, otherStudentsId);
                statement.setInt(5, assignmentId);

                // Execute the query
                int affectedRows = statement.executeUpdate();

                if(affectedRows == 0)
                {
                    // Something went wrong..  Bail.
                    System.out.println("writeReview: failed to create the review in the database!");
                    statement.close();
                    return false;
                }

                else
                {
                    success = true;
                }

                // Close or we get locked errors.
                statement.close();
            }
        }

        catch(SQLException e)
        {
            System.out.println("writeReview: error getting our group from the database!");
            return false;
        }

        return success;
    }//END OF WRITE REVIEW


    /*
    |   +getReviews()
    |
    |   Get's the reviews of a student
    |   parameter: studentId
    |   Returns an ArrayList of Maps that contain the assignmentId, the review text, and the number of stars.
    */
    public ArrayList<Map<String, String>> getReviews(int studentId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> reviews = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT reviewId, writtenById, assignmentId, reviewText, numberOfStars, Students.name as writer " +
                    "FROM Reviews INNER JOIN Students ON Reviews.writtenById = Students.studentId " +
                    "WHERE writtenAboutId = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> review = new HashMap<>();

                // Use a string for the map.
                review.put("reviewId",  Integer.toString(result.getInt("reviewId")));
                review.put("writtenById",  Integer.toString(result.getInt("writtenById")));
                review.put("numberOfStars",  Integer.toString(result.getInt("numberOfStars")));
                review.put("reviewText",  result.getString("reviewText"));
                review.put("writtenBy", result.getString("writer"));
                review.put("assignmentId", result.getString("assignmentId"));

                reviews.add(review);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getReviews: error getting our review from the database!");
            return null;
        }

        return reviews;
    }//END OF GET REVIEWS


    /*
    |   +getReviewsByAssignment()
    |
    |   Get's the reviews of a student by assignment id
    |   parameter: studentId, assignemntId
    |   Returns an ArrayList of Maps that contain the assignmentId, the review text, and the number of stars.
    */
    public ArrayList<Map<String, String>> getReviewsByAssignment(int studentId, int assignmentId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> reviews = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT reviewId, writtenById, assignmentId, reviewText, numberOfStars, Students.name as writer " +
                    "FROM Reviews INNER JOIN Students ON Reviews.writtenById = Students.studentId " +
                    "WHERE writtenAboutId = ? AND assignmentId = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> review = new HashMap<>();

                // Use a string for the map.
                review.put("reviewId",  Integer.toString(result.getInt("reviewId")));
                review.put("writtenById",  Integer.toString(result.getInt("writtenById")));
                review.put("numberOfStars",  Integer.toString(result.getInt("numberOfStars")));
                review.put("reviewText",  result.getString("reviewText"));
                review.put("writtenBy", result.getString("writer"));
                review.put("assignmentId", result.getString("assignmentId"));

                reviews.add(review);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getReviews: error getting our review from the database!");
            return null;
        }

        return reviews;
    }//END OF GET REVIEWS BY ASSIGNMENT


    /*
    |   +deleteReview()
    |
    |   Deletes the reviews of a student
    |   Only an instructor can do this.
    |   parameters: writerId, writtenOfId, assignmentId
    |   Returns true on success, false on failure
    */
    public boolean deleteReview(int writerId, int writtenAboutId, int assignmentId)
    {
        // Check to ensure the user os not a student
        if(!this.checkCredentials() || this.isStudent)
        {
            // No can do
            return false;
        }

        boolean success = true;

        try
        {
            // Make our query for the prepared statement
            String query = "DELETE FROM Reviews " +
                    "WHERE writtenById = ? AND writtenAboutId = ? AND assignmentId = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, writerId);
            statement.setInt(2, writtenAboutId);
            statement.setInt(3, assignmentId);

            // Execute the query
            int affectedRows = statement.executeUpdate();

            if(affectedRows == 0)
            {
                // Something went wrong..  Bail.
                System.out.println("deletReview: failed to delete the review in the database!");
                statement.close();
                return false;
            }

            else
            {
                success = true;
            }
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("deleteReview: error deleting our review from the database!");
            return false;
        }

        return success;
    }//END OF DELETE REVIEW


    /*
    |   +getStudentReviewAverage()
    |
    |   Returns the average number of stars that a student has gotten
    |   parameters: takes studentId
    |   Called from BreakDownController class.
    */
    public double getStudentReviewAverage(int studentId)
    {
        double averageStars = -1.0;

        // Check user credentials
        if(!checkCredentials())
        {
            return averageStars;
        }

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT avg(numberOfStars) " +
                    "FROM Reviews WHERE writtenAboutId = ?";

            // Get the prepared statement so we can escape argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                averageStars = result.getDouble(1);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getReviews: error getting our review from the database!");
            return averageStars;
        }

        return averageStars;
    }// END OF GET STUDENT REVIEW AVERAGE


    /*
    |   checkLogin()
    |
    |   returns true if login succeeded and false if it failed.
    */
    boolean checkLogin()
    {
        // Check that the credential passed match database
        boolean result  = this.checkCredentials();

        if(this.userId == -1 || !result)
        {
            return false;
        }

        else
        {
            return true;
        }
    }// END OF CHECK LOGIN


    /*
    |   -getSalt()
    |
    |   Gets the salt from the database.
    |   return null on failure
    */
    private String getSalt()
    {
        String salt;

        try
        {
            String table;

            if(this.isStudent)
            {
                table = "Students";
            }

            else
            {
                table = "Instructors";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "SELECT salt FROM " + table + " WHERE email is ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);


            // Escape the user name passed in
            statement.setString(1, this.username);

            // Execute the query to get the salt
            ResultSet result = statement.executeQuery();

            boolean isRow = result.next();

            if(isRow)
            {
                salt = result.getString("salt");
            }

            else
            {
                // Not there.  Bail.
                System.out.println("getSalt: failed to get the salt from the database!");
                salt =  null;
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getSalt: error getting our salt from the database!");
            return null;
        }

        return salt;
    }//END OF GET SALT


    /*
    |   hashPassword()
    |
    |   Hashes the password
    |   Returns hashed password or null on failure
    */
    String hashPassword(String password, String salt)
    {
        String hashedPassword = null;

        // Check for null salt
        if(salt == null || password == null)
        {
            // No can do.
            return null;
        }

        // Thanks to https://www.baeldung.com/java-password-hashing
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);

        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hashedPassword = Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        }

        catch (Exception e)
        {
            System.out.println("Error hashing password!");
            return null;
        }

        return hashedPassword;
    }//END OF HASH PASSWORD


    /*
    |   +checkCredentials()
    |
    |   Checks whether the username password combination matches either a student or a professor's credentials.
    |   Sets the userId to -1 on fail or the studentId or instructorId on success.
    |   Returns true on success and false on failure.
    */
    public boolean checkCredentials()
    {
        //assume we fail
        this.userId = -1;

        boolean succeeded = false;

        // This may be the first time called - logging in.
        if(this.hashedPassword == null)
        {
            this.hashedPassword = this.hashPassword(this.password, getSalt());
        }

        if(this.hashedPassword == null)
        {
            // Has failed.
            return false;
        }

        try
        {
            String table;
            String id;

            if(this.isStudent)
            {
                table = "Students";
                id = "studentId";
            }

            else
            {
                table = "Instructors";
                id = "instructorId";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "SELECT " + id + " FROM " + table + " WHERE email = ? AND password  = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);
            statement.setString(1, this.username);
            statement.setString(2, this.hashedPassword);
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                this.userId =  result.getInt(id);
                succeeded = true;
            }

            else
            {
                succeeded = false;
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            return false;
        }

        return succeeded;
    }//END OF CHECK CREDENTIALS


    /*
    |   -createTables()
    |
    |   Creates the default table structure if none exits.
    |   Returns 0 on success.
    */
    private int createTables()
    {
        int error = 0;

        try
        {
            Statement statement = this.connection.createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS Instructors " +
                    "(instructorId INTEGER  PRIMARY KEY AUTOINCREMENT, name VARCHAR(128), " +
                    "email TEXT UNIQUE, " +
                    "address VARCHAR(128), city VARCHAR(64), province VARCHAR(32)," +
                    "postalCode VARCHAR(32), " +
                    "department VARCHAR(128), password TEXT, salt TEXT)");

            String salt = getNewSalt();
            String password =  this.hashPassword("verySecret", salt);

            // Create our root user - has to be some instructor there!
            statement.execute("INSERT into Instructors (name, email, password, salt) VALUES ('root', 'root', '"+
                    password + "', '" + salt + "')");

            statement.execute("CREATE TABLE IF NOT EXISTS Courses " +
                    "(courseCode VARCHAR(32), CourseName VARCHAR(128), " +
                    "description VARCHAR(128), numberOfCredits REAL, PRIMARY KEY(courseCode))");

            statement.execute("CREATE TABLE IF NOT EXISTS Prerequisites " +
                    "(courseCode VARCHAR(32), prerequisiteCode VARCHAR(32), " +
                    "PRIMARY KEY(courseCode, prerequisiteCode))");

            statement.execute("CREATE TABLE IF NOT EXISTS Sections " +
                    "(sectionId INTEGER  PRIMARY KEY AUTOINCREMENT, semester TEXT, " +
                    "room VARCHAR(32), meetingTimes VARCHAR(64), instructorId INT, " +
                    "courseCode INT, " +
                    "FOREIGN KEY(instructorId) REFERENCES instructors(instructorId) " +
                    "FOREIGN KEY(courseCode) REFERENCES Courses(courseCode) " +
                    "ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS Students " +
                    "(studentId INTEGER  PRIMARY KEY AUTOINCREMENT, name VARCHAR(128), " +
                    "email TEXT UNIQUE, " +
                    "address VARCHAR(128), city VARCHAR(64), province VARCHAR(32)," +
                    "postalCode VARCHAR(32), password TEXT, salt TEXT)");

            statement.execute("CREATE TABLE IF NOT EXISTS Enrollments " +
                    "(enrollmentId INTEGER  PRIMARY KEY AUTOINCREMENT, " +
                    "studentId INT, " +
                    "sectionId INT, " +
                    "FOREIGN KEY(studentId) REFERENCES Students(studentId)" +
                    "FOREIGN KEY(sectionId) REFERENCES Sections(sectionId) " +
                    "ON DELETE CASCADE)");

            statement.execute("CREATE TABLE IF NOT EXISTS Assignments " +
                    "(assignmentId INTEGER  PRIMARY KEY AUTOINCREMENT, name TEXT, " +
                    "description TEXT, dueDate TEXT, sectionId INTEGER, " +
                    "FOREIGN KEY(sectionId) REFERENCES Sections(sectionId) " +
                    "ON DELETE CASCADE)");


            statement.execute("CREATE TABLE IF NOT EXISTS Groups " +
                    "(assignmentId INTEGER, studentId INTEGER, " +
                    "groupNumber INTEGER, " +
                    "FOREIGN KEY(assignmentId) REFERENCES Assignments(assignmentId) " +
                    "FOREIGN KEY(studentId) REFERENCES Students(studentId) " +
                    "ON DELETE CASCADE) ");

            statement.execute("CREATE TABLE IF NOT EXISTS Reviews " +
                    "(reviewId INTEGER  PRIMARY KEY AUTOINCREMENT, reviewText TEXT, " +
                    "writtenById INTEGER, writtenAboutId INTEGER, " +
                    "numberOfStars INTEGER, assignmentId INTEGER, " +
                    "FOREIGN KEY(assignmentId) REFERENCES Assignments(assignmentId) " +
                    "FOREIGN KEY(writtenById) REFERENCES Students(studentId) " +
                    "FOREIGN KEY(writtenAboutId) REFERENCES Students(studentId) " +
                    "ON DELETE CASCADE)");

            statement.close();
        }

        catch(SQLException e)
        {
            // Don't log error. probably already done.
        }

        return error;
    }// END OF CREATE TABLES


    /*
    |   +createTestUser
    |
    |   Called by populateTestData to fill database with some test data
    */
    public boolean createTestUser(String name, String email, String password, boolean isStudent)
    {
        boolean succeeded;

        String salt = getNewSalt();
        String hashedPassword = this.hashPassword(password, salt);
        try
        {
            String table;
            if(isStudent)
            {
                table = "Students";
            }

            else
            {
                table = "Instructors";
            }

            // Make our query for the prepared statement - get the salt for our password
            String query = "INSERT INTO " + table + " (name, email, password, salt) VALUES (?, ?, ?, ?)";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);
            statement.setString(4, salt);

            int affectedRows = statement.executeUpdate();

            if(affectedRows == 1)
            {
                succeeded = true;
            }

            else
            {
                // Already there?
                succeeded = false;
            }

            statement.close();
        }

        catch(SQLException e)
        {
            return false;
        }

        return succeeded;
    }//END OF CREATE TEST USER


    /*
    |   +populateTestData()
    |
    |   This method was used for testing purposes
    |   Created users (student and instructor)
    |   Created courses
    |   Deleted courses
    */
    public boolean populateTestData()
    {
        // Check user credentials
        if(this.isStudent || !this.checkCredentials())
        {
            return false;
        }

        boolean succeeded = true;

        // Create an instructor
        String instructorName = "Alaadin Addas";
        String email = "aaddas@trentu.ca";
        String password = "trickypassword";

        this.createTestUser(instructorName, email, password, false);

        this.createTestUser("Joe Smith", "joesmith@email.com", "password", true);

        // Delete the course to start fresh
        deleteCourse("COIS2240");

        // Create a course
        String description = "Good software design and modelling is a necessary prerequisite for the production of " +
                "software which is correct, robust, and maintainable. Using the standard Unified Modeling Language " +
                "(UML) to specify design, core topics include use cases; classes and class membership; aggregation, " +
                "composition, and inheritance; virtual functions and polymorphism; state diagrams; and design " +
                "patterns. Prerequisite: COIS 1020H or both COIS 1520H and COIS-ADMN 2620H. ";
        succeeded = createCourse("COIS2240", "Software Design and Modelling", description, .5 );

        // Create a section of the course
        int sectionId = -1;
        int assignmentId = -1;

        // Because SQLite sucks so bad, we can't set proper constraints.  So, we guard here.
        if(succeeded)
        {
            sectionId = createSection("Winter 2019", "Room 118", "Monday 6-9",
                    "COIS2240");

            if(sectionId == -1)
            {
                succeeded = false;
            }

            // Create an assignment
            assignmentId =  createAssignment("Final Project", "This is going to be really " +
                    "hard to do!", "April 5th 2019", sectionId);

            if(assignmentId == -1)
            {
                succeeded = false;
            }

        }

        // Create two users who can review each other
        String student1 = "Roy Orbison";
        String email1 = "roy@royorbison.com";
        String password1 = "mysterygirl";

        succeeded = this.createTestUser(student1, email1, password1, true);

        int studentId1 = getStudentIdFromEmail(email1);

        if(succeeded)
        {
            // Add this user to a section of the course we created
            succeeded &= addToSection(studentId1, sectionId);
        }

        String student2 = "Buddy Holly";
        String email2 = "buddy@buddyholly.com";
        String password2 = "crickets";

        succeeded = this.createTestUser(student2, email2, password2, true);
        int studentId2 = getStudentIdFromEmail(email2);

        // Guard against doing this more than once because composite keys don't work.
        if(succeeded)
        {
            // Add this user to a section of the course we created
            succeeded = addToSection(studentId2, sectionId);

            // Assign the users to the same group
            succeeded = assignToGroup(studentId1, assignmentId, 1);
            succeeded = assignToGroup(studentId2, assignmentId, 1);
        }

        return succeeded;
    }//END OF POPULATE TEST DATA


    /*
    |   +getDatabaseName()
    |
    |   For test suite
    */
    public String getDatabaseName()
    {
        return this.databaseName;
    }//END OF GET DATABASE NAME


    /*
    |   +getStudentName()
    |
    |   Used to pull a student name from the database
    |   parameters: studentID
    |   Returns studentName
    */
    public String getStudentName(int studentID)
    {
        String studentName = "";

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT name FROM students WHERE studentId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentID);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if (result.next())
            {
                studentName = result.getString("name");
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            // This is very bad indeed. But do nothing. Null will be returned.
        }

        return studentName;
    }// END OF GET STUDENT NAME


    /*
    |   +getStudentSemesters()
    |
    |   Used to get a list of the semesters the student has classes during
    |   parameters: studentID
    |   Returns a map of semesters
    */
    public ArrayList<Map<String, String>> getStudentSemesters(int studentId)
    {
        // Check user credentials
        if(!this.checkCredentials() || (this.isStudent && this.userId != studentId))
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> semesters = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT Sections.sectionId, semester FROM Sections " +
                    "INNER JOIN Enrollments ON Sections.sectionId = Enrollments.sectionId " +
                    "WHERE Enrollments.studentId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> semester = new HashMap<>();

                // Use a string for the map.
                semester.put("sectionId",  result.getString("sectionId"));
                semester.put("semester",  result.getString("semester"));
                semesters.add(semester);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getStudentSemester: error getting our courses from the database!");
            return null;
        }

        return semesters;
    }//END OF GET STUDENT SEMESTERS


    /*
    |   +getGroupsByAssignment()
    |
    |   Used to get a list of the groups for a specific assignment
    |   parameters: assignmentId
    |   Returns a list of groups
    */
    public ArrayList<Integer> getGroupsByAssignment(int assignmentId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Integer> groups = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT groupNumber FROM groups " +
                    "WHERE assignmentId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                groups.add(result.getInt("groupNumber"));
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getGroupsByAssignment: error getting the groups from the database!");
            return null;
        }

        return groups;
    }//END OF GET GROUPS BY ASSIGNMENT


    /*
    |   +checkForGroup()
    |
    |   Used to check if a user is in a group for the assignment already
    |   parameters: studentId, assignmentId
    |   Returns a boolean value (true if in a group; false if not)
    */
    public boolean checkForGroup(int studentId, int assignmentId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return false;
        }

        boolean inGroup;

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT groupNumber FROM groups " +
                    "WHERE studentId = ? and assignmentId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);
            statement.setInt(2, assignmentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            if(result.next())
            {
                inGroup =  true;
            }

            else
            {
                inGroup = false;
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("checkForGroup: error getting the groups from the database!");
            return false;
        }

        return inGroup;
    }// END OF CHECK FOR GROUP


    /*
    |   +getAllStudentCourses
    |
    |   Used to get a list of all courses the student is enrolled in
    |   parameters: studentId
    |   Returns a map with all courses
    */
    public ArrayList<Map<String, String>> getAllStudentCourses(int studentId)
    {
        // Check user credentials
        if(!this.checkCredentials())
        {
            // No can do
            return null;
        }

        ArrayList<Map<String, String>> allCourses = new ArrayList<>();

        try
        {
            // Make our query for the prepared statement
            String query = "SELECT Sections.courseCode, semester, Sections.sectionId FROM Sections " +
                    "INNER JOIN Enrollments ON Sections.sectionId = Enrollments.sectionId " +
                    "WHERE Enrollments.studentId = ?";

            // Get the prepared statement so we can escape the email argument
            PreparedStatement statement = this.connection.prepareStatement(query);

            // Escape the parameters
            statement.setInt(1, studentId);

            // Execute the query to get the data
            ResultSet result = statement.executeQuery();

            while(result.next())
            {
                Map<String, String> allCourse = new HashMap<>();

                // Use a string for the map.
                allCourse.put("courseCode",  result.getString("courseCode"));
                allCourse.put("semester",  result.getString("semester"));
                allCourse.put("sectionId",  result.getString("sectionId"));
                allCourses.add(allCourse);
            }

            result.close();
            statement.close();
        }

        catch(SQLException e)
        {
            System.out.println("getAllStudentCourses: error getting our courses from the database!");
            return null;
        }

        return allCourses;
    }// END OF GET ALL STUDENT COURSES


    /*
    |   +getUserId()
    |
    |   Simply returns the userId of the current user logged in
    */
    public int getUserId()
    {
        return userId;
    }//END OF GET USER ID

}//END OF DATABASE CLASS
