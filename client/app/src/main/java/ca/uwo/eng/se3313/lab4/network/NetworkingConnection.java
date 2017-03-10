package ca.uwo.eng.se3313.lab4.network;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import ca.uwo.eng.se3313.lab4.network.response.ResponseVisitor;

/**
 * Created by danazagar on 2016-12-05.
 */

public class NetworkingConnection implements INetworkingConnection {
    //VARS
    private InetAddress _addr;
    private int _port;
    private ResponseVisitor _responseHandler;
    private Socket _sock;
    private Thread _sockReadThread;
    private ReentrantLock sockLock;

    //CONSTRUCTOR
    public NetworkingConnection(ResponseVisitor respVisitor, String address, String port) throws UnknownHostException {
        sockLock = new ReentrantLock();
        _responseHandler = respVisitor;
        _addr = InetAddress.getByName(address);
        Log.d("Address: ", _addr.toString());
        _port = Integer.parseInt(port);
        Log.d("Port #: ", Integer.toString(_port));
        _responseHandler = respVisitor;
        _sockReadThread = new Thread(new ReaderThread());
        _sockReadThread.start();
    }

    @Override
    public String getAddress(){
        return _addr.toString();
    }

    @Override
    public int getPort(){
        return _port;
    }

    @Override
    public ResponseVisitor getResponseHandler(){
        return _responseHandler;
    }

    @Override
    public <T extends INetworkInstance>
    AsyncTask<T, ?, ?> send(@NonNull final T message,
                            @NonNull final OnErrorSend errorHandler,
                            @Nullable final OnSuccessfulSend<T> successHandler){
        final NetworkingConnection self = this;
        return new AsyncTask<T, Object, Exception>(){
            protected Exception doInBackground(T... params){
                Exception err = null;
                sockLock.lock();
                try {
                    PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())), true);
                    output.println(message.toJson().toString());
                } catch (IOException e){
                    err = e;
                } finally {
                    sockLock.unlock();
                }
                return err;
            }
            protected void onPostExecute(Exception err){
                if (err == null && successHandler != null){
                    successHandler.onSuccess(self, message);
                } else if (err != null){
                    errorHandler.onError(err);
                }
            }
        };

    }

    @Override
    public <T extends INetworkInstance> AsyncTask<T, ?, ?>
    send(@NonNull T message,
         @NonNull OnErrorSend errorHandler){
        return send(message, errorHandler, null);
    }

    public void close(){ }

    class ReaderThread implements Runnable {
        public void run(){
            try {
                sockLock.lock();
                _sock = new Socket(_addr, _port);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int bytesRead;
                InputStream stream = _sock.getInputStream();
                sockLock.unlock();
                while ((bytesRead = stream.read(data)) != -1) {
                    baos.write(data, 0, bytesRead);
                    _responseHandler.visit(baos.toString());
                    Log.d("Something is coming", baos.toString());
                    baos.reset();
                    Log.d("Reset BAOS", baos.toString());
                }
                Log.d("wat", baos.toString());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


}
