package cam;


import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.util.Console;

/**
 * This example code demonstrates how to perform simple I2C
 * communication with the CMUcam5 or PixyCam.
 *
 * http://cmucam.org/projects/cmucam5/wiki/Pixy_Regular_Quick_Start
 *
 * Based on the port guide:
 * http://www.cmucam.org/projects/cmucam5/wiki/Porting_Guide#Writing-the-code 
 *
 * @author Caleb Hyde
 */
public class PixyCam {

    static int getWord(I2CDevice device) throws IOException {
        int c = device.read();
        int w = device.read();
        w = w << 8;
        w = w | c; 
        return w;
    }

    public static final byte PIXY_START_WORD = (byte)0xaa55;
    public static final byte PIXY_START_WORD_CC = (byte)0xaa56;
    public static final byte PIXY_START_WORDX = (byte)0x55aa;
    public static final byte PIXY_ADDR = (byte)0x54;
    
    private static String g_blockType = "";
    private static String NORMAL_BLOCK = "normal";
    private static String CC_BLOCK = "cc";
    
    static int getStart(I2CDevice device) throws IOException {

        int w, lastw;

        lastw = 0xffff; // some inconsequential initial value

        while(true)
        {
            w = getWord(device);
            if (w==0 && lastw==0)
            return 0; // in I2C and SPI modes this means no data, so return immediately
            else if (w==PIXY_START_WORD && lastw==PIXY_START_WORD)
            {
            g_blockType = NORMAL_BLOCK; // remember block type
            return 1; // code found!
            }
            else if (w==PIXY_START_WORD_CC && lastw==PIXY_START_WORD)
            {
            g_blockType = CC_BLOCK; // found color code block
            return 1;
            }    
            else if (w==PIXY_START_WORDX) // this is important, we might be juxtaposed 
            device.read(); // we're out of sync! (backwards)
            lastw = w; // save
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException {

        // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        final Console console = new Console();

        // print program title/header
        // console.title("Pi4J PixyCam demo");

        // allow for user to exit program using CTRL-C
        // console.promptForExit();

        // get the I2C bus to communicate on
        I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_0);

        // create an I2C device for an individual device on the bus that you want to communicate with
        // in this example we will use the default address for the TSL2561 chip which is 0x39.
        I2CDevice device = i2c.getDevice(PIXY_ADDR);

                
        int i=0, curr, prev=0;

        // look for two start codes back to back
        while(true)
            {
                curr = getStart(device);
                if (prev != 0 && curr != 0) {
                console.println("%d", i++);
                prev = curr;
            }
        }
    }
}