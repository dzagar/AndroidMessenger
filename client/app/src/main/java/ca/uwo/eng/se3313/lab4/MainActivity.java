package ca.uwo.eng.se3313.lab4;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.joda.time.DateTime;

import java.lang.reflect.Method;
import java.net.UnknownHostException;

import ca.uwo.eng.se3313.lab4.network.ErrorCode;
import ca.uwo.eng.se3313.lab4.network.INetworkingConnection;
import ca.uwo.eng.se3313.lab4.network.NetworkingConnection;
import ca.uwo.eng.se3313.lab4.network.request.LoginRequest;
import ca.uwo.eng.se3313.lab4.network.request.MessageRequest;
import ca.uwo.eng.se3313.lab4.network.response.AbstractResponseVisitor;
import ca.uwo.eng.se3313.lab4.network.response.LoginResponse;
import ca.uwo.eng.se3313.lab4.network.response.MessageResponse;
import ca.uwo.eng.se3313.lab4.network.response.ServerError;

/**
 * The main application activity. This utilizes multiple fragments to run, {@link LoginFragment} and
 * {@link RoomFragment}.
 *
 */
public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnInteractionListener,
                   RoomFragment.OnInteractionListener {

    /**
     * Defines the marker tag for logging.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Used to switch between fragments.
     */
    private FragmentManager mFragmentManager;

    /**
     * Fragment referring to the view of the chat room.
     */
    private RoomFragment mRoomFragment;

    /**
     * The connection to the server.
     */
    private INetworkingConnection mConnection;
    /**
     * Created by danazagar on 2016-12-08.
     */

    public static interface IUserInterfaceConnection {

        public void onLoginMsg(LoginResponse msg);

        public void onMessageMsg(MessageResponse msg);

    }

    // TODO SE3313A
    // Insert any state here:
    private String _user;
    private enum State {
        NOCONNECTION, HASCONNECTION, ATTEMPTEDLOGIN, ISLOGGEDIN
    };
    private State state;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ---------- DO NOT TOUCH THIS ----------
        // Initialize the Main Activity to show the Server information
        mFragmentManager = MainActivity.this.getSupportFragmentManager();
        final FragmentTransaction trans = mFragmentManager.beginTransaction();

        trans.replace(R.id.fragment_root, LoginFragment.newInstance(savedInstanceState), LoginFragment.TAG);

        trans.commit();
        // ---------- END NO TOUCH ----------

        // TODO SE3313A
        // Do any state-related work here. Activity#onCreate() is called when the application starts
        // there is no UI shown, yet, thus can not be accessed. All of the UI components are stored
        // in Fragments.
        state = State.NOCONNECTION;
    }

    //TODO: Do a login function
    public void login(String addr, String port, String user){
        if (state != State.NOCONNECTION){
            Log.d("State", state.toString());
            return;
        }
        if (user.length() < 3){
            Log.d("Username", "length too short");
            return; //BAD HUMAN!!!
        }
        _user = user;
        try {
            mConnection = new NetworkingConnection(new MyResponseVisitor(), addr, port);
            state = State.HASCONNECTION;
        } catch (UnknownHostException e){
            e.printStackTrace();
            //there is an unknown host aka ur IP is wrong bish
            Log.d("Bad IP address", addr);
            state = State.NOCONNECTION;
        }
        //make the error handler somewher
        INetworkingConnection.OnErrorSend handleErr = new INetworkingConnection.OnErrorSend(){
            public void onError(@NonNull Throwable err){
                err.printStackTrace();
            }
        };
        //send the login request somewhere(???)
        LoginRequest loginReq = new LoginRequest(user);
        mConnection.send(loginReq, handleErr).execute();
        state = State.ATTEMPTEDLOGIN;
    }

    public void onMsgSend(String msg){
        if (state != State.ISLOGGEDIN){
            return;
        }
        //error handler stuff somewhere
        INetworkingConnection.OnErrorSend handleErr = new INetworkingConnection.OnErrorSend() {
            public void onError(@NonNull Throwable err) {
                err.printStackTrace();
            }
        };
        if (msg.length() == 0) return; //no time for blank msgs
        //send message request somewhere
        MessageRequest msgReq = new MessageRequest(DateTime.now(), _user, msg);
        mConnection.send(msgReq, handleErr).execute();
    }


    // ------ DO NOT MODIFY BELOW ------

    /**
     * Shows a {@link RoomFragment} and hides the {@link LoginFragment}
     */
    private void showRoomFragment() {
        Log.d(TAG, "Loading the room...");

        mRoomFragment = new RoomFragment();

        final FragmentTransaction trans = mFragmentManager.beginTransaction();

        trans.replace(R.id.fragment_root, mRoomFragment, RoomFragment.TAG);

        trans.commit();
    }

    private class UIConnection implements IUserInterfaceConnection {
        @Override
        public void onMessageMsg(MessageResponse msg){
            Class<?>[] args = { DateTime.class, String.class, String.class };
            try {
                Method method = mRoomFragment.getClass().getDeclaredMethod("createUserMessageElements", args);
                method.setAccessible(true);
                Object[] params = {DateTime.now(), msg.getOriginator(), msg.getContent()};
                method.invoke(mRoomFragment, params);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onLoginMsg(LoginResponse msg){
            Class<?>[] args = { DateTime.class, String.class };
            try {
                Method method = mRoomFragment.getClass().getDeclaredMethod("createLoggedInElements", args);
                method.setAccessible(true);
                Object[] params = {DateTime.now(), msg.getJoiningUsername()};
                method.invoke(mRoomFragment, params);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class MyResponseVisitor extends AbstractResponseVisitor {
        @Override
        public void visitLogin(@NonNull LoginResponse login) {
            if (login.getJoiningUsername().equals(_user)){ //if the login username equals _user
                //show the chat room, logged in!
                state = State.ISLOGGEDIN;
                showRoomFragment();
            }
            if (state == State.ISLOGGEDIN){
                final LoginResponse finalLogin = login;
                Runnable showLoggedIn = new Runnable() {
                    public void run(){
                        UIConnection connectUI = new UIConnection();
                        connectUI.onLoginMsg(finalLogin);
                    }
                };
                runOnUiThread(showLoggedIn);
            }

        }

        @Override
        public void visitMessage(@NonNull MessageResponse message) {
            if (state != State.ISLOGGEDIN){
                return;
            }
            //if the sender of the msg has the username _user
            if (message.getOriginator().equals(_user)) {
                //set the text field to nothing cuz they sent that shit
                Runnable resetTextField = new Runnable() {
                    public void run() {
                        mRoomFragment.msgText.setText("");
                    }
                };
                runOnUiThread(resetTextField);
            }
            //create a msg and send it
            final MessageResponse finalMessage = message;
            Runnable createMsg = new Runnable() {
                public void run() {
                    UIConnection connectUI = new UIConnection();
                    connectUI.onMessageMsg(finalMessage);
                }
            };
            runOnUiThread(createMsg);
        }

        @Override
        public void visitError(@NonNull ServerError error) {
            //Send a server error?? idk
            Log.d("Serv err", error.toString());
        }

        @Override
        public void error(@NonNull ErrorCode code, String message) {
            //no idea
            Log.d("Internal err , " + code.toString(), message);
        }
    }

}

