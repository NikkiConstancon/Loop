package com.zetta.android;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check email validation
     * @throws Exception
     */
    @Test
    public void email_check() throws Exception{
        String[] emails = {"John", "    ", "12345", "john@gmail.com", "#@dd4f", "Carl@", "Segnor.gmail", "@gmail.com", ".com", ".comgmail@"};

        for(int i = 0; i < 10; i++)
        {
            if(!check_email_valid(emails[i]))
            {
                throw new Exception("Email is not valid");
            }
        }
    }

    /**
     * Function that simulates email validation of app
     * @param email is the email string being tested
     */
    public boolean check_email_valid(String email)
    {
        Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,63}$");
        Matcher m = p.matcher(email);

        if(email.length() < 1 || !m.find())
        {
            return false;
        }

        return true;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check password validation
     * @throws Exception
     */
    @Test
    public void password_check() throws Exception{
        String[] pass = {"", "    ", "12345", "helloworld123", "Th4R", "#@!$%^", "thisislong", "ThisSh0uldWork", "THISSHOULDNOT", "-1EchoAlpha"};
        String[] conf = {"", "    ", "12345", "helloworld123", "Th4R", "#@!$%^", "thisislong", "butitwont", "THISSHOULDNOT", "-1EchoAlpha"};

        for(int i = 0; i < 10; i++)
        {
            if(!check_password_valid(pass[i], conf[i]))
            {
                throw new Exception("Password is not valid");
            }
        }
    }

    /**
     * Function that simulates password validation of app
     * @param pass password to be tested
     * @param conf password confirmation variable
     * @return
     */
    public boolean check_password_valid(String pass, String conf)
    {
        Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
        Matcher m2 = p2.matcher(pass);

        if(pass.length() < 6 || !m2.find() || !pass.equals(conf))
        {
            return false;
        }

        return true;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check username validation
     * @throws Exception
     */
    @Test
    public void check_username() throws Exception
    {
        String[] existing = {"carl123", "bob6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mick", "John", "1Address", "Bob"};
        String[] tested = {"carl13", "bb6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mik", "", "1Address", "Bo"};

        for(int i = 0; i < existing.length; i++)
        {
            for(int j = 0; j < tested.length; j++)
            {
                if(tested[j].length() < 1 || !existing[i].equals(tested[j]))
                {
                    throw new Exception("Username is not valid");
                }
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check age validation
     * @throws Exception
     */
    @Test
    public void check_age() throws Exception
    {
        String[] ages = {"-3", "0", "6", "23", "", "150", "    ", "12", "-4", "-5"};

        for(int i = 0; i < ages.length; i++)
        {
            if(!age_check_valid(ages[i]))
            {
                throw new Exception("Age is not valid");
            }
        }
    }

    /**
     * Function simulating age validation of app
     * @param age value to be tested
     * @return
     */
    public boolean age_check_valid(String age)
    {
        int num = -1;
        boolean success = false;

        try
        {
            num = Integer.parseInt(age);
        }
        catch(Exception ex)
        {
            success = false;
        }

        if(success == false)
        {
            return false;
        }
        else
        {
            if(num < 1 || num > 120)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check weight validation
     * @throws Exception
     */
    @Test
    public void check_weight() throws Exception
    {
        String[] weights = {"-3.0", "0", "6", "23", "", "150", "    ", "675", "-4", "-5"};

        for(int i = 0; i < weights.length; i++)
        {
            if(!weight_check_valid(weights[i]))
            {
                throw new Exception("Weight is not valid");
            }
        }
    }

    /**
     * Function simulating weight validation of app
     * @param weight value to be tested
     * @return
     */
    public boolean weight_check_valid(String weight)
    {
        Double num = -1.0;
        boolean success = false;

        try
        {
            num = Double.parseDouble(weight);
        }
        catch(Exception ex)
        {
            success = false;
        }

        if(success == false)
        {
            return false;
        }
        else
        {
            if(num < 1 || num > 450)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check height validation
     * @throws Exception
     */
    @Test
    public void check_height() throws Exception
    {
        String[] heights = {"-3.0", "0", "6", "23", "", "150", "    ", "12", "-4", "-5"};

        for(int i = 0; i < heights.length; i++)
        {
            if(!height_check_valid(heights[i]))
            {
                throw new Exception("Height is not valid");
            }
        }
    }

    /**
     * Function simulating weight validation of app
     * @param height value to be tested
     * @return
     */
    public boolean height_check_valid(String height)
    {
        Double num = -1.0;
        boolean success = false;

        try
        {
            num = Double.parseDouble(height);
        }
        catch(Exception ex)
        {
            success = false;
        }

        if(success == false)
        {
            return false;
        }
        else
        {
            if(num < 1 || num > 2.8)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check subscribed to usernames validation
     * @throws Exception
     */
    @Test
    public void check_subs_to_username() throws Exception
    {
        String[] existing = {"carl123", "bob6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mick", "John", "1Address", "Bob"};
        String[] tested = {"carl13", "bb6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mik", "", "1Address", "Bo"};

        for(int i = 0; i < existing.length; i++)
        {
            for(int j = 0; j < tested.length; j++)
            {
                if(tested[j].length() < 1 || !existing[i].equals(tested[j]))
                {
                    throw new Exception("Subscribed to username is not valid");
                }
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Test case to check subscribed to password validation
     * @throws Exception
     */
    @Test
    public void sub_password_check() throws Exception{
        String[] passExisting = {"Carl123", "ThisWorks12", "LongAndG00d", "helloWorld123", "Th4Reee", "Valhalla1", "thisislonG23", "ThisSh0uldWork", "THISSHOULDt00", "NiceStuff123"};
        String[] pass = {"", "    ", "LongAndG00d", "helloworld123", "Th3Reee", "valhalla1", "THISISLONG23", "ThisSh0uldWork", "thisshouldtoo", "BadStuff23"};

        for(int i = 0; i < 10; i++)
        {
            if(!check_sub_password_valid(passExisting[i], pass[i]))
            {
                throw new Exception("Subscribed to password is not valid");
            }
        }
    }

    /**
     * Function that simulates subscribed to password validation of app
     * @param pass existing password
     * @param checkPass password to be checked
     * @return
     */
    public boolean check_sub_password_valid(String pass, String checkPass)
    {
        Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
        Matcher m2 = p2.matcher(checkPass);

        if(checkPass.length() < 6 || !m2.find() || !pass.equals(checkPass))
        {
            return false;
        }

        return true;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}
