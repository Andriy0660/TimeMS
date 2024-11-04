import {useMutation, useQueryClient} from "@tanstack/react-query";
import useAppContext from "../context/useAppContext.js";
import externalTimeLogApi from "../api/externalTimeLogApi.js";

export default function useExternalTimeLogMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: createExternalTimeLog} = useMutation({
    mutationFn: (body) => externalTimeLogApi.create(body),
    onSuccess: async () => {
      queryClient.invalidateQueries(externalTimeLogApi.key);
      addAlert({
        text: "External time log is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating external time log failed:", error);
    }
  });

  const {mutateAsync: deleteExternalTimeLog} = useMutation({
    mutationFn: (id) => externalTimeLogApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(externalTimeLogApi.key);
      addAlert({
        text: "You have successfully deleted external time log",
        type: "success"
      });
    },
    onError: (error) => {
      queryClient.invalidateQueries(externalTimeLogApi.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting external time log failed:", error);
    }
  });

  return {
    onCreate: createExternalTimeLog,
    onDelete: deleteExternalTimeLog
  }
}