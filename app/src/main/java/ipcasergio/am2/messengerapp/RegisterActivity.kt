package ipcasergio.am2.messengerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

@Suppress("NAME_SHADOWING")
class RegisterActivity : AppCompatActivity() {


    private lateinit var  mAuth: FirebaseAuth
    private lateinit var  refUsers : DatabaseReference
    private var firebaseUserID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val toolbar : Toolbar = findViewById(R.id.toolbar_register)

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener{
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        button_register.setOnClickListener {
            registerUer()

        }
    }

    @SuppressLint("DefaultLocale")
    private fun registerUer() {

        val username : String = editText_username_register.text.toString()
        val email : String = editText_email_register.text.toString()
        val password : String = editText_password_register.text.toString()

        if (username == ""){
            Toast.makeText(this@RegisterActivity, "Please write username.", Toast.LENGTH_SHORT).show()
        }
        else if (email == ""){
            Toast.makeText(this@RegisterActivity, "Please write email.", Toast.LENGTH_SHORT).show()
        }
        else if (password == ""){
            Toast.makeText(this@RegisterActivity, "Please write password.", Toast.LENGTH_SHORT).show()
        }
        else{
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){

                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messegerapp-d957e.appspot.com/o/profile-icons-user.jpeg?alt=media&token=428006b0-d8c8-4295-aa67-4a955146b2c5"
                        userHashMap["cover"]   = "https://firebasestorage.googleapis.com/v0/b/messegerapp-d957e.appspot.com/o/profile-imag.jfif?alt=media&token=f7a52a32-8169-4061-b0f5-d3f4552fb5cc"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){

                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()

                                }
                            }

                    }else{
                        Toast.makeText(this@RegisterActivity, "Error Message:" + task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}