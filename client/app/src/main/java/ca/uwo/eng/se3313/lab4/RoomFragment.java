package ca.uwo.eng.se3313.lab4;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Function;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import ca.uwo.eng.se3313.lab4.network.response.LoginResponse;
import ca.uwo.eng.se3313.lab4.network.response.MessageResponse;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnInteractionListener} interface
 * to handle interaction events.
 */
public class RoomFragment extends Fragment {

    public static final String TAG = RoomFragment.class.getName();

    /**
     * The table of messages
     */
    private TableLayout mMessageTable;

    private OnInteractionListener mListener;

    // TODO SE3313 put your fields below:
    EditText msgText;
    ImageButton sendButton;



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mMessageTable = (TableLayout)view.findViewById(R.id.messageTable);
        mMessageTable.removeAllViewsInLayout();

        getActivity().setTitle(R.string.title_activity_room);

        // TODO SE3313 Put your view interactions heere
        // e.g. sendButton = (ImageButton)view.findViewById(R.id.sendMsgButton);
        // mMessageTable is already intialized for you.
        View.OnClickListener submitMsg = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgText.getText().toString();
                mListener.onMsgSend(msg);
            }
        };
        sendButton = (ImageButton)view.findViewById(R.id.sendMsgButton);
        sendButton.setOnClickListener(submitMsg);
        msgText = (EditText)view.findViewById(R.id.editMessage);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mMessageTable = null;

        // TODO SE3313 Set all of your View references to null
        sendButton = null;
        msgText = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnInteractionListener {

        // TODO SE3313 Add any interactions you expect the MainActivity to have
        public void onMsgSend(String msg);
    }

    // ------ DO NOT MODIFY BELOW THIS ------
    // The following are all used to create UI components, you'll want to use the functions but do
    // not change them.

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room, container, false);
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");

    /**
     * Creates an entry when a message is received and adds it to the {@link #mMessageTable}.
     *
     * @param dateTime When the message was received by the server
     * @param username Who sent the message
     * @param content Content of the message
     */
    private void createUserMessageElements(final DateTime dateTime, final String username, final String content) {
        final Resources res = getResources();

        final TableRow infoRow = new TableRow(getActivity());

        final TextView dateView = new TextView(getActivity());
        dateView.setText(dateTime.toString(DATE_TIME_FORMATTER));
        final TableRow.LayoutParams dateLayoutParams = new TableRow.LayoutParams(res.getInteger(R.integer.msgs_date_column));
        dateLayoutParams.weight =  res.getInteger(R.integer.msgs_date_weight);
        dateLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;

        infoRow.addView(dateView, dateLayoutParams);

        // Username:
        final TextView usernameView = new TextView(getActivity());
        usernameView.setText(username);
        final TableRow.LayoutParams usernameLayoutParams = new TableRow.LayoutParams(res.getInteger(R.integer.msgs_name_column));
        usernameLayoutParams.weight = res.getInteger(R.integer.msgs_name_weight);
        usernameLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        usernameLayoutParams.span = 2;

        infoRow.addView(usernameView, usernameLayoutParams);

        // Row for message:
        final TableRow contentRow = new TableRow(getActivity());

        final TextView contentView = new TextView(getActivity());
        contentView.setText(content);
        contentView.setMaxLines(res.getInteger(R.integer.msgs_content_maxlines));
        contentView.setHorizontallyScrolling(false);

        final TableRow.LayoutParams contentLayoutParams = new TableRow.LayoutParams(res.getInteger(R.integer.msgs_content_column));
        contentLayoutParams.span = 3;

        contentRow.addView(contentView, contentLayoutParams);

        mMessageTable.addView(infoRow);
        mMessageTable.addView(contentRow);
    }

    /**
     * Creates a table entry when someone logs into the server.
     * @param dateTime When the login occurred
     * @param username Who logged in
     */
    private void createLoggedInElements(final DateTime dateTime, final String username) {
        final Resources res = getResources();

        final TableRow infoRow = new TableRow(getActivity());

        final TextView dateView = new TextView(getActivity());
        dateView.setText(dateTime.toString(DATE_TIME_FORMATTER));
        final TableRow.LayoutParams dateLayoutParams = new TableRow.LayoutParams(0);
        dateLayoutParams.weight =  res.getInteger(R.integer.msgs_date_weight);
        dateLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        infoRow.addView(dateView, dateLayoutParams);

        final ImageView joinedImg = new ImageView(getActivity());
        joinedImg.setImageResource(android.R.drawable.ic_input_add);

        final TableRow.LayoutParams joinedImgLayoutParams = new TableRow.LayoutParams(1);
        final int join_icon_weight = res.getInteger(R.integer.msgs_join_icon_weight);
        joinedImgLayoutParams.weight = join_icon_weight;
        joinedImgLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;

        infoRow.addView(joinedImg, joinedImgLayoutParams);

        // Username:
        final TextView usernameView = new TextView(getActivity());
        usernameView.setText(username);
        final TableRow.LayoutParams usernameLayoutParams = new TableRow.LayoutParams(2);
        usernameLayoutParams.weight = res.getInteger(R.integer.msgs_name_weight) - join_icon_weight;
        usernameLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;

        infoRow.addView(usernameView, usernameLayoutParams);

        mMessageTable.addView(infoRow);
    }
}
