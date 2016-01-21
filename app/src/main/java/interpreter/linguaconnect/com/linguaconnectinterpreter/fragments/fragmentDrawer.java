package interpreter.linguaconnect.com.linguaconnectinterpreter.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.fragments.adapter.NavDrawerAdapter;
import interpreter.linguaconnect.com.linguaconnectinterpreter.fragments.adapter.NavDrawerItem;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.EditProfile;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.HistoryActivity;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.LoginActivity;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Constants;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.HandleProgressBar;
import interpreter.linguaconnect.com.linguaconnectinterpreter.utils.Utility;

/**
 * Created by anisha on 7/1/16.
 */
public class fragmentDrawer extends Fragment {

    private FragmentDrawerListener drawerListener;
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavDrawerAdapter adapter;
    private View containerView;
    private byte activityCode;
    private TextView userName;
    private HandleProgressBar progressBar;
    private String TAG = fragmentDrawer.class.getName();
    private View.OnClickListener openProfileListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getActivity(), EditProfile.class));
        }
    };

    enum DrawerMenuClick {HISTORY, SIGNOUT }

    private static int[] imagesId = new int[]{  R.mipmap.ic_history_white,
            R.mipmap.ic_phonelink_erase_white
    };
    // drawer labels
    private static String[] titles = new String[]{ "History", "Logout"};

    public fragmentDrawer() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    public static List<NavDrawerItem> getData() {
        List<NavDrawerItem> data = new ArrayList<>();

        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            NavDrawerItem navItem = new NavDrawerItem(titles[i],imagesId[i]);
            data.add(navItem);
        }
        return data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_drawer_layout, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        TextView userName = (TextView) layout.findViewById(R.id.drawer_user_name);
        userName.setText(Utility.getLocalString(getActivity(), Constants.USER_FIRST_NAME) + " "+
                Utility.getLocalString(getActivity(), Constants.USER_LAST_NAME));
        ImageView profileImage = (ImageView) layout.findViewById(R.id.avatarProfilePic);
        profileImage.setOnClickListener(openProfileListener);

        adapter = new NavDrawerAdapter(getActivity(), getData());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent drawerIntent = null;
                        DrawerMenuClick drawerItem = values()[position];

                        switch (drawerItem) {
                            case HISTORY:
                                drawerIntent = new Intent(getActivity(), HistoryActivity.class);
                                startActivity(drawerIntent);
                                break;
                    /*case WALLET_HISTORY:
                        drawerIntent = new Intent(getActivity(), WalletHistory.class);
                        startActivity(drawerIntent);
                        break;
                    case TRANSACTION_HISTORY:
                        drawerIntent = new Intent(getActivity(), TransactionActivity.class);
                        startActivity(drawerIntent);
                        break;
                    case CHAT:
                        Log.e(TAG, "chat conversation called");
                        drawerIntent = new Intent(getActivity(), StartConversationActivity.class);
                        startActivity(drawerIntent);
                        break;
                    case INVITE:
                        drawerIntent = new Intent(getActivity(), InviteActivity.class);
                        startActivity(drawerIntent);
                        break;*/

                            case SIGNOUT:
                                Log.e(TAG, "Signout Clicked");
                                showLogoutAlert(getActivity());

                                break;
                            default:
                                break;
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawer(containerView);
                            }
                        },700);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));
        return layout;
    }

    private DrawerMenuClick[] values() {
        return DrawerMenuClick.values();
    }

    public void showLogoutAlert(final Activity activity) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage(getResources().getString(R.string.sign_out_msg));
        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                signOut();
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    void signOut() {
        progressBar = new HandleProgressBar(getActivity());
        if (progressBar != null) {
            progressBar.setMessage(getString(R.string.signout_progress));
        }
        progressBar.showProgressBar();
        Utility.clearPreferences(getActivity());

        Thread toRun = new Thread() {
            public void run() {
                try {
                    sleep(500);
                    Utility.saveLocalBoolean(getActivity(), Constants.IS_LOGGED_IN, false);
                    final Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(mainIntent);
                    getActivity().finish();
                    progressBar.hideProgressBar();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        toRun.start();

    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

    }

    public void setUp(final int fragmentId, final DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.empty_string, R.string.empty_string) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerListener.onDrawerOpen();
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerListener.onDrawerClose();
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public interface FragmentDrawerListener {
        void onDrawerOpen();
        void onDrawerClose();
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(containerView);
    }

}
