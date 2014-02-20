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

public class BFDeviceData {

    private String deviceName;
    private int serialNo, versionHigh, versionLow, idleTimer, fingerImgStatus, battStatus, buzzerTime, detectArea, detectThreshold;
    private boolean isScanEnabled;
    public final int NO_FINGERPRINT = 0, PRE_FINGERPRINT = 1, NEW_FINGERPRINT = 2;
    private int[] fpImage;

    public int[] getFpImage() {
        return fpImage;
    }

    public void setFpImage(int[] fpImage) {
        this.fpImage = fpImage;
    }

    public int getVersionLow() {
        return versionLow;
    }

    public void setVersionLow(int versionLow) {
        this.versionLow = versionLow;
    }
    private boolean serialChksumStatus, aesChksumStatus;

    public boolean isAesChksumStatus() {
        return aesChksumStatus;
    }

    public void setAesChksumStatus(boolean aesChksumStatus) {
        this.aesChksumStatus = aesChksumStatus;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName.trim();
    }

    public int getIdleTimer() {
        return idleTimer;
    }

    public void setIdleTimer(int idleTimer) {
        this.idleTimer = idleTimer;
    }

    public boolean isSerialChksumStatus() {
        return serialChksumStatus;
    }

    public void setSerialChksumStatus(boolean serialChksumStatus) {
        this.serialChksumStatus = serialChksumStatus;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public int getVersionHigh() {
        return versionHigh;
    }

    public void setVersionHigh(int version) {
        this.versionHigh = version;
    }

    public int getfingerImgStatus() {
        return fingerImgStatus;
    }

    public void setfingerImgStatus(int fingerImgStatus) {
        this.fingerImgStatus = fingerImgStatus;
    }

    public boolean isScanEnabled() {
        return isScanEnabled;
    }

    public void setIsScanEnabled(boolean isScanEnabled) {
        this.isScanEnabled = isScanEnabled;
    }

    public int getBattStatus() {
        return battStatus;
    }

    public void setBattStatus(int battStatus) {
        this.battStatus = battStatus;
    }

    public int getBuzzerTime() {
        return buzzerTime;
    }

    public void setBuzzerTime(int buzzerTime) {
        this.buzzerTime = buzzerTime;
    }

    public int getDetectArea() {
        return detectArea;
    }

    public void setDetectArea(int detectArea) {
        this.detectArea = detectArea;
    }

    public int getDetectThreshold() {
        return detectThreshold;
    }

    public void setDetectThreshold(int detectThreshold) {
        this.detectThreshold = detectThreshold;
    }
}
