import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Brian on 8/5/2015.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(new File("1.jpg"));
        String messageToSend = "Testing to see if this really works!";


        BufferedImage outputImage = streganophy(bufferedImage, messageToSend);
        ImageIO.write(outputImage, "png", new File("output.png"));

        BufferedImage toDecode = ImageIO.read(new File("output.png"));
        String outputMessage = deStreganophy(toDecode);
        System.out.println(outputMessage);

    }

    public static BufferedImage streganophy(BufferedImage input, String message) {
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

    public static String deStreganophy(BufferedImage input) {
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
