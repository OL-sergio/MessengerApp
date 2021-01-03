package ipcasergio.am2.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import ipcasergio.am2.messengerapp.ModelClasses.Users
import ipcasergio.am2.messengerapp.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*


@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    var usersReference : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       var view = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser= FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")


        usersReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {

                    var user  : Users? = p0.getValue(Users::class.java)


                    if (context!= null) {
                        view.user_name_profile_settings.text = user!!.getUserName()


                        // tem um erro no picasso
                       // Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        //Picasso.get().load(user.getCover()).into(view.cover_image_settings)

                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        view.profile_image_settings.setOnClickListener {
            pickImage()

        }

        view.cover_image_settings.setOnClickListener {
            coverChecker  = "cover"
            pickImage()

        }

        view.set_facebook.setOnClickListener {
            socialChecker = "Facebook"
            setSocialLinks()

        }
        view.set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()

        }
        view.set_website.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()

        }

        return view

    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if (socialChecker == "website"){

            builder.setTitle("Write URL:")
        }
        else{
            builder.setTitle("Write username:")
        }
        val editText = EditText(context)

        if (socialChecker == "website"){

            editText.hint ="e.g www.google.com"
        }
        else{
            editText.hint ="e.g sergio.oliveira.3538"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, witch ->
            val str = editText.text.toString()
            if (str == ""){
                Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()
            }
            else{
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{
                dialog, witch ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()

        when(socialChecker){
            
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }

            "instagram" ->{
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }

            "website" ->{
                mapSocial["website"] = "https://$str"
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener{ task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context, "updated Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && requestCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data

            Toast.makeText(context, "uploading...", Toast.LENGTH_LONG).show()
            uploadImage()
        }
    }

    private fun uploadImage() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading , please wait...")
        progressBar.show()

        if (imageUri!=null){
          val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful){

                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover"){
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    }else{
                        val mapProfileIng = HashMap<String, Any>()
                        usersReference!!.updateChildren(mapProfileIng)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }

            }


        }
    }
}