import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Created by Brian on 8/5/2015.
 */
public class Sender {
    public static void main(String[] args) throws Exception {

        BufferedImage bufferedImage = ImageIO.read(new File("input.jpg"));
        String messageToSend = "Hello Cary! Check out this amazing hidden message!\n";
//                "A Priest was being honoured at his retirement dinner after 25 years in the parish." +
//                "\n A leading local politician and member of the congregation was chosen to make the " +
//                "\npresentation and to give a little speech at the dinner. However, he was delayed, so" +
//                "\n the Priest decided to say his own few words while they waited: Thank Goodness we \n" +
//                "Catholics have a wonderful sense of humour!\n" +
//                "\"I got my first impression of the parish from the first confession I heard here. " +
//                "\nI thought I had been assigned to a terrible place. The very first person who " +
//                "\nentered my confessional told me he had stolen a television set and, when questioned\n " +
//                "by the police, was able to lie his way out of it. He had stolen money from his parents; \n" +
//                "embezzled from his employer; had an affair with his boss’s wife; had sex with his boss’s \n" +
//                "17 year old daughter on numerous occasions, taken illegal drugs; had several homosexual affairs; \n" +
//                "was arrested several times for public nudity and gave VD to his sister in-law.\n" +
//                "I was appalled that one person could do so many awful things. But as the days went on, \n" +
//                "I learned that my people were not all like that and I had, indeed, come to a fine parish \n" +
//                "full of good and loving people.\"\n" +
//                "Just as the Priest finished his talk, the politician arrived full of apologies at being late. \n" +
//                "He immediately began to make the presentation and gave his talk:\n" +
//                "\"I'll never forget the first day our parish Priest arrived,\" said the politician. \"In fact, \n" +
//                "I had the honour of being the first person to go to him for confession.\"";


        BufferedImage outputImage = steganophy(bufferedImage, messageToSend);
        ImageIO.write(outputImage, "png", new File("output.png"));


        Socket connection = new Socket("192.168.2.17", 7777);
        System.out.println("Connected to PC");
        File toSend = new File("output.png");
        byte[] byteArray = new byte[(int) toSend.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(toSend));
        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
        bis.read(byteArray, 0, byteArray.length);
        bos.write(byteArray, 0, byteArray.length);
        bos.flush();
        bos.close();
        connection.close();
        System.out.println("Sent File!");
        System.exit(0);



//        BufferedImage toDecode = ImageIO.read(new File("output.png"));
//        String outputMessage = deSteganophy(toDecode);
//        System.out.println(outputMessage);

    }

    public static BufferedImage steganophy(BufferedImage input, String message) {
        byte[] messageArray = message.getBytes();
        int length = messageArray.length;
        if (length > 255) {
            throw new RuntimeException("Message is too long!");
        }

        int totalCapacity = input.getWidth() * input.getHeight();
        if (totalCapacity < length + 1) {
            throw new RuntimeException("Image too small for message");
        }

        BufferedImage outputImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int count = 0;
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                int originalRgbValue = input.getRGB(i, j);
                int rgbValue = originalRgbValue & 0b11111100_11111100_11111100_11111100;
                if (count == 0) {
                    int top2Bits = length & 0b00000000_00000000_00000000_11000000;
                    top2Bits = top2Bits << 18;
                    int upperMiddle2Bits = length & 0b00000000_00000000_00000000_00110000;
                    upperMiddle2Bits = upperMiddle2Bits << 12;
                    int lowerMiddle2Bits = length & 0b00000000_00000000_00000000_00001100;
                    lowerMiddle2Bits = lowerMiddle2Bits << 6;
                    int bottom2Bits = length & 0b00000000_00000000_00000000_00000011;
                    outputImage.setRGB(i, j, rgbValue | top2Bits | upperMiddle2Bits | lowerMiddle2Bits | bottom2Bits);
                } else if (count < length + 1) {
                    int top2Bits = messageArray[count - 1] & 0b00000000_00000000_00000000_11000000;
                    top2Bits = top2Bits << 18;
                    int upperMiddle2Bits = messageArray[count - 1] & 0b00000000_00000000_00000000_00110000;
                    upperMiddle2Bits = upperMiddle2Bits << 12;
                    int lowerMiddle2Bits = messageArray[count - 1] & 0b00000000_00000000_00000000_00001100;
                    lowerMiddle2Bits = lowerMiddle2Bits << 6;
                    int bottom2Bits = messageArray[count - 1] & 0b00000000_00000000_00000000_00000011;
                    outputImage.setRGB(i, j, rgbValue | top2Bits | upperMiddle2Bits | lowerMiddle2Bits | bottom2Bits);
                } else {
                    outputImage.setRGB(i, j, originalRgbValue);
                }
                count++;

            }
        }
        return outputImage;
    }

    public static String deSteganophy(BufferedImage input) {
        int firstPixel = input.getRGB(0, 0);
        int top2Bits = firstPixel & 0b00000011_00000000_00000000_00000000;
        top2Bits = top2Bits >> 18;
        int upperMiddle2Bits = firstPixel & 0b00000000_00000011_00000000_00000000;
        upperMiddle2Bits = upperMiddle2Bits >> 12;
        int lowerMiddle2Bits = firstPixel & 0b00000000_00000000_00000011_00000000;
        lowerMiddle2Bits = lowerMiddle2Bits >> 6;
        int bottom2Bits = firstPixel & 0b00000000_00000000_00000000_00000011;
        int length = top2Bits | upperMiddle2Bits | lowerMiddle2Bits | bottom2Bits;
        int count = 0;

        byte[] arrayBytes = new byte[length];
        boolean toBreak = false;
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                int originalRgbValue = input.getRGB(i, j);
                if (i == 0 && j == 0) {
                    continue;
                } else {
                    if (count < length) {
                        int top2Bits1 = originalRgbValue & 0b00000011_00000000_00000000_00000000;
                        top2Bits1 = top2Bits1 >> 18;
                        int upperMiddle2Bits1 = originalRgbValue & 0b00000000_00000011_00000000_00000000;
                        upperMiddle2Bits1 = upperMiddle2Bits1 >> 12;
                        int lowerMiddle2Bits1 = originalRgbValue & 0b00000000_00000000_00000011_00000000;
                        lowerMiddle2Bits1 = lowerMiddle2Bits1 >> 6;
                        int bottom2Bits1 = originalRgbValue & 0b00000000_00000000_00000000_00000011;
                        byte answer = (byte) (top2Bits1 | upperMiddle2Bits1 | lowerMiddle2Bits1 | bottom2Bits1);
                        arrayBytes[count] = answer;
                    } else {
                        toBreak = true;
                        break;
                    }
                    count++;
                }
            }
            if (toBreak) {
                break;
            }
        }

        return new String(arrayBytes);
    }
}
