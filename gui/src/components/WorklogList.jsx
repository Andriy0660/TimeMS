import Worklog from "./Worklog.jsx";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import worklogApi from "../api/worklogApi.js";
import useAppContext from "../context/useAppContext.js";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress} from "@mui/material";
import {useEffect} from "react";
import NoLogs from "./NoLogs.jsx";

export default function WorklogList({worklogs, isWorklogsListing, listWorklogsError, isJiraEditMode}) {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: createTimeLogFromWorklog} = useMutation({
    mutationFn: (body) => timeLogApi.createFromWorklog(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating time log from worklog failed:", error);
    }
  });

  const {mutateAsync: deleteWorklog} = useMutation({
    mutationFn: (body) => worklogApi.delete(body.issueKey, body.id),
    onSuccess: () => {
      queryClient.invalidateQueries(worklogApi.key);
      addAlert({
        text: "You have successfully deleted worklog",
        type: "success"
      });
    },
    onError: (error) => {
      queryClient.invalidateQueries(worklogApi.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting worklog failed:", error);
    }
  });

  useEffect(() => {
    if (listWorklogsError) {
      addAlert({
        text: `${listWorklogsError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listWorklogsError]);

  if (isWorklogsListing) {
    return <div className="text-center">
      <CircularProgress />
    </div>
  }

  return (
    <div className={`m-4`}>
      <div className="flex flex-col items-center">
        <div className="w-full overflow-x-auto">
          {worklogs.length
            ? worklogs.map(worklog =>
              <Worklog
                key={worklog.id}
                worklog={worklog}
                onDelete={deleteWorklog}
                onTimeLogCreate={createTimeLogFromWorklog}
                isJiraEditMode={isJiraEditMode}
              />)
            : <NoLogs />
          }
        </div>
      </div>
    </div>
  );
}