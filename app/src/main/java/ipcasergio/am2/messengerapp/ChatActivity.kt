@file:Suppress("DEPRECATION")

package ipcasergio.am2.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import ipcasergio.am2.messengerapp.AdapterClasses.ChatAdapter
import ipcasergio.am2.messengerapp.ModelClasses.Chat
import ipcasergio.am2.messengerapp.ModelClasses.Users
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {


    var userIDVisit: String = ""
    var firebaseUser : FirebaseUser? = null
    var chatAdapter : ChatAdapter? = null
    var mChatList : List<Chat>? = null
    lateinit var recyclerView_chat: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        intent = intent
        //mudei aqui adicionei tostring
        userIDVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerView_chat = findViewById(R.id.recyclerView_chat_view)
        recyclerView_chat.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView_chat.layoutManager = linearLayoutManager


        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIDVisit)
        reference.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }


            override fun onDataChange(p0: DataSnapshot) {

                val user : Users? = p0.getValue(Users::class.java)
                    username_chat.text = user!!.getUserName()
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile_img).into(profile_image_chat)

                    retrieveMessages(firebaseUser!!.uid, userIDVisit, user.getProfile())
            }


        })


        button_send_message_chat.setOnClickListener {
            val message = text_messege_chat.text.toString()
            if (message == ""){

                Toast.makeText(this@ChatActivity, "Please write a message, first", Toast.LENGTH_LONG).show()
            }
            else{

                sendMessageToUser(firebaseUser!!.uid, userIDVisit, message)
            }
            text_messege_chat.setText("")
        }

        button_send_image_file.setOnClickListener {
            val intent = Intent ()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),  438)
        }


    }




    // chat send message code
    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {

        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatLists")
                        .child(firebaseUser!!.uid)
                        .child(userIDVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }
                        override fun onDataChange(p0: DataSnapshot) {

                            if (!p0.exists()){
                                chatsListReference.child("id").setValue(userIDVisit)
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference.child("ChatLists")
                                .child(userIDVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)

                        }

                    })

                    // implement the notifications using fcm
                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)

                }
            }
    }

        //chat send message file/image code

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data!=null && data.data!=null){
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading , please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageID = ref.push().key
            val filePath = storageReference.child("$messageID.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful){

                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->

                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIDVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageID

                    ref.child("Chats").child(messageID!!).setValue(messageHashMap)

                    progressBar.dismiss()
                    
                }

            }
        }
    }
    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children ){
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(this@ChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    recyclerView_chat.adapter = chatAdapter
                }
            }


        })
    }
}
