package edu.colorado.playfulcomputation.blockytalky;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import edu.mit.appinventor.ble.BluetoothLE;
import java.util.List;

@DesignerComponent(version = 1,
  description = "BlockyTalkyBLE",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/bluetooth.png")
@SimpleObject(external = true)
public class BlockyTalkyBLEExtension extends AndroidNonvisibleComponent implements BluetoothLE.BluetoothConnectionListener {

  // Define the service and characteristic UUIDs for Foo
  private static final String BTBLE_SERVICE = "0b78ac2d-fe36-43ac-32d0-a29d8fbe05d6".toUpperCase();
  private static final String BTBLE_TX_CHARACTERISTIC = "0b78ac2d-fe36-43ac-32d0-a29d8fbe05d7".toUpperCase();
  private static final String BTBLE_RX_CHARACTERISTIC = "0b78ac2d-fe36-43ac-32d0-a29d8fbe05d8".toUpperCase();


  private BluetoothLE bleDevice;

  // Create a response handler that will run when BTBLE Messages are received.
  private final BluetoothLE.BLEResponseHandler<Integer> BTBLEHandler = new BluetoothLE.BLEResponseHandler<Integer>() {
    @Override
    public void onReceive(String serviceUuid, String characteristicUuid, List<Integer> bytes) {
      BTMessage message = BTBLEEncoder.DecodeMessage(bytes);
      if (message.isIntType()){
        IntReceived(message.key, message.intValue);
      } else if (message.isStringType()){
        StringReceived(message.key, message.stringValue);
      } else if (message.isFloatType()){
        //TODO: Handle float messages
      } else {
        //TODO: Somehow report error to user.
      }
    }

    // @Override
    // public void onWrite(String serviceUuid, String characteristicUuid, List<Integer> values) {
    //   StringWritten(values.get(0));
    //   IntWritten(values.get(0));
    // }
  };

  public BlockyTalkyBLEExtension(ComponentContainer container) {
    super(container.$form());
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":edu.mit.appinventor.ble.BluetoothLE")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void BluetoothDevice(BluetoothLE device) {
    if (bleDevice == device) return;
    if (bleDevice != null) bleDevice.removeConnectionListener(this);
    bleDevice = device;
    if (bleDevice != null) bleDevice.addConnectionListener(this);
  }

  @SimpleProperty
  public BluetoothLE BluetoothDevice() {
    return bleDevice;
  }

  //No need to ever do a manual read, so disabling this template block
  // @SimpleFunction(description = "Reads Foo from the device. The read Foo will be reported in the FooReceived event.")
  // public void ReadFoo() {
  //   if (bleDevice != null) {
  //     bleDevice.ExReadByteValues(FOO_SERVICE, FOO_CHARACTERISTIC, /* signed */ false, BTBLEHandler);
  //   }
  // }

  // No reason the user should need to manually start and stop getting updates, so hiding block
  //@SimpleFunction(description = "Requests updates to Foo from the device. The updates to Foo will be reported in the FooReceived event.")
  public void RequestBTBLEUpdates() {
    if (bleDevice != null) {
      bleDevice.ExRegisterForByteValues(BTBLE_SERVICE, BTBLE_TX_CHARACTERISTIC, /* signed */ false, BTBLEHandler);
    }
  }

  // No reason the user should need to manually start and stop getting updates, so hiding block
  //@SimpleFunction(description = "Stop receiving updates for Foo from the device.")
  public void StopBTBLEUpdates() {
    if (bleDevice != null) {
      bleDevice.ExUnregisterForValues(BTBLE_SERVICE, BTBLE_TX_CHARACTERISTIC, BTBLEHandler);
    }
  }

  @SimpleFunction(description = "Send a key-value message with a String value")
  public void SendString(String key, String value) {

    //check for valid values
    boolean errorHasOccurred = false;
    if (key.length() > 6){
      form.dispatchErrorOccurredEvent(this, "SendString", ErrorMessages.ERROR_EXTENSION_ERROR, 1, "BlockyTalkyBLEExtension", "Key is too long. Valid keys are 1-6 characters long.");
      errorHasOccurred = true;
    }
    if (value.length() > 11){
      form.dispatchErrorOccurredEvent(this, "SendString", ErrorMessages.ERROR_EXTENSION_ERROR, 1, "BlockyTalkyBLEExtension", "Value is too long. Valid values 1-11 characters long.");
      errorHasOccurred = true;
    }
    if (errorHasOccurred) return;

    List<Integer> encodedMessage = (new BTMessage(key, value)).encodeAsBytesForAppInventorBLE();

    bleDevice.ExWriteByteValues(BTBLE_SERVICE, BTBLE_RX_CHARACTERISTIC, /* signed */ false, encodedMessage);
  }


  @SimpleFunction(description = "Send a key-value message with an Integer value")
  public void SendInt(String key, int value) {

    List<Integer> encodedMessage = (new BTMessage(key, value)).encodeAsBytesForAppInventorBLE();

    bleDevice.ExWriteByteValues(BTBLE_SERVICE, BTBLE_RX_CHARACTERISTIC, /* signed */ false, encodedMessage);
  }


  @SimpleEvent
  public void Connected() {
    EventDispatcher.dispatchEvent(this, "Connected");
    //StartBTBLEUpdates();
  }

  @SimpleEvent
  public void Disconnected() {
    EventDispatcher.dispatchEvent(this, "Disconnected");
    //StopBTBLEUpdates();
  }

  @SimpleEvent
  public void StringReceived(String key, String value) {
    EventDispatcher.dispatchEvent(this, "StringReceived", key, value);
  }

  @SimpleEvent
  public void IntReceived(String key, int value) {
    EventDispatcher.dispatchEvent(this, "IntReceived", key, value);
  }


  // @SimpleEvent
  // public void FooWritten(int foo) {
  //   EventDispatcher.dispatchEvent(this, "FooWritten", foo);
  // }

  // BluetoothConnectionListener implementation
  public void onConnected(BluetoothLE bleConnection) {
    Connected();

    RequestBTBLEUpdates();
  }

  public void onDisconnected(BluetoothLE bleConnection) {
    Disconnected();
  }
}
