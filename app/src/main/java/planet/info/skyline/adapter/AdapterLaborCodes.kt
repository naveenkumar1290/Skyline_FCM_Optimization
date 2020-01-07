package planet.info.skyline.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import planet.info.skyline.R
import planet.info.skyline.model.LaborCode

class AdapterLaborCodes(val context:Context,val laborCodes:List<LaborCode>) : RecyclerView.Adapter<AdapterLaborCodes.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_vendor,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return laborCodes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.txtName.text = laborCodes.get(position).labor_name

    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtName: TextView = itemView!!.findViewById(R.id.txtvw_vendor)


    }
}