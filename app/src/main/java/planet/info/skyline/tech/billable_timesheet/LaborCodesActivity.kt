package planet.info.skyline.tech.billable_timesheet

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.gson.Gson
import org.json.JSONObject
import planet.info.skyline.R
import planet.info.skyline.RequestControler.MyAsyncTask
import planet.info.skyline.RequestControler.ResponseInterface
import planet.info.skyline.adapter.AdapterLaborCodes
import planet.info.skyline.crash_report.ConnectionDetector
import planet.info.skyline.model.LaborCode
import planet.info.skyline.network.Api
import planet.info.skyline.shared_preference.Shared_Preference
import planet.info.skyline.util.Utility


class LaborCodesActivity : AppCompatActivity(), ResponseInterface {

    lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapter: AdapterLaborCodes
    lateinit var context: Context
    lateinit var LaborCodeList: List<LaborCode>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labor_codes)
        context = this
        recyclerView = findViewById<RecyclerView>(R.id.rv_labor_codes_list)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        FetchLaborCodes()
    }

    private fun FetchLaborCodes() {
        val jsonObject = JSONObject()
        try {
            val dealerId = Shared_Preference.getDEALER_ID(this)
            val role = Shared_Preference.getUSER_ROLE(this)
            jsonObject.put("dealerID", dealerId)
            jsonObject.put("cat", role)
            jsonObject.put("type", "1")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (ConnectionDetector(context).isConnectingToInternet) {
            MyAsyncTask(this, true, this, Api.API_BILLABLE_NONBILLABLE_CODE, jsonObject).execute()
        } else {
            Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show()
        }
    }

    override fun handleResponse(responseStr: String?, api: String?) {
        if (api.equals(Api.API_BILLABLE_NONBILLABLE_CODE)) {
            val gson = Gson()
            LaborCodeList = gson.fromJson(responseStr, LaborCodeList::class.java) // work here

        }

    }
}
