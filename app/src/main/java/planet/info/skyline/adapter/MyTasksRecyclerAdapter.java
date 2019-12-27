package planet.info.skyline.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import planet.info.skyline.R;
import planet.info.skyline.model.MyTask;
import planet.info.skyline.model.TaskPlan;
import planet.info.skyline.tech.task_plan.ChecklistActivity;
import planet.info.skyline.tech.task_plan.CompleteTaskActivity;
import planet.info.skyline.tech.task_plan.CompletedTaskDetailActivity;

import planet.info.skyline.util.Utility;

public class MyTasksRecyclerAdapter extends RecyclerView.Adapter<MyTasksRecyclerAdapter.MyViewHolder> {

    List<MyTask> List_myTask;
    Context context;


    public MyTasksRecyclerAdapter(Context context, List<MyTask> myTasks) {
        this.context = context;
        this.List_myTask = myTasks;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_my_task, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int i) {


        final String checklist = List_myTask.get(i).getSelectedchklst() + "/" + List_myTask.get(i).getTotalchklst();
        final String completedDate = List_myTask.get(i).getComletedDate1();
        final String deadlineDate = List_myTask.get(i).getDeadlineDate();
        final String description = String.valueOf(List_myTask.get(i).getDescription());
        final String startBy = List_myTask.get(i).getStartDate();
        final String task = List_myTask.get(i).getTask();
        final String timeBudget = List_myTask.get(i).getTimeBudget();
        final String technician = List_myTask.get(i).getAssignUser();
        final String completedBy = List_myTask.get(i).getCName();
        final String taskID = List_myTask.get(i).getDTaskID();
        final String ID = String.valueOf(List_myTask.get(i).getIDPK());
        final String AssignUser = List_myTask.get(i).getAssignUser();

        final String JobName = List_myTask.get(i).getTxtJob();
        final String JobDesc = "";
        final String Client =String.valueOf( List_myTask.get(i).getTxtCName());


      /*  String day_ = Utility.getDayFromDate(completedDate, "yyyy-MM-dd");
        if (day_ != null) {

            holder.day.setText(day_);
        }*/



    //    holder.view1.setBackgroundColor(context.getResources().getColor(R.color.green_material2));
        holder.index_no.setText(String.valueOf(i + 1));
        if (Client == null || Client.trim().equals("")) {
            holder.Client.setText("Not available");
        } else {
            holder.Client.setText(Client);

        }
        if (JobName == null || JobName.trim().equals("")) {
            holder.JobName.setText("Not available");
        } else {
            holder.JobName.setText(JobName);
        }

        if (JobDesc == null || JobDesc.trim().equals("")) {
            holder.Job_desc.setText("Not available");
        } else {
            holder.Job_desc.setText(JobDesc);
        }
        if (task == null || task.trim().equals("")) {
            holder.Task.setText("Not available");
        } else {
            holder.Task.setText(Utility.TrimString(task, 25));
        }

        if (timeBudget == null || timeBudget.trim().equals("")) {
            holder.Hours.setText("Not available");
        } else {
            holder.Hours.setText(timeBudget);
        }

        if (startBy == null || startBy.trim().equals("")) {
            holder.Start_date.setText("Not available");
        } else {
            holder.Start_date.setText(Utility.ChangeDateFormat("yyyy-MM-dd", "dd MMM yyyy", startBy.trim()));
        }
        if (completedDate == null || completedDate.trim().equals("")) {
            holder.Due_date.setText("Not available");
        } else {
            holder.Due_date.setText(Utility.ChangeDateFormat("yyyy-MM-dd", "dd MMM yyyy", completedDate.trim()));
        }

        if (technician == null || technician.trim().equals("")) {
            holder.Secondary_Tech.setText("Not available");
        } else {
            holder.Secondary_Tech.setText(technician);
        }



        holder.parent_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* Intent i = new Intent(context, CompletedTaskDetailActivity.class);
                i.putExtra("checklist", checklist);
                i.putExtra("completedDate", completedDate);
                i.putExtra("deadlineDate", deadlineDate);
                i.putExtra("description", description);
                i.putExtra("startBy", startBy);
                i.putExtra("task", task);
                i.putExtra("timeBudget", timeBudget);
                i.putExtra("technician", technician);
                i.putExtra("completedBy", completedBy);
                i.putExtra("taskID", taskID);
                i.putExtra("job_id", "");
                i.putExtra("id", ID);
                i.putExtra("AssignUser", AssignUser);
                 context.startActivity(i);
*/
                Intent intent = new Intent(context, ChecklistActivity.class);
                intent.putExtra("Updatable", "1");
                intent.putExtra("RequestCode", Utility.REQUEST_CODE_CHECKLIST);
                intent.putExtra("job_id",  List_myTask.get(i).getJobIDPK());
                intent.putExtra("task_id", taskID);
                intent.putExtra("id",  List_myTask.get(i).getIDPK());
                ((Activity) context).startActivityForResult(intent, Utility.REQUEST_CODE_CHECKLIST);// Activity is started with requestCode 2

            }
        });

    }

    @Override
    public int getItemCount() {
        return List_myTask.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView Task,Client,JobName,Job_desc,Hours,Secondary_Tech;
        TextView Start_date, Due_date;
        Button index_no;
        LinearLayout parent_row;
        View view1;


        public MyViewHolder(View convertview) {
            super(convertview);

            index_no = (Button) convertview.findViewById(R.id.serial_no);
            Task = (TextView) convertview.findViewById(R.id.Task);
            Client = (TextView) convertview.findViewById(R.id.Client);
            JobName = (TextView) convertview.findViewById(R.id.JobName);
            Job_desc = (TextView) convertview.findViewById(R.id.Job_desc);
            Hours = (TextView) convertview.findViewById(R.id.Hours);
            Secondary_Tech = (TextView) convertview.findViewById(R.id.Secondary_Tech);
            view1 = convertview.findViewById(R.id.view1);
            Start_date = (TextView) convertview.findViewById(R.id.Start_date);
            Due_date = (TextView) convertview.findViewById(R.id.Due_date);
            parent_row =  convertview.findViewById(R.id.parent_row);

        }
    }
}

