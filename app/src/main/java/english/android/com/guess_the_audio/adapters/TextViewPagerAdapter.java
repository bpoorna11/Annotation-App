package english.android.com.guess_the_audio.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import english.android.com.guess_the_audio.R;
import english.android.com.guess_the_audio.models.PronounceData;

public class TextViewPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    private List<PronounceData> lists;

    @BindView(R.id.tv_english_text)
    TextView mEnglishTextView;
    @BindView(R.id.tv_local_text)
    TextView mLocalTextView;

    public TextViewPagerAdapter(Context context, List<PronounceData> lists) {
        mContext = context;
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.pronouncer_text_layout, container, false);
        ButterKnife.bind(this, view);
        PronounceData pronounceData = lists.get(position);
        mEnglishTextView.setText(pronounceData.getEnglishText());
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
