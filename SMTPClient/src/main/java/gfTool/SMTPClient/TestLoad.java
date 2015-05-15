package gfTool.SMTPClient;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestLoad {

    private static String USER_NAME = "vithozh410@gmail.com";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "Thankyou410"; // GMail password
    private static String RECIPIENT = "vithozh410@mail.ru";

    public static void main(String[] args) {


        Boolean key = true;
        long i = 0;
        long globalIterations = 0;
        long duration;
        double tps = 0.0;
        double neededTps = 100.0;

        Date now = new Date();
        System.out.println(now);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Date globalEnd = null;
        Date globalBegin = null;
        try {
            globalEnd = df.parse("2015-05-15 12:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        while (now.getTime() < globalEnd.getTime() && key == true)
        {
            Date localEnd;
            Date localBegin;
            localEnd = globalEnd;
            now = new Date();


            if (i == 0){
                globalBegin = now;
                localBegin = now;
                duration = Math.abs(now.getTime() -  localBegin.getTime());
            }
            else{
                localBegin = globalBegin;
                duration = Math.abs(now.getTime() -  localBegin.getTime());
                if (duration/1000 > 60.0)
                {
                    globalBegin = now;
                    localBegin = globalBegin;
                    // there must be part which responsible for updating UI text field
                    duration = Math.abs(now.getTime() -  localBegin.getTime());
                    i = 0;
                }
                else {
                    tps = (i / (duration / 1000));
                    // there must be a part of code responsible for settin UI tps field
                }
                System.out.println(tps);
            }

            i++;
            globalIterations++;


            if (tps > neededTps)
            {
                Double temp = (i/neededTps - duration/1000)* 1000;
                long s = Long.parseLong(temp.toString());
                System.out.println("yep!");
                try {
                    Thread.sleep(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            try{

                // main action!

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        //i = 0;
        //testRunner.testCase.setPropertyValue('i', 0.toString());
        //if ((testRunner.testCase.getPropertyValue('tps')).toDouble() != 0.0);
         //   testRunner.testCase.setPropertyValue('totalTps', (((testRunner.testCase.getPropertyValue('tps')).toDouble())).toString());
        //testRunner.testCase.setPropertyValue('tps', 0.toString());

    }
}