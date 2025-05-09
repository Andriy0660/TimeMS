import {useMutation, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import useAppContext from "../context/useAppContext.js";

export default function useTimeLogMutations() {
  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {mutateAsync: create} = useMutation({
    mutationFn: (body) => timeLogApi.create(body),
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
      console.error("Creating time log failed:", error);
    }
  });

  const {mutateAsync: createTimeLogFromWorklog} = useMutation({
    mutationFn: (body) => timeLogApi.createFromWorklog(body),
    onSuccess: async () => {
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

  const {mutateAsync: divide} = useMutation({
    mutationFn: (id) => timeLogApi.divide(id),
    onSuccess: async () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time logs is successfully divided",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Dividing time logs failed:", error);
    }
  });

  const {mutateAsync: importTimeLogs} = useMutation({
    mutationFn: (body) => timeLogApi.importTimeLogs(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Imported successfully",
        type: "success"
      });

    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Importing failed:", error);
    }
  });

  const {mutateAsync: update} = useMutation({
    mutationFn: (body) => timeLogApi.update(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully updated",
        type: "success"
      });
    },
    onError: async (error, body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating time log failed:", error);
    }
  });

  const {mutateAsync: deleteTimeLog} = useMutation({
    mutationFn: (id) => timeLogApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully deleted time log",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting time log failed:", error);
    }
  });
  const {mutateAsync: setGroupDescription} = useMutation({
    mutationFn: (body) => timeLogApi.setGroupDescription(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully set description",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Setting group description failed:", error);
    }
  });
  const {mutateAsync: changeDate} = useMutation({
    mutationFn: (body) => timeLogApi.changeDate(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully changed date",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Changing date failed:", error);
    }
  });


  return {
    onCreate: create,
    onCreateTimeLogFromWorklog: createTimeLogFromWorklog,
    onImport: importTimeLogs,
    onDivide: divide,
    onUpdate: update,
    onDelete: deleteTimeLog,
    setGroupDescription: setGroupDescription,
    changeDate: changeDate,
  }
}