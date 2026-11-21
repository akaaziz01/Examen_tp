package com.example.listecontacts3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactViewModel : ViewModel() {

    private val _contacts = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf(
            Contact(1L, "Jean", "Dupont", "0601020304", "jean.dupont@email.com", null, 48.8566, 2.3522),
            Contact(2L, "Marie", "Curie", "0605060708", "marie.curie@email.com", null, 43.6047, 1.4442),
            Contact(3L, "Pierre", "Martin", "0699887766", "pierre.martin@email.com", null, 45.764, 4.8357),
            Contact(4L, "Sophie", "Bernard", "0612345678", "sophie.bernard@email.com", null, null, null)
        )
    }
    val contacts: LiveData<MutableList<Contact>> = _contacts

    private val _filteredContacts = MutableLiveData<List<Contact>>().apply {
        value = _contacts.value
    }
    val filteredContacts: LiveData<List<Contact>> = _filteredContacts

    private val _selectedContactId = MutableLiveData<Long?>()
    val selectedContactId: LiveData<Long?> = _selectedContactId

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    fun selectContact(id: Long?) {
        _selectedContactId.value = id
    }

    fun getContact(id: Long): Contact? {
        return _contacts.value?.find { it.id == id }
    }

    fun updateContact(updatedContact: Contact) {
        val list = _contacts.value ?: return
        val index = list.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            list[index] = updatedContact
            _contacts.value = list
            filter(_searchQuery.value ?: "")
        }
    }

    fun addContact(contact: Contact) {
        val list = _contacts.value ?: mutableListOf()
        list.add(contact)
        _contacts.value = list
        filter(_searchQuery.value ?: "")
    }

    fun deleteContact(id: Long) {
        val list = _contacts.value ?: return
        list.removeAll { it.id == id }
        _contacts.value = list
        filter(_searchQuery.value ?: "")
    }

    fun filter(query: String) {
        _searchQuery.value = query
        val list = _contacts.value ?: emptyList()
        if (query.isBlank()) {
            _filteredContacts.value = list
        } else {
            val lowerQuery = query.lowercase().trim()
            _filteredContacts.value = list.filter {
                it.firstName.lowercase().contains(lowerQuery) ||
                        it.lastName.lowercase().contains(lowerQuery) ||
                        it.phoneNumber.contains(lowerQuery) ||
                        it.email.lowercase().contains(lowerQuery)
            }
        }
    }
}
