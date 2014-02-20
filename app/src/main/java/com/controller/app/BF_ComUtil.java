/**
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED,INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.controller.app;

import android.bluetooth.BluetoothSocket;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * This communication protocol object enables Java application to communicate with
 * BlueFin devices. This API does not store any keys except the factory default
 * 256 bit AES key. Hence, the implementation of the key store is left to
 * the user of this API. The API requires developer to set the default key for
 * every instance of the BF_ComUtil using the method setDefaultAES() if the
 * default factory key had being changed.
 * @version 1.0a
 * @since 15/5/2011
 *
 */
public class BF_ComUtil {

    private String TAG = "BF_COM";
    private boolean isInit, isReady = false;
    private StringBuffer dataIn;
    private DataOutputStream out;
    private DataInputStream in;
    private Cipher cipher;
    private boolean isSecure = false;
    private byte[] serialData;
    // factory default master key
    public byte[] mKey = {0x41, 0x64, 0x76, 0x61, 0x6C, 0x6F, 0x67, 0x20,
        0x4B, 0x65, 0x65, 0x43, 0x68, 0x65, 0x77, 0x20,
        0x43, 0x6F, 0x6D, 0x70, 0x75, 0x53, 0x6F, 0x66,
        0x74, 0x20, 0x52, 0x6F, 0x6E, 0x6E, 0x69, 0x65};
    //private byte[] sessionKey;
    private FetchWorker worker;
    private SecretKeySpec seckey;
    byte[] bmpheader = {0x42, 0x4D, 0x36, 0x6C, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x36, 0x04, 0x00, 0x00, 0x28, 0x00,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x68, 0x01, 0x00, 0x00, 0x01, 0x00, 0x08, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x68, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /**
     * The communication utility object only required the bluetooth URL of the
     * BlueFin devices.
     * @param btConnectionURL the bluetooth URL of the device
     */
    public BF_ComUtil(BluetoothSocket socket) {

        dataIn = new StringBuffer();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            worker = new FetchWorker();
            Thread t = new Thread(worker);
            t.start();
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            //SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
            //seckey = factory.generateSecret(new SecretKeySpec(mKey, "AES"));
            seckey = new SecretKeySpec(mKey, "AES");

            isInit = true;
        } catch (Exception ioes) {
            Log.e(TAG, "BF exception", ioes);
            isInit = false;
        }
    }

    public boolean isIsInit() {
        return isInit;
    }

    private void writeData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        int hdrsum, sum, size;
        hdrsum = sum = size = 0;
        byte[] hdrData = new byte[6];
        byte[] data_enc;
        try {
            clearInput();
            //Cmd Size
            size = data.length;
            hdrData[0] = (byte) (size / 256);
            hdrData[1] = (byte) (size % 256);

            //encrypt data
            if (isSecure) {
                data_enc = performEncrypt(data);
            } else {
                data_enc = performEncrypt(data);
            }
            //Cmd Checksum
            for (int tmp = 0; tmp < data_enc.length; tmp++) {
                sum += (data_enc[tmp] & 0xFF);
            }

            hdrData[2] = (byte) (sum / 256);
            hdrData[3] = (byte) (sum % 256);

            //Header checksum
            for (int i = 0; i < 4; i++) {
                hdrsum += (hdrData[i] & 0xFF);
            }
            hdrData[4] = (byte) (hdrsum / 256);
            hdrData[5] = (byte) (hdrsum % 256);
            /*
            for (int i = 0; i < hdrData.length; i++) {
            out.writeByte(hdrData[i]);
            }
            for (int j = 0; j < data_enc.length; j++) {
            out.writeByte(data_enc[j]);
            }*/
            out.write(hdrData);
            out.write(data_enc);
            out.flush();
        } catch (Exception es) {
            dataIn.append("Error Msg: ");
            dataIn.append(es.getMessage());
        }

    }

    /**
     * This method is solely for debugging purpose (internal use only)
     * @deprecated
     * @return debugging code
     */
    public StringBuffer getData() {
        return dataIn;
    }

    private byte[] getSerialData() {
        return getSerialData(1);
    }

    private byte[] getSerialData(int dataLen) {
        byte[] rv, rv1;
        rv = new byte[]{0x00};
        dataIn = new StringBuffer();
        int retry = 10;
        try {
            do {
                try {
                    synchronized (this) {
                        this.wait(100);
                    }
                } catch (Exception e) {
                }
                retry--;
            } while (dataLen > worker.offset && retry > 1);
            System.out.println("Offset " + worker.offset);
            serialData = new byte[worker.offset];
            System.arraycopy(worker.buffer, 0, serialData, 0, serialData.length);
        } catch (Exception ioe) {
            dataIn.append("IOE");
            ioe.printStackTrace();
            return rv;
        }
        return getSerialData(serialData);
    }

    private byte[] getSerialData(byte[] serialData) {
        byte[] rv, rv1;
        rv = new byte[]{0x00};
        dataIn = new StringBuffer();

        if (serialData == null || serialData.length < 8) {
            dataIn.append("null | len");
            return rv;
        }

        int chksum, hdrsum, reslen, tmp = 0;
        chksum = hdrsum = reslen = tmp;
        chksum = ((serialData[4] & 0xFF) * 256) + (serialData[5] & 0xFF);
        hdrsum = (serialData[6] & 0xFF) * 256 + (serialData[7] & 0xFF);
        reslen = (serialData[0] & 0xFF) * 256 * 256 * 256
                + (serialData[1] & 0xFF) * 256 * 256
                + (serialData[2] & 0xFF) * 256 + (serialData[3] & 0xFF);

        //dataIn = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            tmp += (serialData[i] & 0xFF);
        }


        tmp &= 0xFFFF;
        hdrsum = hdrsum & 0xFFFF;

        if (tmp != hdrsum) {
            rv = new byte[5];
            rv[0] = 0;
            rv[1] = (byte) (tmp / 256);
            rv[2] = (byte) (tmp % 256);
            rv[3] = (byte) (hdrsum / 256);
            rv[4] = (byte) (hdrsum % 256);
            dataIn.append(" hchk " + Integer.toHexString(hdrsum));
            dataIn.append(" tmp " + Integer.toHexString(tmp));
            dataIn.append(" len " + serialData.length);
            System.out.println("Header chk fail");
            return rv;
        }

        tmp = 0;

        for (int j = 8; j < serialData.length; j++) {
            tmp += (serialData[j] & 0xFF);
            tmp &= 0xFFFF;
        }
        chksum = chksum & 0xFFFF;

        if (tmp != chksum) {
            rv = new byte[5];
            rv[0] = 1;
            rv[1] = (byte) (tmp / 256);
            rv[2] = (byte) (tmp % 256);
            rv[3] = (byte) (chksum / 256);
            rv[4] = (byte) (chksum % 256);
            dataIn.append(" fchk " + Integer.toHexString(chksum));
            dataIn.append(" tmp " + Integer.toHexString(tmp));
            dataIn.append(" reslen " + reslen);
            dataIn.append(" len " + serialData.length);
            System.out.println("Body chk fail");
            return rv;
        }

        //rv = new byte[reslen];
        if (isSecure) {
            rv1 = performDecrypt(serialData, 8);
        } else {
            rv1 = performDecrypt(serialData, 8);
        }
        /*for (int x = 0; x < rv.length; x++) {
        rv[x] = rv1[x];
        }*/

        return rv1;
    }

    /**
     *@deprecated
     */
    public void clearData() {
        this.dataIn = new StringBuffer();
    }

    /**
     * To stop and close the communication
     */
    public void closeCom() {
        try {
            //readData = false;
            worker.flag = false;
        } catch (Exception e) {
        }
    }

    /**
     * Send an echo command to BlueFin, for commmunication tesing. BlueFin will
     * echo back the same data sent.
     * @param data test data to be sent
     * @return data echo back from BlueFin
     */
    public byte[] BF_Echo(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        byte[] cmd = new byte[16];
        cmd[0] = 0x01;

        System.arraycopy(data, 0, cmd, 1, data.length);

        writeData(cmd);
        byte[] res = getSerialData();
        if (res[0] == (byte) 0x81) {
            data = new byte[res.length - 1];
            System.arraycopy(res, 1, data, 0, data.length);
            return data;
        } else {
            return null;
        }
    }

    /**
     * For verifcation of the BlueFin Serial Numbers and validity of the AES keys.
     * Required a data wrapper class BFDeviceData, which will encapsulate all
     * the response data within.
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_Verify(BFDeviceData deviceData) {
        if (deviceData == null) {
            return false;
        }
        byte[] cmd = {0x02};
        writeData(cmd);
        char[] deviceName;
        int serialNo;
        byte[] res = getSerialData();
        if (res == null || res[0] != (byte) 0x82) {
            return false;
        }
        deviceName = new char[7];

        for (int i = 0; i
                < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == 0x01) {
            deviceData.setSerialChksumStatus(true);
        } else {
            deviceData.setSerialChksumStatus(false);
        }
        if (res[13] == 0x01) {
            deviceData.setAesChksumStatus(true);
        } else {
            deviceData.setAesChksumStatus(false);
        }

        return true;
    }

    /**
     * For retrieving Bluefin unit's Serial no. and firmware version
     * Required a data wrapper class BFDeviceData, which will encapsulate all
     * the response data within.
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_GetInfo(BFDeviceData deviceData) {
        if (deviceData == null) {
            return false;
        }
        byte[] cmd = {0x03};
        writeData(cmd);
        byte[] res = getSerialData();
        char[] deviceName = new char[7];
        int serialNo, verHigh, verLow, idle;

        if (res[0] != (byte) 0x83) {
            return false;
        }
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);
        verHigh = res[12];
        deviceData.setVersionHigh(verHigh);
        verLow = res[13];
        deviceData.setVersionLow(verLow);
        idle = (res[14] & 0xFF) * 256 + (res[15] & 0xFF);
        deviceData.setIdleTimer(idle);

        return true;
    }

    /**
     * To change the default AES key (Require at least 500ms). This API does
     * not store the key for more than the current instance which this method
     * is called. Subquenent instances have to be set with the correct default
     * AES key using the setDefaultAES() methods
     * @param key the new key to replace the factory defaul key
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     * @see setDefaultAES(byte[] nkey)
     */
    public boolean BF_SetDefaultAes(byte[] key, BFDeviceData deviceData) {
        if (key == null || key.length != 32 || deviceData == null) {
            return false;
        }
        byte[] cmd = new byte[33];
        cmd[0] = 0x04;
        for (int i = 0; i < key.length; i++) {
            cmd[i + 1] = key[i];
        }
        writeData(cmd);
        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (Exception es) {
        }
        byte[] res = getSerialData();

        char[] deviceName = new char[7];
        int serialNo;

        if (res[0] != (byte) 0x84) {
            return false;
        }
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == (byte) 0x01) {
            mKey = key;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the Session key for AES data encryption and enable BlueFin for
     * secure mode command
     * @param key the session AES key
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_SetSessionKey(byte[] key, BFDeviceData deviceData) {
        if (key == null || key.length != 32 || deviceData == null) {
            return false;
        }

        byte[] cmd = new byte[33];
        cmd[0] = 0x05;

        for (int i = 0; i < key.length; i++) {
            cmd[i + 1] = key[i];
        }

        writeData(cmd);
        byte[] res = getSerialData();
        if (res[0] != (byte) 0x85) {
            return false;
        }

        char[] deviceName = new char[7];
        int serialNo;

        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }

        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == (byte) 0x01) {
            isSecure = true;
            seckey = new SecretKeySpec(key, "AES");		// bugfix : 20110515
            getSerialData();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the idle timeout interval. This command requires BlueFin to be in
     * secure mode.
     * @param time idle timeout interval in ms
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_SetIdleTimer(int time, BFDeviceData deviceData) {
        if (deviceData == null || time < 60 || !isSecure) {
            return false;
        }

        byte[] cmd = new byte[3];
        cmd[0] = 0x06;
        cmd[1] = (byte) (time / 256);
        cmd[2] = (byte) (time % 256);

        writeData(cmd);
        byte[] res = getSerialData(24);

        if (res[0] != (byte) 0x86) {
            return false;
        }

        char[] deviceName = new char[7];
        int serialNo, idleTimer;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }

        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);
        idleTimer = (res[12] & 0xFF) * 256 + (res[13] & 0xFF);
        deviceData.setIdleTimer(idleTimer);

        return true;
    }

    /**
     * Switch off BlueFin immediately. This command requires BlueFin to be in
     * secure mode.
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_PowerOff(BFDeviceData deviceData) {
        if (!isSecure || deviceData == null) {
            return false;
        }

        byte[] cmd = {0x07};
        writeData(cmd);

        byte[] res = getSerialData();
        if (res[0] != (byte) 0x87) {
            return false;
        }

        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }

        deviceData.setDeviceName(new String(deviceName));
        serialNo =
                res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == (byte) 0x01) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method is currently not being implemented
     * @param colOff
     * @param col
     * @param rolOff
     * @param row
     * @return
     * @deprecated
     */
    public boolean BF_SetWindow(byte colOff, byte col, byte rolOff, byte row) {
        return false;
    }

    /**
     * This method query the BlueFin for the fingerprint scanning status and
     * the battery level. This command requires BlueFin to be in secure mode.
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_GetFPStatus(BFDeviceData deviceData) {
        if (!isSecure || deviceData == null) {
            return false;
        }
        byte[] cmd = {0x09};
        writeData(cmd);
        byte[] res = getSerialData();
        if (res[0] != (byte) 0x89) {
            return false;
        }
        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == (byte) 0x00) {
            deviceData.setIsScanEnabled(false);
            isReady = false;
        } else {
            deviceData.setIsScanEnabled(true);
            isReady = true;
        }
        deviceData.setfingerImgStatus(res[13]);
        int volt = (res[14] & 0XFF) * 256 + (res[15] & 0xFF);
        deviceData.setBattStatus(volt);

        return true;
    }

    /**
     * To enable or disable the fingerprint scanner onboard BlueFin.
     * This command requires BlueFin to be in secure mode.
     * @param enableScan true to enable and false to disable scanner
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_SetScanStatus(boolean enableScan, BFDeviceData deviceData) {
        if (deviceData == null || !isSecure) {
            return false;
        }

        byte[] cmd = {0x0C, 0x00};

        if (enableScan) {
            cmd[1] = 0x01;
        } else {
            cmd[1] = 0x00;
        }
        writeData(cmd);
        byte[] res = getSerialData();
        if (res[0] != (byte) 0x8C) {
            return false;
        }

        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == (byte) 0x00) {
            deviceData.setIsScanEnabled(false);
        } else {
            deviceData.setIsScanEnabled(true);
        }
        deviceData.setfingerImgStatus(res[13]);

        return true;
    }

    /**
     * Check or Activate the onboard Buzzer.
     * @param duration the buzz duration in millisecond
     * @param activate true to buzz and false to query for buzz status
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_Buzzer(int duration, boolean activate, BFDeviceData deviceData) {
        if (deviceData == null) {
            return false;
        }
        byte[] cmd = {0x0D, 0x00, 0x00, 0x00};
        if (activate) {
            cmd[1] = 0x01;
        }
        cmd[2] = (byte) (duration / 256);
        cmd[3] = (byte) (duration % 256);

        writeData(cmd);

        byte[] res = getSerialData();

        if (res[0] != (byte) 0x8D) {
            return false;
        }
        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);
        int time = (res[12] & 0xFF) * 256 + (res[13] & 0xFF);
        deviceData.setBuzzerTime(time);

        return true;
    }

    /**
     * To Get or Set the minimun coverage area and threshold level of the
     * BlueFin scanner for valid fingerprint.
     * This command requires BlueFin to be in secure mode.
     * @param isSet true to set and false to get the setting
     * @param areaPercent for setting coverage area in percentage
     * @param threshold  for setting the threshold level
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_CoverageArea(boolean isSet, byte areaPercent, byte threshold,
            BFDeviceData deviceData) {
        if (deviceData == null || !isSecure) {
            return false;
        }

        byte[] cmd = {0x0E, 0x00, 0x00, 0x00};

        if (isSet) {
            cmd[1] = 0x01;
            cmd[2] = areaPercent;
            cmd[3] = threshold;
        }
        writeData(cmd);
        byte[] res = getSerialData();

        if (res[0] != (byte) 0x8E) {
            return false;
        }
        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);
        deviceData.setDetectArea(res[12] & 0xFF);
        deviceData.setDetectThreshold(res[13] & 0xFF);

        return true;
    }

    /**
     * To restore the BlueFin unit to factory default settings. Users are
     * required to press and hold onto the power switch when turning on
     * the unit before issuing this command.
     * @param deviceData data wrapper for BlueFin response
     * @return true if operation is successful
     */
    public boolean BF_Reset(BFDeviceData deviceData) {
        if (deviceData == null) {
            return false;
        }
        boolean flag = false;
        byte[] cmd = {0x0F};
        writeData(cmd);
        if (isSecure) {
            isSecure = false;
            flag = true;
        }
        byte[] res = getSerialData();
        if (res == null || res[0] != (byte) 0x8F) {
            if (flag) {
                isSecure = true;
            }
            return false;
        }
        char[] deviceName = new char[7];
        int serialNo;
        for (int i = 0; i
                < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }

        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        if (res[12] == 0x01) {
            deviceData.setSerialChksumStatus(true);
        } else {
            deviceData.setSerialChksumStatus(false);
        }

        if (res[13] == 0x01) {
            deviceData.setAesChksumStatus(true);
        } else {
            deviceData.setAesChksumStatus(false);
        }

        return true;
    }

    private byte[] performEncrypt(byte[] clearText) {


        byte[] tmp = new byte[((clearText.length + 15) / 16) * 16];
        byte[] rv = null;
        try {
            cipher.init(cipher.ENCRYPT_MODE, seckey);
            rv = new byte[cipher.getOutputSize(tmp.length)];
            for (int i = 0; i < clearText.length; i++) {
                tmp[i] = clearText[i];
            }

            int oLen = cipher.update(tmp, 0, tmp.length, rv, 0);

            cipher.doFinal(rv, oLen);
        } catch (Exception ce) {
            dataIn.append(ce.getMessage());
        }

        return rv;
    }

    private byte[] performDecrypt(byte[] cipherText, int offset) {

        byte[] tmp = new byte[cipherText.length - offset];
        byte[] rv = null;
        try {
            cipher.init(cipher.DECRYPT_MODE, seckey);
            for (int i = offset; i < cipherText.length; i++) {
                tmp[i - offset] = cipherText[i];
            }
            rv = new byte[cipher.getOutputSize(tmp.length)];


            int oLen = cipher.update(tmp, 0, tmp.length, rv, 0);

            cipher.doFinal(rv, oLen);
        } catch (Exception ce) {
            dataIn.append(ce.getMessage());
        }

        return rv;
    }

    /**
     * To check if the BlueFin is in secure mode.
     * @return true if the unit is in secure mode.
     */
    public boolean isSecure() {
        return isSecure;
    }

    private void clearInput() {
        try {
            while (in.available() > 0) {
                in.read();
            }
            worker.offset = 0;
        } catch (Exception es) {
        }
    }

    /**
     * For setting the default AES key to use for data encryption, if the
     * factory default AES key had being changed in previous instance.
     * Note that this method does not change the BlueFine default AES, it only
     * update the default key within the API. The API does not have a key store,
     * hence every new instance of the API has to be updated with the new default
     * key.
     * @param nkey the new default AES key
     * @return true if operation is successful
     * @see BF_SetDefaultAes(byte[] key, BFDeviceData deviceData)
     */
    public boolean setDefaultAES(byte[] nkey) {
        if (nkey == null || nkey.length < 32) {
            return false;
        }

        this.mKey = nkey;
        return true;
    }

    /**
     * Get the fingerprint image. This command requires BlueFin to be in secure
     * mode and a fingerprint has to be avaliable for download.
     * @param deviceData data wrapper for BlueFin response
     * @return the Image object of the fingerprint
     * @see Image
     */
    public byte[] BF_GetFPImg(BFDeviceData deviceData) {
        byte[] img = null;
        if (deviceData == null || !isSecure) {
            return img;
        }
        byte[] cmd = {0x0B, 0x00};
        byte[] res = {0x00};
        int counter = 0;
        int offset = 0;
        int retry = 0;
        int[] raw = new int[92160];

        while (counter < 30) {
            cmd[1] = (byte) (counter & 0xFF);
            writeData(cmd);
            res = getSerialData(3096);

            offset = counter * 3072;

            if (res.length > 12 && res[0] == (byte) 0x8B && res[12] == (byte) 0x01) {
                for (int j = 13; j < res.length - 3; j++) {
                    raw[offset + (j - 13)] = res[j];
                }
                retry = 0;
                counter++;
            } else {
                retry++;
                if (retry > 50) {
                    return img;
                }
            }

        }
        int serialNo;
        char[] deviceName = new char[7];
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);
        int left = 0;
        for (int right = raw.length - 1; left < right; left++, right--) {
            // exchange the first and last
            int temp = raw[left];
            raw[left] = raw[right];
            raw[right] = temp;
        }
        deviceData.setFpImage(raw);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(bmpheader);
            byte[] palette = new byte[4];
            palette[3] = 0;
            for (int i = 0; i < 256; i++) {
                palette[0] = palette[1] = palette[2] = (byte) i;

                bout.write(palette);
            }

            for (int temp : raw) {
                bout.write(temp & 0xff);
            }
            img = bout.toByteArray();
            bout.close();
        } catch (Exception ies) {
            ies.printStackTrace();
        }
        return img;
    }

    /**
     * To get and uncompress the compressed fingerPrint image. This command
     * requires BlueFin to be in secure mode and a fingerprint has to be
     * avaliable for download.
     * @param deviceData data wrapper for BlueFin response
     * @return the uncompressed Image object of the fingerprint
     * @see Image
     */
    public byte[] BF_GetKCAImg(BFDeviceData deviceData) {
        byte[] img = null;
        if (deviceData == null || !isSecure) {
            return img;
        }

        byte[] cmd = {0x11, 0x00};
        byte[] res = {0x00};
        int counter = 0;
        int offset = 0;
        int retry = 0;
        int[] raw = new int[36864];

        while (counter < 12) {
            cmd[1] = (byte) (counter & 0xFF);
            writeData(cmd);
            res = getSerialData(3096);

            offset = counter * 3072;

            if (res.length > 12 && res[0] == (byte) 0x91 && res[12] == (byte) 0x01) {
                for (int j = 13; j < res.length - 3; j++) {
                    raw[offset + (j - 13)] = res[j] & 0xFF;
                }
                retry = 0;
                counter++;
            } else {
                retry++;
                if (retry > 50) {
                    return img;
                }
            }
        }
        raw = decompressKCA(raw);
        int left = 0;
        for (int right = raw.length - 1; left < right; left++, right--) {
            // exchange the first and last
            int temp = raw[left];
            raw[left] = raw[right];
            raw[right] = temp;
        }
        deviceData.setFpImage(raw);
        int serialNo;
        char[] deviceName = new char[7];
        for (int i = 0; i < deviceName.length; i++) {
            deviceName[i] = (char) res[i + 1];
        }
        deviceData.setDeviceName(new String(deviceName));
        serialNo = res[8] * 256 * 256 * 256 + res[9] * 256 * 256 + res[10] * 256 + res[11];
        deviceData.setSerialNo(serialNo);

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(bmpheader);
            byte[] palette = new byte[4];
            palette[3] = 0;
            for (int i = 0; i < 256; i++) {
                palette[0] = palette[1] = palette[2] = (byte) i;

                bout.write(palette);
            }

            for (int temp : raw) {
                bout.write(temp & 0xff);
            }
            img = bout.toByteArray();
            bout.close();
        } catch (Exception ies) {
            ies.printStackTrace();
        }
        //img = Image.createRGBImage(raw, 256, 360, false);

        return img;

    }

    private byte[] invertArray(byte[] array) {
        if (array == null | array.length < 2) {
            return array;
        }
        int left = 0;
        for (int right = array.length - 1; left < right; left++, right--) {
            // exchange the first and last
            int temp = array[left];
            array[left] = array[right];
            array[right] = (byte) (temp);
        }
        return array;
    }

    private int[] decompressKCA(int[] data) {
        if (data.length == 0) {
            return null;
        }
        int[] raw = new int[92160];
        int[] KCAData = new int[8];
        int RawData, lastData = 128;
        int count, KCA = 0;
        count = RawData = 0;

        int cnt = 0;
        do {
            KCA = data[cnt++] & 0xFF;
            KCA += (data[cnt++] & 0xFF) * 256;
            KCA += (data[cnt++] & 0xFF) * 256 * 256;
            for (int x = 0; x < 8; x++) {
                KCAData[x] = KCA & 0x07;
                KCA /= 8;
            }

            // reconstruct the data...
            for (int BlkCnt = 0; BlkCnt < 8; BlkCnt++) {
                switch (KCAData[BlkCnt]) {
                    case 0:
                        RawData = (lastData - 64);
                        break;
                    case 1:
                        RawData = (lastData - 22);
                        break;
                    case 2:
                        RawData = (lastData - 8);
                        break;
                    case 3:
                        RawData = (lastData - 3);
                        break;
                    case 4:
                        RawData = (lastData + 3);
                        break;
                    case 5:
                        RawData = (lastData + 8);
                        break;
                    case 6:
                        RawData = (lastData + 22);
                        break;
                    case 7:
                        RawData = (lastData + 64);
                        break;
                    default:
                        dataIn.append("Data > 7");
                }
                lastData = RawData;
                if (count == 92159) {
                    break;
                }
                raw[count++] = lastData & 0xFF;
            }
        } while (cnt < data.length);

        return raw;
    }

    class FetchWorker implements Runnable {

        public boolean flag = true;
        int tmp, offset = 0;
        public byte[] buffer = new byte[4000];

        public void run() {
            while (flag) {
                try {
                    if (in.available() > 0) {
                        tmp = in.available();
                        in.readFully(buffer, offset, in.available());
                        offset += tmp;
                        /*
                        System.out.println("Data");
                        for (int i = 0; i < offset; i++) {
                        System.out.print(" " + Integer.toHexString(buffer[i] & 0xff));
                        }*/
                    }
                } catch (Exception es) {
                    System.out.println("Thread");
                    es.printStackTrace();
                }
            }
        }
    }
}
