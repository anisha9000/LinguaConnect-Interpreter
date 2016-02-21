package interpreter.linguaconnect.com.linguaconnectinterpreter.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import interpreter.linguaconnect.com.linguaconnectinterpreter.AppController;
import interpreter.linguaconnect.com.linguaconnectinterpreter.R;
import interpreter.linguaconnect.com.linguaconnectinterpreter.ui.HistoryActivity;

/**
 * Created by anisha on 19/1/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final ArrayList<HistoryItem> mDataset;
    private final HistoryActivity activity;
    private String TAG = HistoryAdapter.class.getName();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final HistoryItem historyDetail = mDataset.get(position);

        holder.tvName.setText(historyDetail.getName());
        holder.tvLanguage.setText(activity.getResources().getString(R.string.displayLanguage)
                + historyDetail.getLanguage());
        holder.tvDuration.setText(String.valueOf(historyDetail.getDuration()));
        holder.tvStatus.setText(historyDetail.getStatus());
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        if(historyDetail.getPictureUrl() != null) {
            imageLoader.get(historyDetail.getPictureUrl(),
                    ImageLoader.getImageListener(holder.ivInterpreterImage,R.mipmap.profile,R.mipmap.profile));
        }

        setRating(holder, historyDetail.getRating());
        holder.tvBookingTime.setText(historyDetail.getBookingTime());


    }

    private void setTime(TextView tvDuration, String startTime, String endTime) {
        startTime = "2016-01-13T19:56:13.384027+00:00";
        endTime = "2016-01-13T19:59:13.384027+00:00";

        tvDuration.setVisibility(View.GONE);
        /*SimpleDateFormat dateFormat = new SimpleDateFormat();
        Date startDate = new Date(duration);
        Date endDate = new Date(endTime);

        long difference = startDate.getTime() - endDate.getTime();
        Log.e(TAG,"difference:"+ difference);*/



    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView tvName, tvLanguage, tvDuration, tvStatus, tvBookingTime;
        ImageView ivInterpreterImage;
        ImageView[] stars;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.interpreter_name);
            tvLanguage = (TextView) itemView.findViewById(R.id.language);
            tvDuration = (TextView) itemView.findViewById(R.id.duration);
            tvStatus = (TextView) itemView.findViewById(R.id.status);
            ivInterpreterImage = (ImageView) itemView.findViewById(R.id.interpreter_image);
            stars=new ImageView[5];
            stars[0]=(ImageView)itemView.findViewById(R.id.rate1);
            stars[1]=(ImageView)itemView.findViewById(R.id.rate2);
            stars[2]=(ImageView)itemView.findViewById(R.id.rate3);
            stars[3]=(ImageView)itemView.findViewById(R.id.rate4);
            stars[4]=(ImageView)itemView.findViewById(R.id.rate5);
            tvBookingTime = (TextView) itemView.findViewById(R.id.booking_time);
        }
    }

    public HistoryAdapter(ArrayList<HistoryItem> myDataset, HistoryActivity activity) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    void setRating(ViewHolder holder,int rating){
        for(int i=1;i<=5;i++) {
            if(i<=rating) {
                holder.stars[i-1].setImageResource(R.mipmap.ic_star_black);
            } else if (i > rating) {
                holder.stars[i-1].setImageResource(R.mipmap.ic_star_border_black);
            } else {
                holder.stars[i-1].setImageResource(R.mipmap.ic_star_half_black);
            }
        }
    }
}
