package com.example.listecontacts3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Mes contacts"
            setDisplayHomeAsUpEnabled(false)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add(Menu.NONE, 101, Menu.NONE, "Ajouter")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == 101) {
                    val newId = System.currentTimeMillis()
                    val newContact = Contact(newId, "", "", "", "", null, null, null)
                    viewModel.addContact(newContact)
                    viewModel.selectContact(newId)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, DetailContactFragment())
                        .addToBackStack(null)
                        .commit()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = ContactAdapter(emptyList()) { contactId ->
            viewModel.selectContact(contactId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, DetailContactFragment())
                .addToBackStack(null)
                .commit()
        }

        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        viewModel.filteredContacts.observe(viewLifecycleOwner) { contacts ->
            adapter.updateData(contacts)
            if (contacts.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvContacts.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvContacts.visibility = View.VISIBLE
            }
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (etSearch.text.toString() != query) {
                etSearch.setText(query)
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
