package ca.uwo.eng.se3313.lab4;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Pattern;

import ca.uwo.eng.se3313.lab4.network.INetworkingConnection;
import ca.uwo.eng.se3313.lab4.network.request.LoginRequest;
import ca.uwo.eng.se3313.lab4.network.response.ResponseVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    /**
     * Tag for {@link LoginFragment}
     */
    public static final String TAG = LoginFragment.class.getName();

    private View mLoginFormView;

    /**
     * Reference to the Activity or Fragment that created this instance.
     */
    private OnInteractionListener mListener;

    // TODO SE3313
    // Add any fields you want for state below this
    Button loginButton;
    EditText addressTextField;
    EditText portTextField;
    EditText userField;



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.mLoginFormView = view.findViewById(R.id.login_form);

        // TODO SE3313 Set up your view references and interactions.
        // Default the fields to the strings default_* (e.g. R.string.default_username)
        View.OnClickListener submitLogin = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressTextField.getText().toString();
                String port = portTextField.getText().toString();
                String user = userField.getText().toString();
                mListener.login(address, port, user); //Will this work???
            }
        };
        loginButton = (Button)view.findViewById(R.id.sign_in_button);
        loginButton.setOnClickListener(submitLogin);
        addressTextField = (EditText)view.findViewById(R.id.address);
        portTextField = (EditText)view.findViewById(R.id.port);
        userField = (EditText)view.findViewById(R.id.username);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mLoginFormView.animate().cancel();
        this.mLoginFormView = null;

        // TODO SE3313 Set all of your view references to null here
        loginButton = null;
        addressTextField = null;
        portTextField = null;
        userField = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnInteractionListener {

       // TODO SE3313 Add any interactions you expect the MainActivity to have
        public void login(String address, String port, String username);
    }

    // ------ DO NOT MODIFY BELOW THIS ------
    // The following are all used to create UI components, you'll want to use the functions but do
    // not change them.

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(@Nullable Bundle bundle) {
        final LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Assign the listener
        try {
            mListener = (OnInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement OnInteractionListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    private static final Pattern P_HOSTNAME = Pattern.compile("^(?:(?:\\w|\\w[\\w\\-]*\\w)\\.)*(?:\\w|\\w[\\w\\-]*\\w)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern P_IPADDR = Pattern.compile("^(?:(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])$");

    /**
     * Checks if a hostname is valid
     * @param address Address to match against
     * @return {@code true} if the host is valid
     *
     * @see <a href="http://stackoverflow.com/a/106223/1748595">Original Source</a>
     */
    private boolean isValidHostname(@NonNull final String address) {
        return P_HOSTNAME.matcher(address).matches() || P_IPADDR.matcher(address).matches();
    }
}
