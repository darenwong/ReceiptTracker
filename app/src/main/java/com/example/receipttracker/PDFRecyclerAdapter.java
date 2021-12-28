package com.example.receipttracker;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PDFRecyclerAdapter extends RecyclerView.Adapter<PDFRecyclerAdapter.MyViewHolder>{

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<Receipt> receiptList;


    public PDFRecyclerAdapter(Context context,ArrayList<Receipt> receiptList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.receiptList = receiptList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_marks, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MMM-yyyy");
        holder.no.setText(Integer.toString(position+1));
        holder.date.setText(fmtOut.format(receiptList.get(position).getDate()));
        holder.title.setText(receiptList.get(position).getTitle());
        holder.category.setText(receiptList.get(position).getCategory());
        holder.currency.setText(receiptList.get(position).getCurrency());
        holder.amount.setText(receiptList.get(position).getAmount().toString());
        holder.note.setText(receiptList.get(position).getNote());
    }

    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView no;
        TextView date;
        TextView title;
        TextView category;
        TextView currency;
        TextView amount;
        TextView note;


        public MyViewHolder(View itemView) {
            super(itemView);
            no = (TextView) itemView.findViewById(R.id.txt_no_recy);
            date = (TextView) itemView.findViewById(R.id.txt_date_recy);
            title = (TextView) itemView.findViewById(R.id.txt_title_recy);
            category = (TextView) itemView.findViewById(R.id.txt_category_recy);
            currency = (TextView) itemView.findViewById(R.id.txt_currency_recy);
            amount = (TextView) itemView.findViewById(R.id.txt_amount_recy);
            note = (TextView) itemView.findViewById(R.id.txt_note_recy);
        }
    }
}
