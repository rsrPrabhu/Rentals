package com.example.rentals_g03.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentals_g03.databinding.ListingAdapterLayoutBinding
import com.example.rentals_g03.interfaces.ClickInterface
import com.example.rentals_g03.models.ListingModel

class ListingAdapter(val items:MutableList<ListingModel>, val clickI: ClickInterface) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListingAdapterLayoutBinding) : RecyclerView.ViewHolder (binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListingAdapterLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: ListingModel =  items.get(position)
        holder.binding.model.text = "${data.model} (${data.price})"
        holder.binding.address.text = "${data.address}"

        Glide.with(holder.itemView.context).load(data.imageUrl)
            .into(holder.binding.imageView)

       holder.binding.root.setOnClickListener {
           clickI.rowPressed(position)
       }
        holder.binding.buttonCancel.setOnClickListener {
            clickI.cancelPressed(position)
        }
    }
}