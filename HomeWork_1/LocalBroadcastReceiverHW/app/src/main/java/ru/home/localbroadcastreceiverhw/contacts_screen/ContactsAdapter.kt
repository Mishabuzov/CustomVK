package ru.home.localbroadcastreceiverhw.contacts_screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_contact.view.*
import ru.home.localbroadcastreceiverhw.Contact
import ru.home.localbroadcastreceiverhw.R
import ru.home.localbroadcastreceiverhw.widgets.DividerItemDecoration
import ru.home.localbroadcastreceiverhw.widgets.EmptyRecyclerView

class ContactsAdapter(private val recyclerView: EmptyRecyclerView, emptyView: View) :
    RecyclerView.Adapter<ContactsAdapter.ContactsHolder>() {

    private var contacts: List<Contact> = mutableListOf()

    init {
        recyclerView.adapter = this
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.emptyView = emptyView
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context))
    }

    internal fun refreshContacts(contacts: List<Contact>) {
        this.contacts = contacts
        refreshRecycler()
    }

    internal fun refreshRecycler() {
        notifyDataSetChanged()
        recyclerView.checkIfEmptyAndShow()
    }

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
            if (contact.phoneNumbers.isNullOrEmpty().not())
                phone_text_view.text = contact.phoneNumbers?.get(0)
        }
    }

}