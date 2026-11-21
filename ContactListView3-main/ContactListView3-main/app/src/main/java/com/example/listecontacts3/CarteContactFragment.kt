package com.example.listecontacts3

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment : Fragment(R.layout.fragment_carte_contact) {

    private val viewModel: ContactViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMapMessage = view.findViewById<TextView>(R.id.tvMapMessage)
        val contactId = viewModel.selectedContactId.value
        val contact = contactId?.let { viewModel.getContact(it) }

        val lat = contact?.latitude
        val lon = contact?.longitude

        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            tvMapMessage.visibility = View.VISIBLE
            tvMapMessage.text = "Coordonnées GPS absentes ou invalides pour ce contact"
            return
        }

        tvMapMessage.visibility = View.GONE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val position = LatLng(lat, lon)
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("${contact.firstName} ${contact.lastName}")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }
}
