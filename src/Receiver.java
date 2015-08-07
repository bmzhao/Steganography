import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Brian on 8/6/2015.
 */
public class Receiver {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(7777);
        Socket connection = serverSocket.accept();
        System.out.println("Connected!");
        File output = new File("TestMessage.png");
        BufferedOutputStream writeToFile = new BufferedOutputStream(new FileOutputStream(output));
        InputStream is = connection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] aByte = new byte[1];
        int numBytesRead = is.read(aByte, 0, aByte.length);
        do {
            baos.write(aByte);
            numBytesRead = is.read(aByte);
        } while (numBytesRead!=-1);
        writeToFile.write(baos.toByteArray());
        writeToFile.flush();
        writeToFile.close();
        connection.close();

        BufferedImage toDecode = ImageIO.read(new File("TestMessage.png"));
        String outputMessage = deSteganophy(toDecode);
        System.out.println(outputMessage);
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
