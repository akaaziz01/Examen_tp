package com.example.listecontacts3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()
    private var currentContact: Contact? = null
    private var tempPhotoUri: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            tempPhotoUri = it.toString()
            view?.findViewById<ImageView>(R.id.ivDetailPhoto)?.setImageURI(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val uri = saveBitmapToCache(it)
            tempPhotoUri = uri?.toString()
            view?.findViewById<ImageView>(R.id.ivDetailPhoto)?.setImageBitmap(it)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            val imagePath = File(cachePath, "image.png")
            return FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                imagePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Détail du contact"
            setDisplayHomeAsUpEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add(Menu.NONE, 102, Menu.NONE, "Supprimer")
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == 102) {
                    currentContact?.let {
                        viewModel.deleteContact(it.id)
                        Toast.makeText(requireContext(), "Contact supprimé", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    return true
                } else if (menuItem.itemId == android.R.id.home) {
                    parentFragmentManager.popBackStack()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val ivPhoto = view.findViewById<ImageView>(R.id.ivDetailPhoto)
        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = view.findViewById<TextInputEditText>(R.id.etLastName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etLatitude = view.findViewById<TextInputEditText>(R.id.etLatitude)
        val etLongitude = view.findViewById<TextInputEditText>(R.id.etLongitude)
        val btnCall = view.findViewById<Button>(R.id.btnCall)
        val btnEmail = view.findViewById<Button>(R.id.btnEmail)
        val btnShowMap = view.findViewById<Button>(R.id.btnShowMap)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val contactId = viewModel.selectedContactId.value
        if (contactId != null) {
            currentContact = viewModel.getContact(contactId)
        }

        currentContact?.let { contact ->
            etFirstName.setText(contact.firstName)
            etLastName.setText(contact.lastName)
            etPhone.setText(contact.phoneNumber)
            etEmail.setText(contact.email)
            contact.latitude?.let { etLatitude.setText(it.toString()) }
            contact.longitude?.let { etLongitude.setText(it.toString()) }
            tempPhotoUri = contact.photoUri
            if (!contact.photoUri.isNullOrEmpty()) {
                try {
                    ivPhoto.setImageURI(Uri.parse(contact.photoUri))
                } catch (e: Exception) {
                    ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
                }
            } else {
                ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraLauncher.launch(null)
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnCall.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Numéro de téléphone vide", Toast.LENGTH_SHORT).show()
            }
        }

        btnEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Aucune application de messagerie disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Adresse e-mail vide", Toast.LENGTH_SHORT).show()
            }
        }

        btnShowMap.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, CarteContactFragment())
                .addToBackStack(null)
                .commit()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val latStr = etLatitude.text.toString().trim()
            val lonStr = etLongitude.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir les champs obligatoires (Prénom, Nom, Téléphone)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var latitude: Double? = null
            var longitude: Double? = null

            if (latStr.isNotEmpty()) {
                val lat = latStr.toDoubleOrNull()
                if (lat == null || lat < -90.0 || lat > 90.0) {
                    Toast.makeText(requireContext(), "Latitude invalide (-90 à 90)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                latitude = lat
            }

            if (lonStr.isNotEmpty()) {
                val lon = lonStr.toDoubleOrNull()
                if (lon == null || lon < -180.0 || lon > 180.0) {
                    Toast.makeText(requireContext(), "Longitude invalide (-180 à 180)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                longitude = lon
            }

            currentContact?.let { contact ->
                contact.firstName = firstName
                contact.lastName = lastName
                contact.phoneNumber = phone
                contact.email = email
                contact.photoUri = tempPhotoUri
                contact.latitude = latitude
                contact.longitude = longitude
                viewModel.updateContact(contact)
            }

            Toast.makeText(requireContext(), "Contact enregistré", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
