package ashiqur.goriberfitbit.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.TextView;

import ashiqur.goriberfitbit.R;
import hivatec.ir.suradapter.ItemBinder;
import hivatec.ir.suradapter.ItemHolder;

public class TwoItemDataModel implements Comparable,ItemBinder {

    //TODO: Rename this class as necessary e.g- cars,movies,contacts

    public String date;
    public String details;

    //  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //	System.out.println(dateFormat.format(date));

    public TwoItemDataModel(String details,String date ) {
        this.date=date;
        this.details = details;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        TwoItemDataModel rhs = null;
        if(o instanceof TwoItemDataModel)
        {
            rhs=(TwoItemDataModel)o;
        }
        String[] thisDate ,rhsDate ;
        thisDate=date.split("-");
        rhsDate=rhs.date.split("-");
        int yearCheck=Integer.compare(Integer.parseInt(rhsDate[2].trim()) , Integer.parseInt(thisDate[2].trim()));

        if(yearCheck==1)return 1;
        else if(yearCheck==-1) return -1;
        else
        {
            int monthCheck=Integer.compare(Integer.parseInt(rhsDate[1].trim()) , Integer.parseInt(thisDate[1].trim()));

            if(monthCheck==1)return 1;
            else if(monthCheck==-1) return -1;
            else
            {
                int dateCheck=Integer.compare(Integer.parseInt(rhsDate[0].trim()) , Integer.parseInt(thisDate[0].trim()));
                if(dateCheck==1)return 1;
                else if(dateCheck==-1) return -1;
                else return 0;


            }


        }

    }

    @Override
    public String toString() {
        return details +"      " +date;
    }

    @Override
    public int getResourceId() {
        return R.layout.simple_recyclerview_2_items; //set your xml file id
    }

    @Override
    public void bindToHolder(ItemHolder itemHolder, Context context, Object o) {
        itemHolder.<TextView>find(R.id.item_recyclerview_tv1).setText(date);
        itemHolder.<TextView>find(R.id.item_recyclerview_tv2).setText(details);
    }
}
