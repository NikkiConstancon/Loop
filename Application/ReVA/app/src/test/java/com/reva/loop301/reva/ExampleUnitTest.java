package com.reva.loop301.reva;

import android.os.Bundle;
import android.view.View;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void verificationTestLoginDetails() throws Exception
    {
        String[] emails = {"Bob@gmail.com", "Carl@yahoo.co.za", "lisa.com", "mina@hotmail", "tanith", "eric123@yknot.com", "trish@mail.c1", ".co.za@charles", "@.co.za", "co"};
        String[] passwords = {"123", "hello", "youaretheone", "IamGr00t", "1hiiamatest", "IshouldW0rk", "1ishouldnot", "ISHOULDNOTWORK2", "ThisIs123", "       "};

        for(int i = 0; i < emails.length; i++)
        {
            verificationTestLoginDetails(emails[i], passwords[i]);
        }
    }

    public void verificationTestLoginDetails(String email, String password) throws Exception
    {
        String mail = email, pass = password;

        if(email.length() < 1 || password.length() < 6)
        {
            throw new Exception();
        }
        else if(!email.contains("@") || (!password.contains("1") && !password.contains("2") && !password.contains("3") && !password.contains("4") && !password.contains("5")
                && !password.contains("6") && !password.contains("7") && !password.contains("8") && !password.contains("9") && !password.contains("0")))
        {
            throw new Exception();
        }
        else if(!email.contains(".co.za") && !email.contains(".com") || password.toLowerCase().equals(password) || password.toUpperCase().equals(password))
        {
            throw new Exception();
        }
    }

    @Test
    public void verificationTestRegPatient1() throws Exception
    {
        String[] emails = {"Bob@gmail.com", "Carl@yahoo.co.za", "lisa.com", "mina@hotmail", "tanith", "eric123@yknot.com", "trish@mail.c1", ".co.za@charles", "@.co.za", "co"};
        String[] passwords = {"123", "hello", "youaretheone", "IamGr00t", "1hiiamatest", "IshouldW0rk", "1ishouldnot", "ISHOULDNOTWORK2", "ThisIs123", "       "};
        String[] confirmPass = {"123", "hello", "youaretheone", "IamGr00t", "1hiiamatest", "IshouldW0k", "1ishouldnot", "ISHOULDNOTWORK2", "ThisIs124", "      "};

        for(int i = 0; i < 10; i++)
        {
            verificationTestLoginDetails(emails[i], passwords[i]);

            if(!passwords[i].equals(confirmPass[i]))
            {
                throw new Exception();
            }
        }
    }

    @Test
    public void pickUserNameTest() throws Exception
    {
        String[] existing = {"carl123", "bob6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mick", "John", "1Address", "Bob"};
        String[] tested = {"carl13", "bb6", "Juan du Preez", "Jamie Bob", "George", "Sally", "Mik", "John", "1Address", "Bo"};

        for(int i = 0; i < existing.length; i++)
        {
            for(int j = 0; j < tested.length; j++)
            {
                if(existing[i].equals(tested[j]))
                {
                    throw new Exception();
                }
            }
        }
    }

    @Test
    public void enterAgeTest() throws Exception
    {
        int[] ages = {-3, 0, 6, 23, 65, 150, 4, 12, -4, -5};

        for(int i = 0; i < ages.length; i++)
        {
            if(ages[i] < 0 || ages[i] > 140)
            {
                throw new Exception();
            }
        }
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}