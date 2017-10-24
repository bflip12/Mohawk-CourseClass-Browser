package ca.mohawk.bobbyfilippopoulos.project2;

/**
 * Created by BobbyFilippopoulos on 2017-04-15.
 */
//the course object that is used for reading and writing from the database
public class Course {
    public String _id;
    public String program;
    public String semesterNum;
    public String courseCode;
    public String courseTitle;
    public String courseDescription;
    public String courseOwner;
    public String optional;
    public String hours;

    @Override
    public String toString()
    {
        return program;
    }
}
