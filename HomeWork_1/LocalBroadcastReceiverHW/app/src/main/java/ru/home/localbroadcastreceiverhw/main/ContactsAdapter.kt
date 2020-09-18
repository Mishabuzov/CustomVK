package ru.home.localbroadcastreceiverhw.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_contact.view.*
import ru.home.localbroadcastreceiverhw.Contact
import ru.home.localbroadcastreceiverhw.R

class ContactsAdapter(private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ContactsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder =
        ContactsHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        )

    override fun onBindViewHolder(holder: ContactsHolder, position: Int) =
        holder.bind(contacts[position])

    override fun getItemCount(): Int = contacts.size

    class ContactsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Contact) = with(itemView) {
            name_text_view.text = contact.name
            phone_text_view.text = contact.phoneNumbers?.get(0)
        }
    }

}