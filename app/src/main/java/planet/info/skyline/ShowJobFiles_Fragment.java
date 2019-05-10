package planet.info.skyline;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.util.FileDownloader;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.util.Utility.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.URL_EP2;


public class ShowJobFiles_Fragment extends Fragment {

    ListView jobs_list_View;
    View rootView;
    String jobtxt_id, tab;
    TextView tv_msg;

    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> map;

    ArrayList<HashMap<String, String>> data1;
    HashMap<String, String> map1;
    AlertDialog alertDialog;
    SwipeRefreshLayout pullToRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_fragment__show_job_files, container, false);
        jobs_list_View = (ListView) rootView.findViewById(R.id.cart_listview);
        tv_msg = (TextView) rootView.findViewById(R.id.tv_msg);


        pullToRefresh = rootView.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
                    new get_jobFiles_Acyntask().execute();
                } else {
                    Toast.makeText(getActivity(), Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
                }
                // your code

            }
        });


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            jobtxt_id = bundle.getString("jobtxt_id", "");
            tab = bundle.getString("tab", "");
        }


        if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
            new get_jobFiles_Acyntask().execute();
        } else {
            Toast.makeText(getActivity(), Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
        }


        return rootView;
    }

    public void fettch_job_list() {

        data = new ArrayList<>();

        int index = 1;
        int count = 0;
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + "GetJobFileByTextJob11";
        final String METHOD_NAME = "GetJobFileByTextJob11";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("job", jobtxt_id);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int j = 0; j < ks.getPropertyCount(); j++) {
                ks.getProperty(j);
            }
            String recved = ks.toString();
            if (recved.contains("Job not found")) {
                count = 1;
            } else {
                recved = recved.substring(recved.indexOf("=") + 1, recved.lastIndexOf(";"));
                JSONObject jsonObject = new JSONObject(recved);
/*
                String[] aa = recved.split("=");
                String a = aa[1];
                String b[] = a.split(";");
                String k = b[0];
                JSONObject jsonObject = new JSONObject(k);*/
                JSONArray jsonArray = jsonObject.getJSONArray("cds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        map = new HashMap<>();
                        map.clear();
                        index = 1 + i;
                        String index1 = Integer.toString(index);
                        String clientnme = jsonObject1.getString("clientnme");
                        String txt_Job = jsonObject1.getString("txt_Job");
                        String int_job_file_id = jsonObject1.getString("int_job_file_id");
                        String int_job_id = jsonObject1.getString("int_job_id");
                        String var_file_name1 = jsonObject1.getString("var_file_name1");
                        String var_file_name = jsonObject1.getString("var_file_name");
                        String var_file_des = jsonObject1.getString("var_file_des");
                        String int_rndm_id_file = jsonObject1.getString("int_rndm_id_file");
                        String dt_file_Udate = jsonObject1.getString("dt_file_Udate");

                        map.put("index1", index1);
                        map.put("clientnme", clientnme);
                        map.put("txt_Job", txt_Job);
                        map.put("int_job_file_id", int_job_file_id);
                        map.put("int_job_id", int_job_id);
                        map.put("var_file_name1", var_file_name1);
                        map.put("var_file_name", var_file_name);
                        map.put("var_file_des", var_file_des);
                        map.put("int_rndm_id_file", int_rndm_id_file);
                        map.put("dt_file_Udate", dt_file_Udate);
                        map.put("isSelected", "false");
                        data.add(map);
                    } catch (Exception e) {
                        e.getMessage();
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fettch_job_by_Client_list()//methord for job by client
    {
        data1 = new ArrayList<>();

        int index = 1;
        //  count = 0;
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + "Bind_Job_client11";
        final String METHOD_NAME = "Bind_Job_client11";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("job", jobtxt_id);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int j = 0; j < ks.getPropertyCount(); j++) {
                ks.getProperty(j);
            }
            String recved = ks.toString();
            if (recved.contains("No file Available.")) {
                //   count = 1;

            } else {
                recved = recved.substring(recved.indexOf("=") + 1, recved.lastIndexOf(";"));
                JSONObject jsonObject = new JSONObject(recved);

             /* String[] aa = recved.split("=");
                String a = aa[1];
                String b[] = a.split(";");
                String k = b[0];
                JSONObject jsonObject = new JSONObject(k);*/

                JSONArray jsonArray = jsonObject.getJSONArray("cds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    map1 = new HashMap<>();
                    map1.clear();
                    index = 1 + i;
                    String index1 = Integer.toString(index);
                    String clientnme = jsonObject1.getString("Source");
                    String txt_Job = jsonObject1.getString("txt_C_Name");
                    String int_job_file_id = jsonObject1.getString("INT_FID_client");
                    String int_job_id = jsonObject1.getString("job_id_client");
                    String var_file_name1 = jsonObject1.getString("TXT_Filename_client");
                    String var_file_name = jsonObject1.getString("DTR_SubmitDate_client");
                    String var_file_des = jsonObject1.getString("VCHAR_Heading_client");

                    map1.put("index1", index1);
                    map1.put("clientnme", clientnme);
                    map1.put("txt_Job", txt_Job);
                    map1.put("int_job_file_id", int_job_file_id);
                    map1.put("int_job_id", int_job_id);
                    map1.put("var_file_name1", var_file_name1);
                    map1.put("var_file_name", var_file_name);
                    map1.put("var_file_des", var_file_des);
                    data1.add(map1);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void download_file(String file_name1) {
        if ((file_name1.contains(".jpg")) || (file_name1.contains(".JPG"))
                || (file_name1.contains(".jpeg")) || (file_name1.contains(".JPEG"))
                || (file_name1.contains(".png")) || (file_name1.contains(".PNG"))
                || (file_name1.contains(".bmp")) || (file_name1.contains(".BMP"))
                || (file_name1.contains(".gif")) || (file_name1.contains(".GIF"))
                || (file_name1.contains(".webp")) || (file_name1.contains(".WEBP"))
                ) {
            if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
                new Download_images().execute(URL_EP2 + "/upload/" + file_name1, file_name1);
            } else {
                Toast.makeText(getActivity(), Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Toast.makeText(Show_Jobs_Activity.this, "No pdf file Avalable", Toast.LENGTH_SHORT).show();
            if (new ConnectionDetector(getActivity()).isConnectingToInternet()) {
                new DownloadFile().execute(URL_EP2 + "/upload/" + file_name1, file_name1);
            } else {
                Toast.makeText(getActivity(), Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void view_file(String file_name1) {
        if ((file_name1.contains(".jpg")) || (file_name1.contains(".JPG"))
                || (file_name1.contains(".jpeg")) || (file_name1.contains(".JPEG"))
                || (file_name1.contains(".png")) || (file_name1.contains(".PNG"))
                || (file_name1.contains(".bmp")) || (file_name1.contains(".BMP"))
                || (file_name1.contains(".gif")) || (file_name1.contains(".GIF"))
                || (file_name1.contains(".webp")) || (file_name1.contains(".WEBP"))
                ) {
            Intent i = new Intent(getActivity(), FullscreenImageView.class);
            i.putExtra("url", URL_EP2 + "/upload/" + file_name1);
            startActivity(i);

/*
            Intent i=new Intent(getActivity(),ZoomInZoomOut.class);
            i.putExtra("url", URL_EP2+"/upload/" + file_name1);
            startActivity(i);
*/
            /*final Dialog d = new Dialog(getActivity());
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.popup_new);
            d.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ImageView image = (ImageView) d.findViewById(R.id.popup_image);///for open a image
            ImageView close = (ImageView) d.findViewById(R.id.imageButton3);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            Picasso.with(getActivity()).load( URL_EP2+"/upload/" + file_name1).into(image);
            d.show();*/
        } else if ((file_name1.contains(".doc")) || (file_name1.contains(".DOC"))
                || (file_name1.contains(".psd")) || (file_name1.contains(".PSD"))
                || (file_name1.contains(".docx")) || (file_name1.contains(".DOCX"))
                || (file_name1.contains(".pdf")) || (file_name1.contains(".PDF"))
                ) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://docs.google.com/gview?embedded=true&url=" + URL_EP2 + "/upload/" + file_name1));
            startActivity(browserIntent);
        } else if ((file_name1.contains(".aac")) || (file_name1.contains(".AAC"))
                || (file_name1.contains(".m4a")) || (file_name1.contains(".M4A"))
                || (file_name1.contains(".mp4")) || (file_name1.contains(".MP4"))
                || (file_name1.contains(".3gp")) || (file_name1.contains(".3GP"))
                || (file_name1.contains(".m4b")) || (file_name1.contains(".M4B"))
                || (file_name1.contains(".mp3")) || (file_name1.contains(".MP3"))
                || (file_name1.contains(".wave")) || (file_name1.contains(".WAVE"))
                ) {
            /*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://exhibitpower2.com/upload/" + file_name1));
            startActivity(browserIntent);*/
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_EP2 + "/upload/" + file_name1));
            intent.setDataAndType(Uri.parse(URL_EP2 + "/upload/" + file_name1), "video/*");
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Unrecognized file format ! Please download to view the file!", Toast.LENGTH_SHORT).show();
        }
    }

  /*  public List<HashMap<String, String>> getProjectPhotoList() {
        return list_ProjectPhotos;
    }

    public void setProjectPhotoList(List<HashMap<String, String>> list) {
        list_ProjectPhotos = list;
    }*/

    class get_jobFiles_Acyntask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDoalog;

        @Override
        protected Void doInBackground(Void... params) {


            if (tab.equalsIgnoreCase("0")) {

                fettch_job_list();
            } else if (tab.equalsIgnoreCase("1")) {

                fettch_job_by_Client_list();

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDoalog = new ProgressDialog(getActivity());
            progressDoalog.setMessage("Please wait....");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.setCancelable(false);
            progressDoalog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                progressDoalog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }

            order_adapter adapter = null;
            Show_client_Adapter adapter1 = null;
            if (tab.equalsIgnoreCase("0")) {
                if (data.size() < 1) {
                    tv_msg.setVisibility(View.VISIBLE);
                } else {
                    tv_msg.setVisibility(View.GONE);
                }
                adapter = new order_adapter(getActivity(), data);
                jobs_list_View.setAdapter(adapter);


            } else if (tab.equalsIgnoreCase("1")) {
                if (data1.size() < 1) {
                    tv_msg.setVisibility(View.VISIBLE);
                } else {
                    tv_msg.setVisibility(View.GONE);
                }
                adapter1 = new Show_client_Adapter(getActivity(), data1);
                jobs_list_View.setAdapter(adapter1);
            }

            if (pullToRefresh.isRefreshing()) {
                pullToRefresh.setRefreshing(false);
            }
        }
    }

    public class order_adapter extends BaseAdapter {
        List<HashMap<String, String>> beanArrayList;
        Context context;
        int count = 1;

        public order_adapter(Context context, List<HashMap<String, String>> beanArrayList) {
            this.context = context;
            this.beanArrayList = beanArrayList;

        }

        @Override
        public int getCount() {
            return beanArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertview, ViewGroup viewGroup) {

            final Holder holder;
            final String index1 = beanArrayList.get(i).get("index1");
            final String clientnme = beanArrayList.get(i).get("clientnme");
            final String txt_Job = beanArrayList.get(i).get("txt_Job");
            final String job_file_id = beanArrayList.get(i).get("int_job_file_id");
            final String job_id = beanArrayList.get(i).get("int_job_id");
            final String file_name1 = beanArrayList.get(i).get("var_file_name1");
            final String file_name = beanArrayList.get(i).get("var_file_name");
            final String file_des = beanArrayList.get(i).get("var_file_des");
            final String rndm_id_file = beanArrayList.get(i).get("int_rndm_id_file");
            final String file_Udate = beanArrayList.get(i).get("dt_file_Udate");
            final String isSelected = beanArrayList.get(i).get("isSelected");


            if (convertview == null) {
                holder = new Holder();

                convertview = LayoutInflater.from(context).inflate(R.layout.row_job_files_new, null);
                holder.index_no = (Button) convertview.findViewById(R.id.serial_no);
                holder.Source = (TextView) convertview.findViewById(R.id.source);
                holder.user_name = (TextView) convertview.findViewById(R.id.user);
                holder.file_upload = (TextView) convertview.findViewById(R.id.flie_upload);
                holder.date_upload = (TextView) convertview.findViewById(R.id.date_upload);
                holder.view_file = (Button) convertview.findViewById(R.id.view_file);
                holder.download_file = (Button) convertview.findViewById(R.id.download_file);
                holder.row_jobFile = (LinearLayout) convertview.findViewById(R.id.row_jobFile);
                holder.checkBox = (CheckBox) convertview.findViewById(R.id.checkBox);


                convertview.setTag(holder);
            } else {
                holder = (Holder) convertview.getTag();
            }
            holder.index_no.setText(index1);

            if (txt_Job.equals(null) || txt_Job.trim().equals("")) {
                holder.Source.setText("Not available");
            } else {
                holder.Source.setText(txt_Job);

                if (txt_Job.equalsIgnoreCase("Project Photo(s)")) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                } else {
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }
            }

            if (clientnme.equals(null) || clientnme.trim().equals("")) {
                holder.user_name.setText("Not available");
            } else {
                holder.user_name.setText(clientnme);
            }


            holder.checkBox.setTag(i);
            if (isSelected.equalsIgnoreCase("true")) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag(); // Here

                    HashMap<String, String> hashMap = beanArrayList.get(getPosition);
                    if (isChecked) {
                        hashMap.put("isSelected", "true");
                    } else {
                        hashMap.put("isSelected", "false");
                    }

                    beanArrayList.set(getPosition, hashMap);
                    ((Show_Jobs_Activity_New) getActivity()).setProjectPhotoList(beanArrayList);


                }
            });

//nks

            if (file_des.equals(null) || file_des.trim().equals("")) {
                holder.file_upload.setText("Not available");
            } else {
                holder.file_upload.setText(file_des.trim());
            }

            //holder.file_upload.setText(file_name);
            //nks

            if (file_Udate.equals(null) || file_Udate.trim().equals("")) {
                holder.date_upload.setText("Not available");
            } else {
                holder.date_upload.setText(file_Udate);
            }

            holder.view_file.setText("View");
            holder.download_file.setText("Download");

            holder.download_file.setOnClickListener(new View.OnClickListener() {    ///for download a pdf
                @Override
                public void onClick(View view) {

                    download_file(file_name1);


                }
            });


            holder.view_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view_file(file_name1);


                }
            });


            holder.row_jobFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   /* new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Please select the action !")
                            .setContentText("Press Back to cancel !")
                            .setConfirmText("View File")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    view_file(file_name1);
                                }
                            })
                            .setCancelText("Download File")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                    download_file(file_name1);
                                }
                            })
                            .show();*/


                }
            });

            return convertview;
        }


        class Holder {
            TextView Source, user_name, file_upload, date_upload;

            //nks
            Button index_no;
            Button view_file, download_file;
            LinearLayout row_jobFile;
            CheckBox checkBox;

            //nks
        }


    }

    public class Show_client_Adapter extends BaseAdapter {
        List<HashMap<String, String>> beanArrayList;
        Context context;
        int count = 1;

        public Show_client_Adapter(Context context, List<HashMap<String, String>> beanArrayList) {
            this.context = context;
            this.beanArrayList = beanArrayList;
        }

        @Override
        public int getCount() {
            return beanArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertview, ViewGroup viewGroup) {

            final Holder holder;
            final String index1 = beanArrayList.get(i).get("index1");
            final String clientnme = beanArrayList.get(i).get("clientnme");
            final String txt_Job = beanArrayList.get(i).get("txt_Job");
            final String job_file_id = beanArrayList.get(i).get("int_job_file_id");
            final String job_id = beanArrayList.get(i).get("int_job_id");
            final String file_name1 = beanArrayList.get(i).get("var_file_name1");
            final String file_name = beanArrayList.get(i).get("var_file_name");
            final String file_des = beanArrayList.get(i).get("var_file_des");


            if (convertview == null) {
                holder = new Holder();
                convertview = LayoutInflater.from(context).inflate(R.layout.row_job_files_new, null);


                holder.index_no = (Button) convertview.findViewById(R.id.serial_no);
                holder.Source = (TextView) convertview.findViewById(R.id.source);
                holder.user_name = (TextView) convertview.findViewById(R.id.user);
                holder.file_upload = (TextView) convertview.findViewById(R.id.flie_upload);
                holder.date_upload = (TextView) convertview.findViewById(R.id.date_upload);
                holder.view_file = (Button) convertview.findViewById(R.id.view_file);
                holder.download_file = (Button) convertview.findViewById(R.id.download_file);
                //
                //nks
                holder.row_jobFile = (LinearLayout) convertview.findViewById(R.id.row_jobFile);
                //nks
                holder.checkBox = (CheckBox) convertview.findViewById(R.id.checkBox);
                holder.checkBox.setVisibility(View.GONE);
                //
                convertview.setTag(holder);
            } else {
                holder = (Holder) convertview.getTag();
            }
            holder.index_no.setText(index1);
            if (clientnme.equals(null) || clientnme.trim().equals("")) {
                holder.Source.setText("Not available");
            } else {
                holder.Source.setText(clientnme);
            }
            if (txt_Job.equals(null) || txt_Job.trim().equals("")) {
                holder.user_name.setText("Not available");
            } else {
                holder.user_name.setText(txt_Job);
            }
            //nks
            // holder.file_upload.setText(file_name1);
            if (file_des.equals(null) || file_des.trim().equals("")) {
                holder.file_upload.setText("Not available");
            } else {
                holder.file_upload.setText(file_des.trim());
            }

            if (file_name.equals(null) || file_name.trim().equals("")) {
                holder.date_upload.setText("Not available");
            } else {
                holder.date_upload.setText(file_name);
            }
            //nks

            holder.view_file.setText("View");
            holder.download_file.setText("Download");

            holder.download_file.setOnClickListener(new View.OnClickListener() {    ///for download a pdf
                @Override
                public void onClick(View view) {

                    download_file(file_name1);


                }
            });


            holder.view_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    view_file(file_name1);

                }
            });
            holder.row_jobFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    final View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
                    dialogView.setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialogBuilder.setView(dialogView);
                    final TextView title = dialogView.findViewById(R.id.textView1rr);
                    final  TextView message = dialogView.findViewById(R.id.texrtdesc);

                    final Button positiveBtn = dialogView.findViewById(R.id.Btn_Yes);
                    final Button negativeBtn = dialogView.findViewById(R.id.Btn_No);
                    ImageView close = (ImageView) dialogView.findViewById(R.id.close);
                    close.setVisibility(View.INVISIBLE);
                    // dialogBuilder.setTitle("Device Details");
                    title.setText("Please select the action!");
                    message.setText("Press Back to cancel!");
                    positiveBtn.setText("View");
                    negativeBtn.setText("Download");
                    negativeBtn.setVisibility(View.VISIBLE);
                    positiveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            view_file(file_name1);
                        }
                    });
                    negativeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            download_file(file_name1);
                        }
                    });
                    alertDialog = dialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.show();




                    /*new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Please select the action !")
                            .setContentText("Press Back to cancel !")
                            .setConfirmText("View File")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    view_file(file_name1);
                                }
                            })
                            .setCancelText("Download File")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                    download_file(file_name1);
                                }
                            })
                            .show();*/












                }
            });


            return convertview;
        }


        class Holder {
            TextView Source, user_name, file_upload, date_upload;
            Button index_no;
            Button view_file, download_file;
            LinearLayout row_jobFile;
            CheckBox checkBox;
        }


    }

    class Download_images extends AsyncTask<String, Void, String> {///this class make in adapter for downloading the images

        ProgressDialog progressDoalog;

        @Override
        protected String doInBackground(String... strings) {
            String fileUrl = strings[0];
            String fileName = strings[1];
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "Exhibit Power");

            if (!folder.exists()) {
                folder.mkdir();
            }

            File folder1 = new File(folder, "Download");
            if (!folder1.exists()) {
                folder1.mkdir();
            }


            File pdfFile = new File(folder1, fileName);
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            FileDownloader.download_images(fileUrl, pdfFile);
            return pdfFile.getAbsolutePath();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDoalog = new ProgressDialog(getActivity());


            progressDoalog.setMessage("Downloading please wait....");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            progressDoalog.dismiss();
            //  Toast.makeText(Show_Jobs_Activity.this, "File downloaded successfully !", Toast.LENGTH_SHORT).show();

            try {
                Toast.makeText(getActivity(), "File downloaded successfully !" +

                        "  " + "Location:" + path, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.getMessage();
            }
        }

    }

    class DownloadFile extends AsyncTask<String, Void, String> {///this class make in adapter for downloading the pdf
        ProgressDialog progressDoalog;

        @Override
        protected String doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            /**/

            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "Exhibit Power");

            if (!folder.exists()) {
                folder.mkdir();
            }

            File folder1 = new File(folder, "Download");
            if (!folder1.exists()) {
                folder1.mkdir();
            }


            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.indexOf("/") + 1);
            }
            File pdfFile = new File(folder1, fileName);
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            FileDownloader.downloadFile(fileUrl, pdfFile);


            /**/
           /* String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "Skyline");
            folder.mkdir();
            File pdfFile = new File(folder, fileName);
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);*/
            return folder1.getAbsolutePath();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showDialog(progress_bar_type);

            progressDoalog = new ProgressDialog(getActivity());
            progressDoalog.setMessage("Downloading please wait...");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            progressDoalog.dismiss();
            try {
                Toast.makeText(getActivity(), "File downloaded successfully !" +
                        "  " + "Location:" + path, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.getMessage();
            }
        }


    }
}
