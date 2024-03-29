package ipcasergio.am2.messengerapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ipcasergio.am2.messengerapp.AdapterClasses.UserAdapter
import ipcasergio.am2.messengerapp.ModelClasses.ChatList
import ipcasergio.am2.messengerapp.ModelClasses.Users
import ipcasergio.am2.messengerapp.R


class ChatFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var usersChatList : List<ChatList>? = null

    lateinit var recycler_view_chatlist: RecyclerView
    private var firebaseUser : FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recycler_view_chatlist = view.findViewById(R.id.recyclerView_view_chatlist)
        recycler_view_chatlist.setHasFixedSize(true)
        recycler_view_chatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatLists")
            .child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for (dataSnapshot in p0.children){

                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatList!!)

                }
                retrieveChatList()
            }

        })


        return view
    }

    private fun retrieveChatList(){

        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {


            }


            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for (dataSnapshot in p0.children){

                    val user = dataSnapshot.getValue(Users::class.java)

                        for (eachChatList in usersChatList!!)

                            if (user!!.getUID().equals(eachChatList.getId())){

                                (mUsers as ArrayList).add(user)

                             }
                        }
                        userAdapter = UserAdapter(context!!,(mUsers as ArrayList<Users>), true)
                        recycler_view_chatlist.adapter = userAdapter

                    }

                })
            }
}