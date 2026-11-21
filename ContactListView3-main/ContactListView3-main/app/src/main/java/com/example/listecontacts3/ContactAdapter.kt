package com.example.listecontacts3

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivContactPhoto)
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = "${contact.firstName} ${contact.lastName}"
        holder.tvPhone.text = contact.phoneNumber

        if (!contact.photoUri.isNullOrEmpty()) {
            try {
                holder.ivPhoto.setImageURI(Uri.parse(contact.photoUri))
            } catch (e: Exception) {
                holder.ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }

        holder.itemView.setOnClickListener {
            onClick(contact.id)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
