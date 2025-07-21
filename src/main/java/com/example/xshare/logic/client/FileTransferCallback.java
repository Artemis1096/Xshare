package com.example.xshare.logic.client;

public interface FileTransferCallback {
    void onProgressUpdate(String filename,double progress);  // Called periodically during the file transfer
    void onTransferComplete(String fileName, double fileSize);  // Called when a file transfer is successfully completed
    void onTransferFailed(String errorMessage);  // Called if there is an error in transfer
}
