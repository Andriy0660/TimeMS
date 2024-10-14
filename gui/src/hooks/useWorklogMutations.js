import {useMutation, useQueryClient} from "@tanstack/react-query";
import worklogApi from "../api/worklogApi.js";
import useAppContext from "../context/useAppContext.js";

export default function useWorklogMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: createWorklogFromTimeLog} = useMutation({
    mutationFn: (body) => worklogApi.create(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(worklogApi.key);
      addAlert({
        text: "Worklog is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating worklog failed:", error);
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

  return {
    onCreate: createWorklogFromTimeLog,
    onDelete: deleteWorklog
  }
}