import java.util.Scanner;
import java.io.*;

public class Wordgame {
    public static void main(String[] agrs) throws IOException {
        Scanner in = new Scanner(new File("word_answer.txt"));
        Scanner sc = new Scanner(System.in);
        PrintStream out = new PrintStream(new File("result.txt"));
        String Name = "";

        System.out.println("=== Word Journey ===");

        int correctcount = 0;
        int totalcount = 0;

        System.out.print("Name :");
        Name = sc.nextLine();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] data = line.split("\\|");

            if (data.length == 3) {
                String question = data[0];
                String hint = data[1];
                String answer = data[2].trim(); // ✅ ตัดช่องว่างออก

                totalcount++;

                System.out.println("\nQuestion " + totalcount + ": " + question);
                System.out.println("Hint: " + hint);
                System.out.println("You have 20 seconds to answer");

                final String[] userAnswer = new String[1];
                userAnswer[0] = null;

                Thread inputThread = new Thread(() -> {
                    System.out.print("Your answer: ");
                    userAnswer[0] = sc.nextLine().trim(); // ✅ ตัดช่องว่างออก
                });

                inputThread.start();
                long Starttime = System.currentTimeMillis();

                for (int i = 0; i < 200; i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long elapsed = System.currentTimeMillis() - Starttime;
                    if (userAnswer[0] != null || elapsed >= 20000) { // ✅ แก้เป็น 20 วินาที
                        break;
                    }
                }

                if (userAnswer[0] == null) {
                    System.out.println("\n⏰ Time's up!");
                    inputThread.interrupt();
                } else {
                    long Endtime = System.currentTimeMillis();
                    long timeUsed = (Endtime - Starttime) / 1000;

                    // ✅ รองรับพิมพ์เล็ก/ใหญ่
                    if (userAnswer[0].trim().equalsIgnoreCase(answer.trim())) {
                        System.out.println("✅ Correct! (" + timeUsed + " sec)");
                        correctcount++;
                    } else {
                        System.out.println("❌ Wrong! The correct answer is: " + answer);
                    }
                }
            }
        }

        in.close();
        sc.close();
        out.close();

        System.out.println("\n=== Game Over ===");
        System.out.println("Score: " + correctcount + "/" + totalcount);
        out.println(Name+" "+correctcount);
    }
}
