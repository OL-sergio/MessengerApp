package ipcasergio.am2.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import ipcasergio.am2.messengerapp.ChatActivity
import ipcasergio.am2.messengerapp.ModelClasses.Users
import ipcasergio.am2.messengerapp.R

class UserAdapter (mContext: Context, mUsers: List<Users>, isChatCheck : Boolean) :
    RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNametTxt : TextView
        var profileImageView : CircleImageView
        var onlineImageView: CircleImageView
        var offlineImageView : CircleImageView
        var lastMessageTxt : TextView

        init {
            userNametTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)


        }
    }

    private  val mContext : Context
    private  val mUsers : List<Users>
    private  val isChatCheck : Boolean

    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder( viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view: View = LayoutInflater.from(mContext)
            .inflate(R.layout.user_search_item_layout, viewGroup, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {

        val user: Users = mUsers[i]
        holder.userNametTxt.text = user.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile_img).into(holder.profileImageView)

        //chat
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0 ){

                    val intent = Intent(mContext, ChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    // modei aqui arrayof
                    mContext.startActivities(arrayOf(intent))


                }
                if (position == 1 ){



                }


            })
            builder.show()
        }

    }






}