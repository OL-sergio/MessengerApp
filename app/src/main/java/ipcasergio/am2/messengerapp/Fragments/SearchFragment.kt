package ipcasergio.am2.messengerapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ipcasergio.am2.messengerapp.AdapterClasses.UserAdapter
import ipcasergio.am2.messengerapp.ModelClasses.Users
import ipcasergio.am2.messengerapp.R


class SearchFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var recyclerView : RecyclerView? = null
    private var searchEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
   val view : View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.search_Users)

        mUsers = ArrayList()
        retrieveAllUsers()

        searchEditText!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(cs.toString().toLowerCase())
            }


        })

        return view
}
    private  fun retrieveAllUsers(){
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")


        refUsers.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
               if (searchEditText!!.text.toString() == ""){
                   for (snapshot in p0.children ){

                       val user = snapshot.getValue(Users::class.java)
                       if (!(user!!.getUID()).equals(firebaseUserID)){

                           (mUsers as ArrayList<Users>).add(user)
                       }
                   }
                   userAdapter = UserAdapter(context!!, mUsers!!, false)
                   recyclerView!!.adapter = userAdapter
               }
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        }
        )
    }
    private fun searchForUsers(str: String){

        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                for (snapshot in p0.children ){
                    val user = snapshot.getValue(Users::class.java)
                    if (!(user!!.getUID().equals(firebaseUserID))){

                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = userAdapter
            }



        })

    }

}